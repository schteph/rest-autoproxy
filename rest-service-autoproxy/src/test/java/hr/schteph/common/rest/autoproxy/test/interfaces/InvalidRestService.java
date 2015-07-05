package hr.schteph.common.rest.autoproxy.test.interfaces;

import hr.schteph.common.rest.autoproxy.MapResponseHeaders;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author scvitanovic
 */
public interface InvalidRestService {

	/**
	 * Invalid because not all params are annotated..
	 */
	@RequestMapping(value = "/testMethod", consumes = "application/xml", produces = "application/xhtml+xml", method = RequestMethod.PUT)
	@MapResponseHeaders(false)
	public Object testMethod(Object body,
			@PathVariable("path") String path,
			@RequestParam(value = "param", required = false, defaultValue = "defaultParam") String param,
			@RequestHeader(value = "header", required = false, defaultValue = "defaultHeader") String header);
	
	/**
	 * Invalid because method is not annotated with {@link RequestMapping}.
	 */
	public Object nonBodyTestMethod(@PathVariable("path") String path,
			@RequestParam(value = "param", required = false, defaultValue = "defaultParam") String param,
			@RequestHeader(value = "header", required = false, defaultValue = "defaultHeader") String header);
}
