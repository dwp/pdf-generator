package uk.gov.dwp.gysp.pdf.processor;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import uk.gov.dwp.gysp.pdf.PdfGenerator;
import uk.gov.dwp.gysp.pdf.PdfGeneratorException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class PdfGeneratorProcessor {

	private static final String UNABLE_TO_GENERATE_PDF_DOCUMENT = "Unable to generate pdf document";

	public byte[] generatePdfStream(final JsonNode json) throws PdfGeneratorException {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try (PDDocument document = new PdfGenerator(json).generate()) {
			document.save(stream);
		} catch (final IOException e) {
			throw new PdfGeneratorException(UNABLE_TO_GENERATE_PDF_DOCUMENT, e);
		}
		return stream.toByteArray();
	}
}
