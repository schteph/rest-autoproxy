package hr.schteph.common.rest.autoproxy.test.interfaces;


/**
 * @author scvitanovic
 */
public interface TestService {

	public void voidMethod(Object requestBody, Object pathVariable, Object requestParam, Object requestHeader);

	public Object nonVoidMethod(Object requestBody, Object pathVariable, Object requestParam, Object requestHeader);
}
