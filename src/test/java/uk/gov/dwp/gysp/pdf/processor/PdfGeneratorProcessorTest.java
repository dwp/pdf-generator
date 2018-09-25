package uk.gov.dwp.gysp.pdf.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.dwp.gysp.pdf.Application;
import uk.gov.dwp.gysp.pdf.PdfGeneratorException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes={Application.class})
@ActiveProfiles("test")
public class PdfGeneratorProcessorTest {

	private static PDFParser parseToPdf(final byte[] result) throws IOException {
		final PDFParser pdfParser = new PDFParser(new RandomAccessBuffer(result));
		pdfParser.parse();
		return pdfParser;
	}

	@Autowired
	private PdfGeneratorProcessor processor;

	@Test
	public void confirmPDFDocumentIsCreatedWithJSON()
			throws JsonProcessingException, IOException, PdfGeneratorException {
		// Given
		final JsonNode json = new ObjectMapper().readTree("{\"First Name\":\"Samba\"}");

		// When
		final byte[] result = this.processor.generatePdfStream(json);

		// Then
		assertNotNull("Response should not be null", result);
		final PDFParser pdfParser = parseToPdf(result);

		final String output = new PDFTextStripper().getText(pdfParser.getPDDocument());
		final String[] outputContents = output.split(System.getProperty("line.separator"));
		assertEquals("First name not as expected", "First Name", outputContents[0]);
		assertEquals("First name value not as expected", "Samba", outputContents[1]);
	}

	@Test
	public void emptyJsonReturnsStream() throws JsonProcessingException, IOException, PdfGeneratorException {
		// Given
		final JsonNode json = new ObjectMapper().readTree("{}");

		// When
		final byte[] result = this.processor.generatePdfStream(json);

		// Then
		assertNotNull("Response should not be null", result);

		final PDFParser pdfParser = parseToPdf(result);

		assertNotNull("Response should include a Pdf document", pdfParser.getPDDocument());
	}

	@Before
	public void setUp() {
		this.processor = new PdfGeneratorProcessor();
	}

}
