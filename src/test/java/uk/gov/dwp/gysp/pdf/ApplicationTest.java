package uk.gov.dwp.gysp.pdf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.data.mongodb.port = 0")
@ActiveProfiles("test")
public class ApplicationTest {

	@Test
	public void contextLoads() {
		// Test context loads
	}

}
