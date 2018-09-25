# SpringBoot

This project is intended to be used by SpringBoot Applications or ran as a standalone application.

To run as a standalone application, simply package the project with maven and run the pdf-generator-2.0.0-exec.jar.
The application by default runs on port 8080, to change simply pass in your own application.yml file. You can test the
app using swagger: localhost:8080/swagger-ui.html

To use as a library you will need to add the annotation below to your configuration class:

`@ComponentScan({"uk.gov.dwp.gysp.pdf.processor"})`

If @ComponentScan is already in use it will need to be expanded, for example:

`@ComponentScan({"uk.gov.dwp.gysp.foo", "uk.gov.dwp.gysp.pdf.processor"})`

# PDF Generator

This class takes json input and outputs a PDF file. 

For example: 
```java

@Autowired
private PdfGeneratorProcessor pdfGeneratorProcessor;
```
```java
final JsonNode json = new ObjectMapper().readTree("{\"First Name\":\"Samba\"}");

final byte[] result = pdfGenerator.generatePdfStream(json);
```

#### Project inclusion

Dependency reference:

    <dependency>
        <groupId>uk.gov.dwp.gysp</groupId>
        <artifactId>pdf-generator</artifactId>
    </dependency>
    
#### Example of use

    import uk.gov.dwp.gysp.pdf.PdfGeneratorException;
    import uk.gov.dwp.gysp.pdf.processor.PdfGeneratorProcessor;

_Declaration_

```java
@Autowired
private PdfGeneratorProcessor pdfGeneratorProcessor;
```

_Use example_

```java
pdfGeneratorResponse = pdfGeneratorProcessor.generatePdfStream(jsonNode);
```

