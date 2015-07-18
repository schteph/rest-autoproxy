package hr.schteph.common.rest.autoproxy;

/**
 * @author scvitanovic
 */
public interface RequestArgumentsValidator {
	public void validateArgument(Object arg, boolean required, String type, String name);
	
	public void validateRequestBody(Object body, boolean required);
	
	public void validateRequestBody(boolean hasRequestBody, boolean shouldHaveRequestBody);
}
