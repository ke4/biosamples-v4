package uk.ac.ebi.biosamples.service;

import java.time.Instant;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import uk.ac.ebi.biosamples.model.Attribute;
import uk.ac.ebi.biosamples.model.ExternalReference;
import uk.ac.ebi.biosamples.model.Relationship;
import uk.ac.ebi.biosamples.model.Sample;
import uk.ac.ebi.biosamples.utils.XmlPathBuilder;

@Service
public class XmlGroupToSampleConverter implements Converter<Document, Sample>  {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	public Sample convert(Document doc) {
		
		Instant release = null;
		Instant update = null;
		String accession = null;
		if (XmlPathBuilder.of(doc).attributeExists("id")) {
			accession = XmlPathBuilder.of(doc).attribute("id");
		}
		String name = null;
		SortedSet<Attribute> attributes = new TreeSet<>();
		SortedSet<Relationship> relationships = new TreeSet<>();
		SortedSet<ExternalReference> externalReferences = new TreeSet<>();
		
		for (Element property : XmlPathBuilder.of(doc).elements("Property")){
			if ("Group Name".equals(XmlPathBuilder.of(property).attribute("class"))) {
				name = XmlPathBuilder.of(property).path("QualifiedValue", "Value").text();
			} else if ("Group Description".equals(XmlPathBuilder.of(property).attribute("class"))) {
				String value = XmlPathBuilder.of(property).path("QualifiedValue", "Value").text();
				attributes.add(Attribute.build("description", value, null, null));	
			} else if ("Submission Release Date".equals(XmlPathBuilder.of(property).attribute("class"))) {
				release = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(XmlPathBuilder.of(property).path("QualifiedValue", "Value").text()));
			} else if ("Submission Update Date".equals(XmlPathBuilder.of(property).attribute("class"))) {
				update = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(XmlPathBuilder.of(property).path("QualifiedValue", "Value").text()));
			} else {
				String type = XmlPathBuilder.of(property).attribute("class");
				String value = XmlPathBuilder.of(property).path("QualifiedValue", "Value").text();
				String iri = null;
				String unit = null;
				
				if (XmlPathBuilder.of(property).path("QualifiedValue", "TermSourceREF").exists()
						&& XmlPathBuilder.of(property).path("QualifiedValue", "TermSourceREF", "TermSourceID").exists()) {
					iri = XmlPathBuilder.of(property).path("QualifiedValue", "TermSourceREF", "TermSourceID").text();
				}
				
				if (XmlPathBuilder.of(property).path("QualifiedValue", "TermSourceREF").exists()
						&& XmlPathBuilder.of(property).path("QualifiedValue", "TermSourceREF", "Unit").exists()) {
					unit = XmlPathBuilder.of(property).path("QualifiedValue", "TermSourceREF", "Unit").text();
				}
				
				attributes.add(Attribute.build(type, value, iri, unit));				
			}
			//TODO relationships
			//TODO contacts
			//TODO organisations
		}

		for (Element database : XmlPathBuilder.of(doc).elements("Database")){
			if (XmlPathBuilder.of(database).path("URI").exists()) {
				externalReferences.add(ExternalReference.build(XmlPathBuilder.of(database).path("URI").text()));
			}
		}
		
		log.info("name = "+name);
		log.info("accession = "+accession);
		log.info("release = "+release);
		log.info("update = "+update);
		log.info("attributes = "+attributes);
		log.info("relationships = "+relationships);
		log.info("relationships = "+relationships);
		
		return Sample.build(name, accession, null, release, update, attributes, relationships, externalReferences);
	}

}