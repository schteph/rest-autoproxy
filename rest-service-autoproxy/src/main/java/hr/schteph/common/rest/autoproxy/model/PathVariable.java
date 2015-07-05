package hr.schteph.common.rest.autoproxy.model;


import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author scvitanovic
 */
@Data
@RequiredArgsConstructor
public class PathVariable {

	private final String value;
	
	public PathVariable(org.springframework.web.bind.annotation.PathVariable pathVariable) {
		value = pathVariable.value();
	}
}
