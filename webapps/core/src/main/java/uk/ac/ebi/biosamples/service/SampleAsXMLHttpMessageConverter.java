package uk.ac.ebi.biosamples.service;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import uk.ac.ebi.biosamples.model.Sample;
import uk.ac.ebi.biosamples.service.SampleToXmlConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SampleAsXMLHttpMessageConverter extends AbstractHttpMessageConverter<Sample> {
	
	private final SampleToXmlConverter sampleToXmlConverter;
	
	private final List<MediaType> DEFAULT_SUPPORTED_MEDIA_TYPES = Arrays.asList(MediaType.APPLICATION_XML, MediaType.TEXT_XML);

	private Logger log = LoggerFactory.getLogger(getClass());

	public SampleAsXMLHttpMessageConverter(SampleToXmlConverter sampleToXmlConverter) {
		this.setSupportedMediaTypes(this.DEFAULT_SUPPORTED_MEDIA_TYPES);
		this.sampleToXmlConverter = sampleToXmlConverter;
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return Sample.class.isAssignableFrom(clazz);
	}

	@Override
	protected Sample readInternal(Class<? extends Sample> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new HttpMessageNotReadableException("Cannot read xml"); 
	}

	@Override
	protected void writeInternal(Sample sample, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		Document doc = sampleToXmlConverter.convert(sample);
		OutputFormat format = OutputFormat.createCompactFormat();
		XMLWriter writer = new XMLWriter(outputMessage.getBody(), format);
		writer.write(doc);
	}

}
