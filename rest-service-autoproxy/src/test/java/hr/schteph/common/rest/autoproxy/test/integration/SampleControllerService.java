package hr.schteph.common.rest.autoproxy.test.integration;

import hr.schteph.common.rest.autoproxy.MapResponseHeaders;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author scvitanovic
 */
@RequestMapping(value = "/test", consumes = "application/json", produces = "application/json", method = RequestMethod.GET)
public interface SampleControllerService {
	@RequestMapping("/")
	@MapResponseHeaders
	public SimpleClass test1(@RequestParam("param1") String value);
}
