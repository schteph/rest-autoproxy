package hr.schteph.common.rest.autoproxy;

import hr.schteph.common.rest.autoproxy.model.CookieValue;
import hr.schteph.common.rest.autoproxy.model.PathVariable;
import hr.schteph.common.rest.autoproxy.model.RequestHeader;
import hr.schteph.common.rest.autoproxy.model.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author scvitanovic
 */
@Setter
@Slf4j
public class RestAutoproxyFactoryImpl implements RestAutoproxyFactory {
	
	private String						baseUrl;

	private RequestArgumentsMapper		mapper			= new RequestArgumentsMapperDefault();

	private RequestArgumentsValidator	validator		= new RequestArgumentsValidatorDefault();

	private ObjectMapper				objectMapper	= new ObjectMapper();

	private RestOperations				restOperations	= new RestTemplate();


	@SuppressWarnings("unchecked")
	@Override
	public <T> T createFor(Class<T> serviceInterface) {
		check(serviceInterface);
		log.debug("Creating autoproxy for interface: " + serviceInterface);
		Method[] methods = serviceInterface.getMethods();
		log.debug("Found " + methods.length + " methods");
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetClass(serviceInterface);
		proxyFactory.addInterface(serviceInterface);
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			log.debug("Doing method " + method.getName());
			proxyFactory.addAdvice(create(serviceInterface, method));
		}
		proxyFactory.setOptimize(true);
		T createdObject = ((T) proxyFactory.getProxy());
		return createdObject;
	}

	public void check(Class<?> serviceInterface) {
		Assert.notNull(serviceInterface);
		Assert.isTrue(serviceInterface.isInterface(), "The serviceInterface must be a interface");
		Assert.notNull(mapper);
		Assert.notNull(validator);
		Assert.notNull(objectMapper);
		Assert.notNull(restOperations);
		Assert.hasText(baseUrl);
	}
	
	public RestServiceExecutorInterceptor create(@NonNull Class<?> serviceInterface, @NonNull Method m) {
		RestServiceExecutorInterceptor retVal = new RestServiceExecutorInterceptor();
		retVal.setForMethod(m);
		RequestMapping baseRequestMapping = AnnotationUtils.findAnnotation(serviceInterface, RequestMapping.class);
		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(m, RequestMapping.class);
		if (methodRequestMapping == null) {
			throw new IllegalStateException("All methods must be annotated with request mapping");
		}

		String url = constructUrl(baseUrl, baseRequestMapping, methodRequestMapping);
		retVal.setUrl(url);

		MediaType produces = constructProduces(baseRequestMapping, methodRequestMapping);
		MediaType consumes = constructConsumes(baseRequestMapping, methodRequestMapping);
		retVal.setProduces(produces);
		retVal.setConsumes(consumes);

		RequestMethod method = constructMethod(baseRequestMapping, methodRequestMapping);
		retVal.setRequestMethod(HttpMethod.valueOf(method.name()));
		retVal.setMapper(mapper);
		retVal.setValidator(validator);
		retVal.setObjectMapper(objectMapper);
		retVal.setRestOperations(restOperations);
		int mapped = 0;
		Map<Integer, PathVariable> pathVariables = constructPathVariables(m);
		Map<Integer, RequestParam> requestParams = constructRequestParams(m);
		Map<Integer, RequestHeader> requestHeaders = constructRequestHeaders(m);
		Map<Integer, CookieValue> cookieValues = constructCookieValues(m);
		mapped += pathVariables.size();
		mapped += requestParams.size();
		mapped += requestHeaders.size();
		mapped += cookieValues.size();
		retVal.setRequestHeaders(requestHeaders);
		retVal.setRequestParameters(requestParams);
		retVal.setPathVariables(pathVariables);
		retVal.setCookieValues(cookieValues);
		mapped += doWithRequestBody(m, retVal);
		if (mapped < m.getParameterTypes().length) {
			throw new IllegalStateException("Only " + mapped + " parameters mapped out of "
					+ m.getParameterTypes().length + " in method named " + m.getName());
		}
		retVal.setMapResponseHeaders(shouldMapResponseHeaders(serviceInterface, m));
		return retVal;
	}

	public boolean shouldMapResponseHeaders(@NonNull Class<?> serviceInterface2, @NonNull Method m) {
		MapResponseHeaders mapResponseHeaders = AnnotationUtils.findAnnotation(m, MapResponseHeaders.class);
		if (mapResponseHeaders != null) {
			return mapResponseHeaders.value();
		}
		mapResponseHeaders = AnnotationUtils.findAnnotation(serviceInterface2, MapResponseHeaders.class);
		if (mapResponseHeaders != null) {
			return mapResponseHeaders.value();
		}
		return false;
	}

	public int doWithRequestBody(@NonNull Method m, @NonNull RestServiceExecutorInterceptor interceptor) {
		Annotation[][] parameterAnnotations = m.getParameterAnnotations();
		Class<?>[] args = m.getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			RequestBody rb = findAnnotation(parameterAnnotations[i], RequestBody.class);
			if (rb != null) {
				interceptor.setRequestBodyArgument(i);
				interceptor.setRequestBodyRequired(rb.required());
				return 1;
			}
		}
		interceptor.setRequestBodyArgument(-1);
		interceptor.setRequestBodyRequired(false);
		return 0;
	}
	
	public Map<Integer, CookieValue> constructCookieValues(@NonNull Method m) {
		Map<Integer, CookieValue> retVal = new HashMap<>();
		Annotation[][] parameterAnnotations = m.getParameterAnnotations();
		Class<?>[] args = m.getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			org.springframework.web.bind.annotation.CookieValue pv = findAnnotation(parameterAnnotations[i],
					org.springframework.web.bind.annotation.CookieValue.class);
			if (pv == null) {
				continue;
			}
			retVal.put(i, new CookieValue(pv));
		}
		return retVal;
	}

	public Map<Integer, RequestHeader> constructRequestHeaders(@NonNull Method m) {
		Map<Integer, RequestHeader> retVal = new HashMap<>();
		Annotation[][] parameterAnnotations = m.getParameterAnnotations();
		Class<?>[] args = m.getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			org.springframework.web.bind.annotation.RequestHeader pv = findAnnotation(parameterAnnotations[i],
					org.springframework.web.bind.annotation.RequestHeader.class);
			if (pv == null) {
				continue;
			}
			retVal.put(i, new RequestHeader(pv));
		}
		return retVal;
	}

	public Map<Integer, RequestParam> constructRequestParams(@NonNull Method m) {
		Map<Integer, RequestParam> retVal = new HashMap<>();
		Annotation[][] parameterAnnotations = m.getParameterAnnotations();
		Class<?>[] args = m.getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			org.springframework.web.bind.annotation.RequestParam pv = findAnnotation(parameterAnnotations[i],
					org.springframework.web.bind.annotation.RequestParam.class);
			if (pv == null) {
				continue;
			}
			retVal.put(i, new RequestParam(pv));
		}
		return retVal;
	}

	public Map<Integer, PathVariable> constructPathVariables(@NonNull Method m) {
		Map<Integer, PathVariable> retVal = new HashMap<>();
		Annotation[][] parameterAnnotations = m.getParameterAnnotations();
		Class<?>[] args = m.getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			org.springframework.web.bind.annotation.PathVariable pv = findAnnotation(parameterAnnotations[i],
					org.springframework.web.bind.annotation.PathVariable.class);
			if (pv == null) {
				continue;
			}
			retVal.put(i, new PathVariable(pv));
		}
		return retVal;
	}

	public RequestMethod constructMethod(RequestMapping baseRequestMapping, @NonNull RequestMapping methodRequestMapping) {
		if (methodRequestMapping.method() != null && methodRequestMapping.method().length > 0) {
			return methodRequestMapping.method()[0];
		}
		if (baseRequestMapping == null) {
			return RequestMethod.GET;
		}
		if (baseRequestMapping.method() != null && baseRequestMapping.method().length > 0) {
			return baseRequestMapping.method()[0];
		}
		return RequestMethod.GET;
	}

	public MediaType constructConsumes(RequestMapping baseRequestMapping, @NonNull RequestMapping methodRequestMapping) {
		String produces = getFirstStringOrNull(methodRequestMapping.consumes());
		if (produces != null) {
			return MediaType.valueOf(produces);
		}
		if (baseRequestMapping != null) {
			produces = getFirstStringOrNull(baseRequestMapping.consumes());
			if (produces != null) {
				return MediaType.valueOf(produces);
			}
		}
		return MediaType.APPLICATION_JSON;
	}

	public MediaType constructProduces(RequestMapping baseRequestMapping, @NonNull RequestMapping methodRequestMapping) {
		String produces = getFirstStringOrNull(methodRequestMapping.produces());
		if (produces != null) {
			return MediaType.valueOf(produces);
		}
		if (baseRequestMapping != null) {
			produces = getFirstStringOrNull(baseRequestMapping.produces());
			if (produces != null) {
				return MediaType.valueOf(produces);
			}
		}
		return MediaType.APPLICATION_JSON;
	}

	public String getFirstStringOrNull(String[] strings) {
		if (strings == null) {
			return null;
		}
		if (strings.length > 0) {
			return strings[0];
		}
		return null;
	}

	public String constructUrl(@NonNull String baseUrl, RequestMapping baseRequestMapping, @NonNull RequestMapping methodRequestMapping) {
		if (methodRequestMapping.value() == null || methodRequestMapping.value().length == 0) {
			throw new IllegalStateException("All methods must be annotated with RequestMapping and have their value declared");
		}
		String url = baseUrl;
		if (baseRequestMapping != null && baseRequestMapping.value() != null && baseRequestMapping.value().length > 0) {
			url += baseRequestMapping.value()[0];
		}
		url += methodRequestMapping.value()[0];
		return url;
	}
	
	@SuppressWarnings("unchecked")
	public <R> R findAnnotation(@NonNull Annotation[] annotations, @NonNull Class<R> annotationType) {
		for (int i = 0; i < annotations.length; i++) {
			Annotation annotation = annotations[i];
			if (annotation.annotationType().equals(annotationType)) {
				return (R) annotation;
			}
		}
		return null;
	}

}
