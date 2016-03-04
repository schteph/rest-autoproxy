package hr.schteph.common.rest.autoproxy.interfaces;

import hr.schteph.common.rest.autoproxy.MapResponseHeaders;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author scvitanovic
 */
@MapResponseHeaders
@RequestMapping("/test")
public interface TestRestService {

	@RequestMapping(value = "/testMethod", consumes = "application/xml", produces = "application/xhtml+xml", method = RequestMethod.PUT)
	@MapResponseHeaders(false)
	public Object testMethod(@RequestBody(required = false) Object body,
			@PathVariable("path") String path,
			@RequestParam(value = "param", required = false, defaultValue = "defaultParam") String param,
			@RequestHeader(value = "header", required = false, defaultValue = "defaultHeader") String header);
	
	@RequestMapping(value = "/testMethod2", consumes = "application/xml", produces = "application/xml", method = RequestMethod.POST)
	public Object nonBodyTestMethod(@PathVariable("path") String path,
			@RequestParam(value = "param", required = false, defaultValue = "defaultParam") String param,
			@RequestHeader(value = "header", required = false, defaultValue = "defaultHeader") String header);
}
