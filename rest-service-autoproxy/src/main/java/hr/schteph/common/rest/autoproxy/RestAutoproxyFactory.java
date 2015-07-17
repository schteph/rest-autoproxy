package hr.schteph.common.rest.autoproxy;

/**
 * @author scvitanovic
 */
public interface RestAutoproxyFactory {

	public <T> T createFor(Class<T> serviceInterface);
}
