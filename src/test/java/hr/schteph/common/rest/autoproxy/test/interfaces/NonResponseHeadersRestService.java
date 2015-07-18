package hr.schteph.common.rest.autoproxy.test.interfaces;

import hr.schteph.common.rest.autoproxy.MapResponseHeaders;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public interface NonResponseHeadersRestService {

	@RequestMapping(value = "/testMethod", consumes = "application/xml", produces = "application/xml", method = RequestMethod.PUT)
	@MapResponseHeaders(true)
	public Object testMethod(@RequestBody(required = true) Object body,
			@PathVariable("path") String path,
			@RequestParam(value = "param", required = false, defaultValue = "defaultParam") String param,
			@RequestHeader(value = "header", required = false, defaultValue = "defaultHeader") String header);
	
	@RequestMapping(value = "/testMethod", consumes = "application/xml", produces = "application/xml", method = RequestMethod.PUT)
	public Object nonBodyTestMethod(@PathVariable("path") String path,
			@RequestParam(value = "param", required = false, defaultValue = "defaultParam") String param,
			@RequestHeader(value = "header", required = false, defaultValue = "defaultHeader") String header);
}
