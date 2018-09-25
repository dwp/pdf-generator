package uk.gov.dwp.gysp.pdf;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.AbstractEnvironment;

public class ApplicationRunner {

	public static void main(final String[] args) {
        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "test");
		SpringApplication.run(Application.class, args);
	}

}
