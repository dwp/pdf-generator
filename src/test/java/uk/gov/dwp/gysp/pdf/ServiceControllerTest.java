package uk.gov.dwp.gysp.pdf;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.dwp.gysp.pdf.processor.PdfGeneratorProcessor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@WebMvcTest(ServiceController.class)
@WebAppConfiguration
public class ServiceControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private PdfGeneratorProcessor pdfGeneratorProcessor;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Test
	public void postGeneratePdfWithInvalidJsonShouldReturnStatus400() throws Exception {
		// Given - Invalid Json
		final String json = "{\"}";

		// When
		final ResultActions action = this.mockMvc
				.perform(post("/api/pdfGenerator/generatePdf").contentType(MediaType.APPLICATION_JSON).content(json));

		// Then
		action.andExpect(status().isBadRequest());
	}

	@Test
	public void postGeneratePdfWithShouldReturnStatus200() throws Exception {
		// Given
		final String json = "{}";
		given(pdfGeneratorProcessor.generatePdfStream(any(JsonNode.class))).willReturn(new byte[2]);

		// When
		final ResultActions action = this.mockMvc
				.perform(post("/api/pdfGenerator/generatePdf").contentType(MediaType.APPLICATION_JSON).content(json));

		// Then
		action.andExpect(status().isOk());
		final MvcResult result = action.andReturn();
		assertEquals("Content type not as expected", MediaType.APPLICATION_OCTET_STREAM_VALUE,
				result.getResponse().getContentType());

	}

	@Before
	public void setup() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
	}
}
