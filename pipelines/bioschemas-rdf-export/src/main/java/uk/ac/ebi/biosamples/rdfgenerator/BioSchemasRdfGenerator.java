package uk.ac.ebi.biosamples.rdfgenerator;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class BioSchemasRdfGenerator implements Callable<Void> {
    private Logger log = LoggerFactory.getLogger(getClass());
    private static File file;
    private static long sampleCount = 0;
    private final URL url;

    public static void setFilePath(String filePath) {
        file = new File(filePath);
    }

    BioSchemasRdfGenerator(final URL url) {
        log.info("HANDLING " + url.toString() + " and the current sample count is: " + ++sampleCount);

        this.url = url;
    }

    @Override
    public Void call() throws Exception {
        requestHTTPAndHandle(this.url);

        return null;
    }

    private static void requestHTTPAndHandle(final URL url) throws Exception {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int response;

        try {
            conn.setRequestMethod("GET");
            conn.connect();
            response = conn.getResponseCode();

            if (response == 200) {
                handleSuccessResponses(url);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            conn.disconnect();
        }
    }

    private static void handleSuccessResponses(final URL url) {
        try (Scanner sc = new Scanner(url.openStream())) {
            final StringBuilder sb = new StringBuilder();

            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }

            try (InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8))) {
                String dataAsRdf = readRdfToString(in);

                write(dataAsRdf);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings(value = "deprecation")
    private static void write(final String sampleData) throws Exception {
        FileUtils.writeStringToFile(file, sampleData, true);
    }

    /**
     * @param in a rdf input stream
     * @return a string representation
     */
    private static String readRdfToString(final InputStream in) {
        return graphToString(readRdfToGraph(in));
    }

    /**
     * @param inputStream an Input stream containing rdf data
     * @return a Graph representing the rdf in the input stream
     */
    private static Collection<Statement> readRdfToGraph(final InputStream inputStream) {
        try {
            final RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);
            final StatementCollector collector = new StatementCollector();

            rdfParser.setRDFHandler(collector);
            rdfParser.parse(inputStream, "");

            return collector.getStatements();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms a graph to a string.
     *
     * @param myGraph a sesame rdf graph
     * @return a rdf string
     */
    private static String graphToString(final Collection<Statement> myGraph) {
        final StringWriter out = new StringWriter();
        final TurtleWriterCustom turtleWriterCustom = new TurtleWriterCustom(out);

        return modifyIdentifier(writeRdfInTurtleFormat(myGraph, out, turtleWriterCustom));
    }

    private static String modifyIdentifier(String rdfString) {
        if (rdfString != null)
            rdfString = rdfString.replaceAll("biosample:", "");

        return rdfString;
    }

    private static String writeRdfInTurtleFormat(Collection<Statement> myGraph, StringWriter out, TurtleWriterCustom writer) {
        try {
            writer.startRDF();
            handleNamespaces(writer);

            for (Statement st : myGraph) {
                writer.handleStatement(st);
                //below line is commented: for short RDF
                //writer.writeValue(st.getObject(),O true);
            }

            writer.endRDF();
        } catch (final RDFHandlerException e) {
            throw new RuntimeException(e);
        }

        return out.getBuffer().toString();
    }

    private static void handleNamespaces(final TurtleWriterCustom writer) {
        writer.handleNamespace("schema", "http://schema.org/");
        writer.handleNamespace("obo", "http://purl.obolibrary.org/obo/");
        writer.handleNamespace("ebi-bsd", "https://www.ebi.ac.uk/biosamples/");
        writer.handleNamespace("biosamples", "http://identifiers.org/biosample/");
    }
}

