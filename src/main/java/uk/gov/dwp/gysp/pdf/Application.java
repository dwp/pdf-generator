package uk.gov.dwp.gysp.pdf;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

@SpringBootApplication
@EnableSwagger2
@Configuration
public class Application {

    @Autowired
    private TypeResolver typeResolver;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    static UiConfiguration uiConfig() {
        return UiConfiguration.DEFAULT;
    }

    @Bean
    public Docket api() {
        ApiInfo info = new ApiInfo("Pdf-Generator API",
                "The Pdf-Generator API generates a PDF (in bytes) when a json is passed as request", "1.0.0",
                "http://terms-of-service.url", "Samba Mitra <samba.mitra@gmail.com>", "License", "http://licenseurl");

        return new Docket(DocumentationType.SWAGGER_2).apiInfo(info).select().apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/api/.*")).build().pathMapping("/")
                .directModelSubstitute(LocalDate.class, String.class).genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(newRule(
                        this.typeResolver.resolve(DeferredResult.class,
                                this.typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                        this.typeResolver.resolve(WildcardType.class)))
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, newArrayList(new ResponseMessageBuilder().code(500)
                        .message("500 message").responseModel(new ModelRef("Error")).build()));
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
