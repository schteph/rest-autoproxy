package hr.schteph.common.rest.autoproxy;

import lombok.Setter;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates a proxy bean around an annotated interface to call an exposed REST
 * service.
 * 
 * @author scvitanovic
 */
@Setter
public class RestAutoproxyFactoryBean<T> implements InitializingBean, FactoryBean<T> {

	private Class<T>					serviceInterface;

	private RestAutoproxyFactory		restAutoproxyFactory;

	private T							createdObject;

	public T getObject() throws Exception {
		return createdObject;
	}

	public Class<?> getObjectType() {
		return serviceInterface;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		createdObject = restAutoproxyFactory.createFor(serviceInterface);
	}

	
}
