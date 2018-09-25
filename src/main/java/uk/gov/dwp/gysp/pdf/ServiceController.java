package uk.gov.dwp.gysp.pdf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.dwp.gysp.pdf.processor.PdfGeneratorProcessor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api/pdfGenerator")
@Api(value = "/pdfGenerator", tags = { "PDF" }, description = "PDF Generation service")
public class ServiceController {

	@Autowired
	private PdfGeneratorProcessor pdfGeneratorProcessor;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error while processing the request")
	public static ResponseEntity<Void> handleControllerException(HttpServletRequest req, Throwable ex) {
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "generatePdf", method = RequestMethod.POST)
	@ApiOperation(value = "Generate PDF bytes", notes = "This endpoint generates a PDF in bytes")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "PDF bytes generated successfully"),
			@ApiResponse(code = 400, message = "Invalid Json format provided"),
			@ApiResponse(code = 500, message = "Error while processing the request") })
	public ResponseEntity<byte[]> generatePdf(@RequestBody final String json) throws PdfGeneratorException {

		final JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(json);
		} catch (final IOException e) {
			LOGGER.error("Error reading json {} ", json, e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		final byte[] bytes = pdfGeneratorProcessor.generatePdfStream(jsonNode);
		return new ResponseEntity<>(bytes, HttpStatus.OK);
	}
}
