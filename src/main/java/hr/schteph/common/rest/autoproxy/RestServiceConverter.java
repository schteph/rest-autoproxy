package hr.schteph.common.rest.autoproxy;

/**
 * Converts a parameter of a service argument, according to the consumes
 * parameter.
 * 
 * @author scvitanovic
 */
public interface RestServiceConverter {
	public Object convert(Object input, String consumes, boolean required);
}
