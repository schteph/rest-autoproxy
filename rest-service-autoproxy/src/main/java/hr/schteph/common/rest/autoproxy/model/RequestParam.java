package hr.schteph.common.rest.autoproxy.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author scvitanovic
 */
@Data
@RequiredArgsConstructor
public class RequestParam {
	private final String	value;

	private final boolean	required;

	private final String	defaultValue;
	
	public RequestParam(org.springframework.web.bind.annotation.RequestParam requestParam) {
		value = requestParam.value();
		required = requestParam.required();
		defaultValue = requestParam.defaultValue();
	}
}
