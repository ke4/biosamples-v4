package uk.ac.ebi.biosamples.curatedview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biosamples.PipelinesProperties;
import uk.ac.ebi.biosamples.client.BioSamplesClient;
import uk.ac.ebi.biosamples.mongo.model.MongoSample;
import uk.ac.ebi.biosamples.mongo.repo.MongoSampleRepository;
import uk.ac.ebi.biosamples.mongo.service.SampleToMongoSampleConverter;
import uk.ac.ebi.biosamples.utils.AdaptiveThreadPoolExecutor;
import uk.ac.ebi.biosamples.utils.MailSender;
import uk.ac.ebi.biosamples.utils.ThreadUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

@Component
public class CuratedViewApplicationRunner implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CuratedViewApplicationRunner.class);

    private final BioSamplesClient bioSamplesClient;
    private final PipelinesProperties pipelinesProperties;
    private final MongoSampleRepository repository;
    private final SampleToMongoSampleConverter sampleToMongoSampleConverter;
    private final MongoOperations mongoOperations;

    public CuratedViewApplicationRunner(BioSamplesClient bioSamplesClient,
                                        PipelinesProperties pipelinesProperties,
                                        MongoSampleRepository repository,
                                        SampleToMongoSampleConverter sampleToMongoSampleConverter,
                                        MongoOperations mongoOperations) {
        this.bioSamplesClient = bioSamplesClient;
        this.pipelinesProperties = pipelinesProperties;
        this.repository = repository;
        this.sampleToMongoSampleConverter = sampleToMongoSampleConverter;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Future<Void>> futures = new HashMap<>();
        Instant startTime = Instant.now();
        LOG.info("Pipeline started at {}", startTime);
        long sampleCount = 0;
        boolean isPassed = true;

        try (AdaptiveThreadPoolExecutor executorService = AdaptiveThreadPoolExecutor.create(100, 10000, true,
                pipelinesProperties.getThreadCount(), pipelinesProperties.getThreadCountMax())) {

            try (CloseableIterator<MongoSample> it = mongoOperations.stream(new Query(), MongoSample.class)) {
                while (it.hasNext()) {
                    MongoSample mongoSample = it.next();
                    String accession = mongoSample.getAccession();
                    LOG.trace("Handling {}", accession);

                    Callable<Void> task = new CuratedViewCallable(accession, repository, sampleToMongoSampleConverter, bioSamplesClient);
                    futures.put(accession, executorService.submit(task));

                    if (++sampleCount % 5000 == 0) {
                        LOG.info("Scheduled {} samples for processing", sampleCount);
                    }
                }
                LOG.info("Waiting for all scheduled tasks to finish");
                ThreadUtils.checkFutures(futures, 0);
            }

        } catch (Exception e) {
            LOG.error("Pipeline failed to finish successfully", e);
            isPassed = false;
            throw e;
        } finally {
            final ConcurrentLinkedQueue<String> failureQueue = CuratedViewCallable.failedQueue;
            if (failureQueue.size() > 0) {
                final List<String> fails = new LinkedList<>();

                while (failureQueue.peek() != null) {
                    fails.add(failureQueue.poll());
                }

                final String failures = "Failed files (" + fails.size() + ") " + String.join(" , ", fails);

                MailSender.sendEmail("Copy-down", failures, isPassed);
            }

            logPipelineStat(startTime, sampleCount);
        }
    }

    private void logPipelineStat(Instant startTime, long sampleCount) {
        Instant endTime = Instant.now();
        LOG.info("Total samples processed {}", sampleCount);
        LOG.info("Pipeline finished at {}", endTime);
        LOG.info("Pipeline total running time {} seconds", Duration.between(startTime, endTime).getSeconds());
    }

}
