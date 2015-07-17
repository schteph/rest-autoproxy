package hr.schteph.common.rest.autoproxy.test.integration;

import hr.schteph.common.rest.autoproxy.RestAutoproxyFactoryBean;
import hr.schteph.common.rest.autoproxy.RestAutoproxyFactoryImpl;
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
		SpringBootApplicationStarter.stop();
	}

	@Test
	public void testService() throws Exception {
		RestAutoproxyFactoryImpl factory = new RestAutoproxyFactoryImpl();
		factory.setBaseUrl("http://localhost:8080");
		RestAutoproxyFactoryBean<SampleControllerService> factoryBean = new RestAutoproxyFactoryBean<>();
		factoryBean.setRestAutoproxyFactory(factory);
		factoryBean.setServiceInterface(SampleControllerService.class);
		factoryBean.afterPropertiesSet();
		SampleControllerService service = factoryBean.getObject();
		SimpleClass sc = service.test1("testValue");
		SimpleClass expected = new SimpleClass("testValue", "test", "Apache-Coyote/1.1");
		expected.assertEquals(sc);
	}
}
