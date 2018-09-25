package uk.gov.dwp.gysp.pdf;

import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
public class ServiceControllerIT {

	@Autowired
	private Environment env;

	private RestTemplate restTemplate = new RestTemplate();

	@Test
	public void postGeneratePdfShouldReturnStatus200() throws IOException {
		// Given
		final String json = "{" + "\"address\": {" + "\"buildingNumber\": \"100\"," + "\"county\": \"Tyne & Wear\","
				+ "\"postCode\": \"NE6 5LS\"," + "\"street\": \"Warton Terrace\"," + "\"town\": \"Newcastle\"" + "},"
				+ "\"dob\": \"2016-05-27T10:59:10.050Z\"," + "\"firstName\": \"Samba\"," + "\"nino\": \"SS112233A\","
				+ "\"surname\": \"Mitra\"" + "}";

		// When
		final ResponseEntity<byte[]> response = this.restTemplate
				.postForEntity(getPdfServiceApiEndpoint() + "/generatePdf", json, byte[].class);

		// Then
		assertEquals("Response status code not as expected", response.getStatusCode(), HttpStatus.OK);
		assertEquals("Response content type not as expected",
				response.getHeaders().getContentType().getType() + "/"
						+ response.getHeaders().getContentType().getSubtype(),
				MediaType.APPLICATION_OCTET_STREAM_VALUE);

		PDFParser pdfParser = new PDFParser(new RandomAccessBuffer(response.getBody()));
		pdfParser.parse();

		// Here parse() method will parse the stream and populate the in-memory
		// representation of the PDF document i.e. COSDocument object

		String output = new PDFTextStripper().getText(pdfParser.getPDDocument());
		String[] outputContents = output.split("\n");
		assertEquals("Address not as expected", "address", outputContents[0]);
		assertEquals("Building number not as expected", "buildingNumber", outputContents[1]);
		assertEquals("Building number value not as expected", "100", outputContents[2]);

		pdfParser.getPDDocument().close();
	}

	@Test
	public void postGeneratePdfWithInvalidJsonShouldReturnStatus400() {
		// Given - Invalid Json not ended properly
		final String json = "{" + "\"address\": {" + "\"buildingNumber\": \"100\"," + "\"county\": \"Tyne & Wear\","
				+ "\"postCode\": \"NE6 5LS\"," + "\"street\": \"Warton Terrace\"," + "\"town\": \"Newcastle\"" + "}";

		// When
		try {
			this.restTemplate.postForEntity(getPdfServiceApiEndpoint() + "/generatePdf", json, byte[].class);
			// Then
		} catch (final HttpClientErrorException ex) {
			assertEquals("HTTP status code not as expected.", HttpStatus.BAD_REQUEST, ex.getStatusCode());
			return;
		}
		fail("Expected 500 Internal Server Error was not returned.");
	}

	private String getPdfServiceApiEndpoint() {
		return this.env.getProperty("service.api.url");
	}

}
