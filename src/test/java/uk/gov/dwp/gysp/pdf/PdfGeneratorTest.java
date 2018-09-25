package uk.gov.dwp.gysp.pdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class PdfGeneratorTest {

	private static String getPdfContent(final PDDocument pdf) throws IOException {
		final File file = File.createTempFile("temp", ".pdf");
		pdf.save(file);
		final PDFTextStripper pts = new PDFTextStripper();
		return pts.getText(pdf);
	}

	@Test
	public void emptyJsonReturnsPdf() throws JsonProcessingException, IOException {
		// Given
		final int expectedPageNo = 1;
		final JsonNode json = new ObjectMapper().readTree("{}");

		// When
		final PDDocument result = new PdfGenerator(json).generate();
		// Then
		assertNotNull("Document expected to be produced.", result);
		assertEquals("At least one page should always be provided.", expectedPageNo, result.getNumberOfPages());
	}

	@Test
	public void isTwoPagePdf() throws JsonProcessingException, IOException {
		// Given
		final int expectedPageNo = 2;

		final StringBuffer jsonInput = new StringBuffer();
		jsonInput.append("{ \"Stuff\": {");
		final int itemsToGenerate = 20;
		for (int i = 0; i < itemsToGenerate; i++) {
			jsonInput.append(String.format(" \"First Name%s\":\"Vasudev%s\" ", i, i));
			if (i < itemsToGenerate - 1) {
				jsonInput.append(",");
			}
		}
		jsonInput.append(" } }");

		final JsonNode json = new ObjectMapper().readTree(jsonInput.toString());
		// When
		final PDDocument result = new PdfGenerator(json).generate();
		// Then
		assertEquals("Number of pages not as expected.", expectedPageNo, result.getNumberOfPages());
	}

	@Test
	public void pdfContainsExpectedContentForEmbeddedArrayObjects() throws JsonProcessingException, IOException {
		// Given
		final String objectName = "ArrayOfNos";
		final String firstItemName = "First";
		final String firstItemValue = "One";
		final String secondItemName = "Second";
		final String secondItemValue = "Two";
		final String thirdItemName = "Third";
		final String thirdItemValue = "Three";
		final JsonNode json = new ObjectMapper().readTree(
				"{\"" + objectName + "\": [{\"" + firstItemName + "\":\"" + firstItemValue + "\"},{\"" + secondItemName
						+ "\":\"" + secondItemValue + "\"},{\"" + thirdItemName + "\":\"" + thirdItemValue + "\"}]}");

		// When
		final PDDocument result = new PdfGenerator(json).generate();

		// Then
		final String content = getPdfContent(result);
		assertNotNull("Content should not be null.", content);
		assertTrue("Object Name JSON value expected to be present.", content.contains(objectName));
		assertTrue("First array item name expected to be present.", content.contains(firstItemName));
		assertTrue("First array item value expected to be present.", content.contains(firstItemValue));
		assertTrue("Second array item name expected to be present.", content.contains(secondItemName));
		assertTrue("Second array item value expected to be present.", content.contains(secondItemValue));
		assertTrue("Third array item name expected to be present.", content.contains(thirdItemName));
		assertTrue("Third array item value expected to be present.", content.contains(thirdItemValue));
	}

	@Test
	public void pdfContainsExpectedContentForEmbeddedArrayValues() throws JsonProcessingException, IOException {
		// Given
		final String objectName = "ArrayOfNos";
		final String firstItem = "One";
		final String secondItem = "Two";
		final String thirdItem = "Three";
		final JsonNode json = new ObjectMapper().readTree(
				"{\"" + objectName + "\": [\"" + firstItem + "\",\"" + secondItem + "\",\"" + thirdItem + "\"]}");

		// When
		final PDDocument result = new PdfGenerator(json).generate();

		// Then
		final String content = getPdfContent(result);
		assertNotNull("Content should not be null.", content);
		assertTrue("Object Name JSON value expected to be present.", content.contains(objectName));
		assertTrue("First array item expected to be present.", content.contains(firstItem));
		assertTrue("Second array item expected to be present.", content.contains(secondItem));
		assertTrue("Third array item expected to be present.", content.contains(thirdItem));
	}

	@Test
	public void pdfContainsExpectedContentForEmbeddedObject() throws JsonProcessingException, IOException {
		// Given
		final String objectName = "Person";
		final String name = "First Name";
		final String value = "Samba";
		final JsonNode json = new ObjectMapper()
				.readTree("{\"" + objectName + "\": {\"" + name + "\":\"" + value + "\"}}");

		// When
		final PDDocument result = new PdfGenerator(json).generate();

		// Then
		final String content = getPdfContent(result);
		assertNotNull("Content should not be null.", content);
		assertTrue("Object Name JSON value expected to be present.", content.contains(objectName));
		assertTrue("Name JSON value expected to be present.", content.contains(name));
		assertTrue("Value JSON value expected to be present", content.contains(value));
	}

	@Test
	public void pdfContainsExpectedContentMultipleNodes() throws JsonProcessingException, IOException {
		// Given
		final String firstFieldName = "First Name";
		final String firstFieldValue = "Samba";
		final String secondFieldName = "Last Name";
		final String secondFieldValue = "Mitra";

		final JsonNode json = new ObjectMapper().readTree("{\"" + firstFieldName + "\":\"" + firstFieldValue + "\", \""
				+ secondFieldName + "\":\"" + secondFieldValue + "\"}");

		// When
		final PDDocument result = new PdfGenerator(json).generate();

		// Then
		final String content = getPdfContent(result);

		assertNotNull("Content should not be null.", content);
		assertTrue("First Name JSON field name expected to be present.", content.contains(firstFieldName));
		assertTrue("First Name JSON field value expected to be present.", content.contains(firstFieldValue));
		assertTrue("Last Name JSON field name expected to be present.", content.contains(secondFieldName));
		assertTrue("Last Name JSON field value expected to be present.", content.contains(secondFieldValue));
	}

	@Test
	public void pdfContainsExpectedContentSingleNode() throws JsonProcessingException, IOException {
		// Given
		final String name = "First Name";
		final String value = "Samba";
		final JsonNode json = new ObjectMapper().readTree("{\"" + name + "\":\"" + value + "\"}");

		// When
		final PDDocument result = new PdfGenerator(json).generate();

		// Then
		final String content = getPdfContent(result);
		assertNotNull("Content should not be null.", content);
		assertTrue("Name JSON value expected to be present.", content.contains(name));
		assertTrue("Value JSON value expected to be present", content.contains(value));
	}

	@Test
	public void pdfContentInExpectedOrder() throws JsonProcessingException, IOException {
		// Given
		final int expectedNoLines = 6;

		final String firstFieldName = "Field1";
		final String firstFieldValue = "One";
		final String secondFieldName = "Field2";
		final String secondFieldValue = "Two";
		final String thirdFieldName = "Field3";
		final String thirdFieldValue = "Three";

		final JsonNode json = new ObjectMapper()
				.readTree("{\"" + firstFieldName + "\":\"" + firstFieldValue + "\", \"" + secondFieldName + "\":\""
						+ secondFieldValue + "\", \"" + thirdFieldName + "\":\"" + thirdFieldValue + "\"}");
		// When
		final PDDocument result = new PdfGenerator(json).generate();

		// Then
		final String content = getPdfContent(result);
		final String[] lines = content.split(System.getProperty("line.separator"));

		assertEquals("No of lines in PDF different from expected", expectedNoLines, lines.length);
		assertEquals("First Name JSON field name expected to first.", 0, ArrayUtils.indexOf(lines, firstFieldName));
		assertEquals("First Name JSON field value expected to first.", 1, ArrayUtils.indexOf(lines, firstFieldValue));
		assertEquals("Second Name JSON field name expected to first.", 2, ArrayUtils.indexOf(lines, secondFieldName));
		assertEquals("Second Name JSON field value expected to first.", 3, ArrayUtils.indexOf(lines, secondFieldValue));
		assertEquals("Third Name JSON field name expected to first.", 4, ArrayUtils.indexOf(lines, thirdFieldName));
		assertEquals("Third Name JSON field value expected to first.", 5, ArrayUtils.indexOf(lines, thirdFieldValue));
	}

	@Test
	public void pdfIsGenerated() throws JsonProcessingException, IOException {
		// Given
		final JsonNode json = new ObjectMapper().readTree("{\"First Name\":\"Samba\"}");
		// When
		final PDDocument result = new PdfGenerator(json).generate();
		// Then
		assertNotNull("PDF Document should not be null.", result);
	}

	@Test
	public void unusedPagesRemoved() throws JsonProcessingException, IOException {
		// Given
		final int expectedPageNo = 1;
		final JsonNode json = new ObjectMapper().readTree("{\"First Name\":\"Samba\"}");
		// When
		final PDDocument result = new PdfGenerator(json).generate();
		// Then
		assertEquals("Number of pages not as expected.", expectedPageNo, result.getNumberOfPages());
	}

	@Test
	public void pdfContentIncludesUnicode() throws JsonProcessingException, IOException {
		// Given
		final int expectedNoLines = 6;

		final String firstFieldName = "Fieĺd1";
		final String firstFieldValue = "OneĊ";
		final String secondFieldName = "Fiĕld2";
		final String secondFieldValue = "TwoĀ";
		final String thirdFieldName = "FielĐ3";
		final String thirdFieldValue = "Threeĩ";

		final JsonNode json = new ObjectMapper()
				.readTree("{\"" + firstFieldName + "\":\"" + firstFieldValue + "\", \"" + secondFieldName + "\":\""
						+ secondFieldValue + "\", \"" + thirdFieldName + "\":\"" + thirdFieldValue + "\"}");
		// When
		final PDDocument result = new PdfGenerator(json).generate();

		// Then
		final String content = getPdfContent(result);
		final String[] lines = content.split(System.getProperty("line.separator"));

		assertEquals("No of lines in PDF different from expected", expectedNoLines, lines.length);
		assertEquals("First Name JSON field name expected to first.", 0, ArrayUtils.indexOf(lines, firstFieldName));
		assertEquals("First Name JSON field value expected to first.", 1, ArrayUtils.indexOf(lines, firstFieldValue));
		assertEquals("Second Name JSON field name expected to first.", 2, ArrayUtils.indexOf(lines, secondFieldName));
		assertEquals("Second Name JSON field value expected to first.", 3, ArrayUtils.indexOf(lines, secondFieldValue));
		assertEquals("Third Name JSON field name expected to first.", 4, ArrayUtils.indexOf(lines, thirdFieldName));
		assertEquals("Third Name JSON field value expected to first.", 5, ArrayUtils.indexOf(lines, thirdFieldValue));
	}
}

