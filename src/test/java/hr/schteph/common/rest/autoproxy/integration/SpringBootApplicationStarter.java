package hr.schteph.common.rest.autoproxy.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring boot application to start a rest service.
 * 
 * @author scvitanovic
 */
@SpringBootApplication
public class SpringBootApplicationStarter {
	
	private static ConfigurableApplicationContext ctx;

	public static final void start() {
		ctx = SpringApplication.run(SpringBootApplicationStarter.class, new String[0]);
	}
	
	public static final void stop() {
		ctx.close();
	}
}
