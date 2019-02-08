package uk.ac.ebi.biosamples.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.ac.ebi.biosamples.model.BioSchemasContext;

import java.io.IOException;
import java.net.URI;

public class ContextDeserializer extends StdDeserializer<BioSchemasContext> {

    protected ContextDeserializer() {
        super(BioSchemasContext.class);
    }

    @Override
    public BioSchemasContext deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        BioSchemasContext context = new BioSchemasContext();

        JsonToken currentToken = jsonParser.getCurrentToken();
        if (currentToken.equals(JsonToken.START_ARRAY)) {
            while (jsonParser.hasCurrentToken() && !currentToken.equals(JsonToken.END_ARRAY)) {
                currentToken = jsonParser.nextToken();

                if (currentToken.equals(JsonToken.VALUE_STRING) && !jsonParser.getValueAsString().equals("http://schema.org")) {
                    // This is not the link to schema.org we expect to see
                    throw new JsonParseException(jsonParser, "BioSchemasContext should contain a single schema.org entry string");
                } else if (currentToken.equals(JsonToken.START_OBJECT)) {
                    while (jsonParser.hasCurrentToken() && !currentToken.equals(JsonToken.END_OBJECT)) {
                        currentToken = jsonParser.nextToken();
                        if (currentToken.equals(JsonToken.FIELD_NAME)) {
                            context.addOtherContexts(jsonParser.getValueAsString(), URI.create(jsonParser.nextTextValue()));
                        }
                    }
                }
            }
        } else {
            throw new JsonParseException(jsonParser, "BioSchemasContext should be an array");
        }

        return context;
    }
}
