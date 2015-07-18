package hr.schteph.common.rest.autoproxy.test.integration;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author scvitanovic
 */
@RestController
@RequestMapping(value = "/test", consumes = "application/json", produces = "application/json", method = RequestMethod.GET)
public class SampleController {

	@RequestMapping("/")
	public SimpleClass test1(@RequestParam("param1")String value) {
		SimpleClass retVal = new SimpleClass(value, "test");
		return retVal;
	}
}
