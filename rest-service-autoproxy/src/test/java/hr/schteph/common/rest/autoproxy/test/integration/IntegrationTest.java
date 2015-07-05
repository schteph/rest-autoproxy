package hr.schteph.common.rest.autoproxy.test.integration;

import hr.schteph.common.rest.autoproxy.RestAutoproxyFactoryBean;
import lombok.experimental.ExtensionMethod;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@ExtensionMethod({Assert.class})
public class IntegrationTest {
	
	@Before
	public void before() {
		SpringBootApplicationStarter.start();
	}
	
	@After
	public void after() {
		
	}

	@Test
	public void testService() throws Exception {
		RestAutoproxyFactoryBean<SampleControllerService> factory = new RestAutoproxyFactoryBean<>();
		factory.setBaseUrl("http://localhost:8080");
		factory.setServiceInterface(SampleControllerService.class);
		factory.afterPropertiesSet();
		SampleControllerService service = factory.getObject();
		SimpleClass sc = service.test1("testValue");
		SimpleClass expected = new SimpleClass("testValue", "test", "Apache-Coyote/1.1");
		expected.assertEquals(sc);
	}
}
