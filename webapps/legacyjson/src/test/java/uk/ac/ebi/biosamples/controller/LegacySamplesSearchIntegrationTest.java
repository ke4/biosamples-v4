package uk.ac.ebi.biosamples.controller;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.biosamples.service.SampleRepository;


@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class LegacySamplesSearchIntegrationTest {

	@MockBean
	private SampleRepository sampleRepositoryMock;

	@Autowired
    private MockMvc mockMvc;

	@Test
	@Ignore
	public void testSearchEndpointStatusIs200() throws Exception {
	    /*TODO */
	}

	@Test
	@Ignore
	public void testAllSearchLinksAreAvailable() throws Exception {
	    /*TODO */
	}


	@Test
	@Ignore
	public void testSearchFirstSampleByGroupReturnCorrectSample() throws Exception {
	    /*TODO */
	}
	
	@Test
	@Ignore
	public void testSearchByAccessionAndGroupReturnCorrectSample() throws Exception {
	    /*TODO */
	}
	
	@Test
	@Ignore
	public void testSearchByTextReturnSamplesContainingText() throws Exception {
	    /*TODO To implement in the end-to-end test*/
	}

	@Test
	@Ignore
	public void testSearchByTextAndGroupFilterSamplesInAGroupByText() throws Exception {
	    /*TODO implement in end-to-end test*/
	}
	
	@Test
	@Ignore
	public void testSearchByGroupReturnSamplesInAGroup() throws Exception {
	    /*TODO */
	}

	@Test
	@Ignore
	public void testSearchByAccessionReturnSampleWithThatAccession() throws Exception {
	    /*TODO */
	}

	@Test
	@Ignore
	public void testSearchContainsSelfLink() throws Exception {
	    /*TODO */
	}


	
	


}
