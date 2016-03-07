package hr.schteph.common.rest.autoproxy;

import hr.schteph.common.rest.autoproxy.model.CookieValue;
import hr.schteph.common.rest.autoproxy.model.PathVariable;
import hr.schteph.common.rest.autoproxy.model.RequestHeader;
import hr.schteph.common.rest.autoproxy.model.RequestParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;

/**
 * Each method argument must be tagged with either {@link RequestParam}, {@link PathVariable} or {@link RequestBody}. If
 * the method returns an object the return code must be 200. If the method returns void, the return code must be 204.
 *
 * @author scvitanovic
 */
@Setter
@Getter
@Slf4j
public class RestServiceExecutorInterceptor implements MethodInterceptor {

    /**
     * The type this method consumes. Currently supports only application/json. If none is specified on the method,
     * assumes application/json.
     */
    private MediaType consumes = MediaType.APPLICATION_JSON;

    /**
     * The type this method produces. Currently supports only application/json. If none is specified on the method,
     * assumes application/json.
     */
    private MediaType produces = MediaType.APPLICATION_JSON;

    /**
     * The method of the request. Defaults to {@link RequestMethod#GET GET}.
     */
    private HttpMethod requestMethod = HttpMethod.GET;

    /**
     * Request parameters of the REST service call. Will be validated (i.e. if a request parameter is required and none
     * is specified, an {@link IllegalArgumentException} will be thrown). The key is the argument position in the method
     * call.
     */
    private Map<Integer, RequestParam> requestParameters;

    /**
     * Path variables of the request. Wiil be validated (must not be <code>null</code>). The value parameter must be
     * populated.
     */
    private Map<Integer, PathVariable> pathVariables;

    /**
     * Request headers of the REST service call. Will be validated.
     */
    private Map<Integer, RequestHeader> requestHeaders;

    private Map<Integer, CookieValue> cookieValues;

    /**
     * The url to call.
     */
    private String url;

    private RestOperations restOperations;

    private ObjectMapper objectMapper;

    private RequestArgumentsMapper mapper = new RequestArgumentsMapperDefault();

    private RequestArgumentsValidator validator = new RequestArgumentsValidatorDefault();

    /**
     * Which argument (by number) to pass on as request body. Will be serialized according to {@link #getConsumes()
     * consumes} method declaration.
     */
    private int requestBodyArgument = -1;

    private boolean requestBodyRequired = true;

    private Method forMethod;

