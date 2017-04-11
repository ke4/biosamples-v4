package uk.ac.ebi.biosamples.solr.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import uk.ac.ebi.biosamples.model.Attribute;
import uk.ac.ebi.biosamples.model.Sample;
import uk.ac.ebi.biosamples.solr.model.SolrSample;

@Service
public class SampleToSolrSampleConverter implements Converter<Sample, SolrSample> {

	
	private final DateTimeFormatter solrDateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'Z'");
	
	@Override
	public SolrSample convert(Sample sample) {
		Map<String, List<String>> attributeValues = null;
		Map<String, List<String>> attributeIris = null;
		Map<String, List<String>> attributeUnits = null;
		
		if (sample.getAttributes() != null && sample.getAttributes().size() > 0) {
			attributeValues = new HashMap<>();
			attributeIris = new HashMap<>();
			attributeUnits = new HashMap<>();
			
			for (Attribute attr : sample.getAttributes()) {
				
				String key = attr.getType();
				//key = SolrSampleService.attributeTypeToField(key);
				
				String value = attr.getValue();
				//if its longer than 255 characters, don't add it to solr
				//solr cant index long things well, and its probably not useful for search
				if (value.length() > 255) {
					continue;
				}
				
				if (!attributeValues.containsKey(key)) {
					attributeValues.put(key, new ArrayList<>());
				}
				
				attributeValues.get(key).add(value);

				if (!attributeIris.containsKey(key)) {
					attributeIris.put(key, new ArrayList<>());
				}
				if (attr.getIri() == null) {
					attributeIris.get(key).add("");
				} else {
					attributeIris.get(key).add(attr.getIri().toString());
				}

				if (!attributeUnits.containsKey(key)) {
					attributeUnits.put(key, new ArrayList<>());
				}
				if (attr.getUnit() == null) {
					attributeUnits.get(key).add("");
				} else {
					attributeUnits.get(key).add(attr.getUnit());
				}
			}
		}
		
		String releaseSolr = formatDate(sample.getRelease());
		String updateSolr = formatDate(sample.getUpdate());		
				
		
		return SolrSample.build(sample.getName(), sample.getAccession(), releaseSolr, updateSolr,
				attributeValues, attributeIris, attributeUnits);
	}
	
	private String formatDate(LocalDateTime d) {
		//this ensures that all components are present, even if they default to zero
		int year = d.getYear();
		int month = d.getMonthValue();
		int dayOfMonth = d.getDayOfMonth();
		int hour = d.getHour();
		int minute = d.getMinute();
		int second = d.getSecond();
		int nano = d.getNano();	
		
		d = LocalDateTime.of(year,month,dayOfMonth,hour,minute,second,nano);		
		String solrDate =  solrDateTimeFormatter.format(d);
		return solrDate;
	}
}
