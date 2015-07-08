package hr.schteph.common.rest.autoproxy.model;

/**
 * @author scvitanovic
 */
public class CookieValue extends RequestParam {

	public CookieValue(org.springframework.web.bind.annotation.CookieValue cookieValue) {
		this(cookieValue.value(), cookieValue.required(), cookieValue.defaultValue());
	}

	public CookieValue(String value, boolean required, String defaultValue) {
		super(value, required, defaultValue);
	}
}
