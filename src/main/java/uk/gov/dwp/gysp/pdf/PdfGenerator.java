package uk.gov.dwp.gysp.pdf;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.gysp.pdf.processor.PdfGeneratorProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;

public class PdfGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfGenerator.class);

	private static final int FONT_SIZE = 10;
	private static final float MARGIN = 72;
	private static final float Y_DECREMENT = 20;

	private PDFont boldFont;
	private PDPageContentStream contentStream;
	private PDPage currentPage;
	private PDDocument doc;
	private PDFont font;
	private float heightCounter;
	private final JsonNode json;
	private float leftMarginOffset;
	private float pageEndY;
	private int pageIndex = 0;
	private float pageStartY;

	public PdfGenerator(final JsonNode json) {
		this.json = json;
	}

	public PDDocument generate() throws IOException {
		initPdfDocument();
		return populateDocument();
	}

	private static InputStream getArialBoldFontStream() {
		return PdfGeneratorProcessor.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/Roboto-Bold.ttf");
	}

	private static InputStream getArialFontStream() {
		return PdfGeneratorProcessor.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/Roboto-Regular.ttf");
	}

	private void addFormAnswer(final JsonNode valueNode) throws IOException {
		this.contentStream.beginText();
		this.contentStream.setFont(this.font, FONT_SIZE);
		this.contentStream.newLineAtOffset(this.leftMarginOffset, this.heightCounter);
		this.contentStream.showText(valueNode.textValue());
		this.contentStream.endText();
		decrementHeight();
		decrementHeight();

		if (nextPage()) {
			initNextPage();
		}
	}

	private void addFormQuestion(final Entry<String, JsonNode> entry) throws IOException {

		if (nextPageQ()) {
			initNextPage();
		}
		this.contentStream.beginText();
		this.contentStream.setFont(this.boldFont, FONT_SIZE);
		this.contentStream.newLineAtOffset(this.leftMarginOffset, this.heightCounter);
		this.contentStream.showText(entry.getKey());
		this.contentStream.endText();
		decrementHeight();
	}

	private void decrementHeight() {
		this.heightCounter -= Y_DECREMENT;
	}

	private void initCurrentPage() {
		this.currentPage = this.doc.getPage(this.pageIndex++);
		initPageVars();
	}

	private void initNextPage() throws IOException {
		this.contentStream.close();

		initCurrentPage();

		this.contentStream = new PDPageContentStream(this.doc, this.currentPage);
	}

	private void initPageVars() {
		final PDRectangle pageSize = this.currentPage.getMediaBox();
		this.pageStartY = pageSize.getUpperRightY() - MARGIN;
		this.pageEndY = pageSize.getLowerLeftY() + MARGIN;
		this.leftMarginOffset = pageSize.getLowerLeftX() + MARGIN;
		this.heightCounter = this.pageStartY;
	}

	private void initPdfDocument() throws IOException {
		final URL resource = this.getClass().getResource("/Claim.pdf");
		this.doc = PDDocument.load(resource.openStream());
		this.boldFont = PDType0Font.load(this.doc, getArialBoldFontStream());
		this.font = PDType0Font.load(this.doc, getArialFontStream());
	}

	private boolean nextPage() {
		return this.heightCounter < this.pageEndY;
	}

	private boolean nextPageQ() {
		return this.heightCounter - Y_DECREMENT < this.pageEndY;
	}

	private PDDocument populateDocument() throws IOException {
		initCurrentPage();
		try {
			this.contentStream = new PDPageContentStream(this.doc, this.currentPage);
			processJsonObject(this.json);
		} finally {
			this.contentStream.close();
		}

		final int noOfPages = this.doc.getNumberOfPages();
		for (int indexToRemove = noOfPages - 1; indexToRemove >= this.pageIndex; indexToRemove--) {
			this.doc.removePage(indexToRemove);
		}

		return this.doc;
	}

	private void processJsonArray(final ArrayNode arrayNode) throws IOException {

		final Iterator<JsonNode> elements = arrayNode.elements();
		while (elements.hasNext()) {
			final JsonNode element = elements.next();
			if (element.isTextual()) {
				addFormAnswer(element);
			} else if (element.isObject()) {
				processJsonObject(element);
			} else {
				LOGGER.error("Bad node type found for json {}", json.toString());
				throw new IllegalArgumentException(
						String.format("Node type: %s for node %s is not currently supported when processing JSON Arrays",
								element.getNodeType().name()));
			}
		}
	}

	private void processJsonObject(final JsonNode json) throws IOException {

		final Iterator<Entry<String, JsonNode>> entries = json.fields();
		while (entries.hasNext()) {

			final Entry<String, JsonNode> entry = entries.next();
			addFormQuestion(entry);

			final JsonNode valueNode = entry.getValue();
			if (valueNode.isTextual()) {
				addFormAnswer(valueNode);
			} else if (valueNode.isObject()) {
				processJsonObject(valueNode);
			} else if (valueNode.isArray()) {
				processJsonArray((ArrayNode) valueNode);
			} else {
				LOGGER.error("Bad node type found for json {}", json.toString());
				throw new IllegalArgumentException(
						String.format("Node type: %s for node %s is not currently supported when processing JSON objects",
								valueNode.getNodeType().name(), entry.getKey()));
			}
		}
	}

}
