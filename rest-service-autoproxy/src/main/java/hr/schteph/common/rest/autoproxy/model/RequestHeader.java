package hr.schteph.common.rest.autoproxy.model;


/**
 * @author scvitanovic
 */
public class RequestHeader extends RequestParam {

	public RequestHeader(String value, boolean required, String defaultValue) {
		super(value, required, defaultValue);
	}
	
	public RequestHeader(org.springframework.web.bind.annotation.RequestHeader requestParam) {
		this(requestParam.value(), requestParam.required(), requestParam.defaultValue());
	}
}
