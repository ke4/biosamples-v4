package uk.ac.ebi.biosamples.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biosamples.model.structured.AMREntry;
import uk.ac.ebi.biosamples.model.structured.AMRTable;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
@TestPropertySource(properties={"spring.jackson.serialization.INDENT_OUTPUT=true"})
public class AmrJsonConversionTest {

    Logger log = LoggerFactory.getLogger(getClass());

    JacksonTester<AMREntry> amrEntryJacksonTester;
    JacksonTester<AMRTable> amrTableJacksonTester;

    @Before
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    public void testAmrEntrySerializer() throws IOException {
        AMREntry entry = new AMREntry.Builder()
                .withAntibiotic("A")
                .withResistancePhenotype("Something")
                .withMeasure("==", 10, "mg/L")
                .withVendor("in-house")
                .withTestingStandard("VeryHighStandard")
                .withLaboratoryTypingMethod("TypingMethod")
                .build();

        JsonContent<AMREntry> json = this.amrEntryJacksonTester.write(entry);

        log.info(json.getJson());

        assertThat(json).hasJsonPathStringValue("@.antibiotic", "A");
    }

    @Test
    public void testAmrTableSerializer() throws IOException {
        AMRTable.Builder tableBuilder = new AMRTable.Builder("http://some-fake-schema.com");
        tableBuilder.withEntry(new AMREntry.Builder()
                .withAntibiotic("A")
                .withResistancePhenotype("Something")
                .withMeasure("==", 10, "mg/L")
                .withVendor("in-house")
                .withTestingStandard("VeryHighStandard")
                .withLaboratoryTypingMethod("TypingMethod")
                .build());

        tableBuilder.withEntry(new AMREntry.Builder()
                .withAntibiotic("B")
                .withResistancePhenotype("pectine")
                .withMeasure(">=", 14, "mg/L")
                .withVendor("GSKey")
                .withTestingStandard("low-quality-standard")
                .withLaboratoryTypingMethod("Nothing")
                .build());

        AMRTable table = tableBuilder.build();

        JsonContent<AMRTable> json = this.amrTableJacksonTester.write(table);
        log.info(json.getJson());

        assertThat(json).hasJsonPathValue("@.schema", "http://some-fake-schema.com");
        assertThat(json).hasJsonPathValue("@.type", "AMR");

        assertThat(json).hasJsonPathArrayValue("@.content");
        assertThat(json).extractingJsonPathArrayValue("@.content").hasSize(2);
        assertThat(json).extractingJsonPathArrayValue("@.content").hasOnlyElementsOfType(LinkedHashMap.class);

        assertThat(json).extractingJsonPathMapValue("@.content[1]").containsKeys(
                "antibiotic", "resistancePhenotype", "testingStandard", "vendor", "measurementUnit",
                "laboratoryTypingMethod", "measurementSign", "measurementValue"
        );
        assertThat(json).extractingJsonPathMapValue("@.content[1]").containsEntry("measurementValue", 10);

    }

    @Test
    public void testAMRDeserialization() throws Exception{
        AMRTable.Builder tableBuilder = new AMRTable.Builder("test");
        tableBuilder.withEntry(new AMREntry.Builder()
                .withAntibiotic("ampicillin")
                .withResistancePhenotype("susceptible")
                .withMeasure("==", 2, "mg/L")
                .withVendor("in-house")
                .withTestingStandard("CLSI")
                .withLaboratoryTypingMethod("MIC")
                .build());
        AMRTable table = tableBuilder.build();
        // Assert sample with AMR table entry

        assertThat(this.amrTableJacksonTester.readObject("/AMRSample.json")).isEqualTo(table);


    }


}