    private boolean mapResponseHeaders;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method m = invocation.getMethod();
        if (!m.equals(forMethod)) {
            return invocation.proceed();
        }
        log.debug("Calling method " + m.getName() + " on url " + url);
        Object[] args = invocation.getArguments();
        Map<String, Object> requestParams = new HashMap<>();
        Map<String, Object> pathVariables = new HashMap<>();
        Map<String, String> cookies = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(produces));
        headers.setContentType(consumes);
        Object requestBody =
                        extractRequestParams(requestParams, pathVariables, cookies, headers, args, requestBodyArgument,
                                        requestBodyRequired, requestParameters, this.pathVariables, requestHeaders,
                                        cookieValues);
        String url = this.url;

        URI uri = buildUri(url, requestParams, pathVariables);

        Set<String> cookieNames = cookies.keySet();
        for (String cookieName : cookieNames) {
            String cookieValue = cookies.get(cookieName);
            if (cookieValue == null) {
                continue;
            }
            headers.add(HttpHeaders.COOKIE, cookieName + "=" + cookieValue);
        }
        HttpEntity<Object> he = new HttpEntity<Object>(requestBody, headers);

        Class<?> returnType = m.getReturnType();
        boolean isVoid = void.class.equals(returnType);

        ResponseEntity<?> response = restOperations.exchange(uri, requestMethod, he, Map.class);

        assertResponseOk(response, isVoid, uri);

        if (isVoid) {
            return null;
        }

        Object retVal = mapResult(objectMapper, response, returnType, mapResponseHeaders, m.getGenericReturnType());

        return retVal;
    }

    public Object mapResult(ObjectMapper objectMapper, ResponseEntity<?> response, Class<?> returnType,
                    boolean mapResponseHeaders, Type genericReturnType) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> result = (Map<Object, Object>) response.getBody();
        if (mapResponseHeaders) {
            HttpHeaders responseHeaders = response.getHeaders();
            Set<String> responseHeaderKeys = responseHeaders.keySet();
            for (String headerName : responseHeaderKeys) {
                List<String> headerValue = responseHeaders.get(headerName);
                StringBuilder sb = new StringBuilder();
                for (Iterator<String> iter = headerValue.iterator(); iter.hasNext();) {
                    String string = iter.next();
                    sb.append(string);
                    if (iter.hasNext()) {
                        sb.append(", ");
                    }
                }
                result.put(headerName, sb.toString());
            }
        }
        Class<?> realReturnType = convertToRealReturnType(returnType);
        Object retVal = objectMapper.convertValue(result, realReturnType);
        Object realRetVal = convertToRealReturnValue(returnType, realReturnType, retVal, genericReturnType, objectMapper);
        return realRetVal;
    }

    protected Object convertToRealReturnValue(@NonNull Class<?> returnType, @NonNull Class<?> realReturnType, Object retVal, @NonNull Type genericReturnType, ObjectMapper objectMapper) {
        if (returnType == realReturnType) {
            return retVal;
        }
        Object realRetVal = retVal;
        if (("org.springframework.data.domain.Page".equals(returnType.getName()) || "org.springframework.data.domain.PageImpl"
                        .equals(returnType.getName())) &&
                        "hr.schteph.common.rest.autoproxy.model.PageStub".equals(realReturnType.getName())) {
            Class<?> parameterType = null;
            if (genericReturnType instanceof ParameterizedType) {
                ParameterizedType pt =  (ParameterizedType) genericReturnType;
                Type[] arguments = pt.getActualTypeArguments();
                if (arguments != null && arguments.length > 0) {
                    parameterType = (Class<?>) pt.getActualTypeArguments()[0];
                }
            }
            Method m;
            if (parameterType == null) {
                m = BeanUtils.findMethod(realReturnType, "toPage");
            } else {
                m = BeanUtils.findMethod(realReturnType, "toPage", JavaType.class, ObjectMapper.class);
            }
            try {
                if (parameterType == null) {
                    realRetVal = m.invoke(retVal);
                } else {
                    JavaType jt = SimpleType.construct(parameterType);
                    realRetVal = m.invoke(retVal, jt, objectMapper);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                log.error("Exception while converting from PageStub to page", e);
                // TODO: bolji exception
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return realRetVal;
    }

    protected Class<?> convertToRealReturnType(Class<?> returnType) {
        // TODO: generalize this, allow override, allow user edit
        if ("org.springframework.data.domain.Page".equals(returnType.getName()) ||
                        "org.springframework.data.domain.PageImpl".equals(returnType.getName())) {
            return ClassUtils.resolveClassName("hr.schteph.common.rest.autoproxy.model.PageStub", Thread
                            .currentThread().getContextClassLoader());
        }
        return returnType;
    }

    public void assertResponseOk(ResponseEntity<?> response, boolean isVoid, URI uri) {
        HttpStatus status = response.getStatusCode();
        if ((isVoid && (status != HttpStatus.NO_CONTENT && status != HttpStatus.CREATED)) ||
                        (!isVoid && status != HttpStatus.OK)) {
            String msg = String.format("Error while calling rest service on url %s, status code: %s", uri, status);
            log.error(msg);
            // TODO: bolji exception
            throw new RuntimeException(msg);
        }
    }

    public URI buildUri(String url, Map<String, Object> requestParams, Map<String, Object> pathVariables) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        Set<String> requestParamKeys = requestParams.keySet();
        for (String key : requestParamKeys) {
            Object value = requestParams.get(key);
            builder.queryParam(key, value);
        }
        URI uri = builder.buildAndExpand(pathVariables).encode().toUri();
        return uri;
    }

    public Object extractRequestParams(Map<String, Object> requestParamValues, Map<String, Object> pathVariableValues,
                    Map<String, String> cookies, HttpHeaders headers, Object[] args, int requestBodyArgument,
                    boolean requestBodyRequired, Map<Integer, RequestParam> requestParams,
                    Map<Integer, PathVariable> pathVariables, Map<Integer, RequestHeader> requestHeaders,
                    Map<Integer, CookieValue> cookieValues) {
        Object retVal = null;
        boolean hasRequestBody = false;
        for (int i = 0; i < args.length; i++) {
            Integer key = i;
            Object object = args[i];
            if (i == requestBodyArgument) {
                retVal = mapper.extractRequestBody(object, requestBodyRequired);
                hasRequestBody = true;
            } else {
                boolean res = mapper.fillRequestHeader(key, object, headers, requestHeaders);
                res = res || mapper.fillRequestParam(key, object, requestParamValues, requestParams);
                res = res || mapper.fillPathVariable(key, object, pathVariableValues, pathVariables);
                res = res || mapper.fillCookies(key, object, cookies, cookieValues);
                Assert.isTrue(res, "Unannotated parameter number: " + i);
            }
        }
        validator.validateRequestBody(hasRequestBody, requestBodyArgument > -1);
        return retVal;
    }
}
