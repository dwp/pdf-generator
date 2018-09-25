package uk.gov.dwp.gysp.pdf;

public class PdfGeneratorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3746899054274701926L;

	public PdfGeneratorException(String message) {
		super(message);
	}

	public PdfGeneratorException(String message, Throwable cause) {
		super(message, cause);
	}
}
