package hr.schteph.common.rest.autoproxy.integration;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author scvitanovic
 */
@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleClass {

	private String property1;
	
	private String property2;
	
	@JsonProperty("Server")
	private String server;
	
	public SimpleClass() {
		super();
	}

	public SimpleClass(String property1, String property2) {
		super();
		this.property1 = property1;
		this.property2 = property2;
	}
}
