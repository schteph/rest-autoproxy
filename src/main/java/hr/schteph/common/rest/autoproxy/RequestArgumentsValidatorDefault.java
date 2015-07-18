package hr.schteph.common.rest.autoproxy;

import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * @author scvitanovic
 */
@Slf4j
public class RequestArgumentsValidatorDefault implements RequestArgumentsValidator {

	@Override
	public void validateArgument(Object arg, boolean required, String type, String name) {
		if (arg == null && required) {
			String msg = String.format("{} {} is required but was null", type, name);
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}		
	}

	@Override
	public void validateRequestBody(Object body, boolean required) {
		if (required && body == null) {
			throw new IllegalArgumentException("The request body is required but none is provided");
		}
	}

	@Override
	public void validateRequestBody(boolean hasRequestBody, boolean shouldHaveRequestBody) {
		Assert.isTrue(hasRequestBody || !shouldHaveRequestBody, "The request body is required but none is provided");
	}
}
