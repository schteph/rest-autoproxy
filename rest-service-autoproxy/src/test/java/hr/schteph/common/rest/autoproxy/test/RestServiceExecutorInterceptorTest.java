package hr.schteph.common.rest.autoproxy.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hr.schteph.common.rest.autoproxy.RequestArgumentsMapper;
import hr.schteph.common.rest.autoproxy.RequestArgumentsMapperDefault;
import hr.schteph.common.rest.autoproxy.RestServiceExecutorInterceptor;
import hr.schteph.common.rest.autoproxy.model.PathVariable;
import hr.schteph.common.rest.autoproxy.model.RequestHeader;
import hr.schteph.common.rest.autoproxy.model.RequestParam;
import hr.schteph.common.rest.autoproxy.test.interfaces.TestService;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.experimental.ExtensionMethod;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author scvitanovic
 */
@ExtensionMethod({Assert.class})
public class RestServiceExecutorInterceptorTest {
	public RestServiceExecutorInterceptor	underTest;

	@Mock
	private RestOperations					restOperations;

	@Mock
	private ObjectMapper					objectMapper;

	@Mock
	private RequestArgumentsMapper			mapper;

	private Method							voidMethod;
	
	private Method							nonVoidMethod;

	@Before
	public void before() throws NoSuchMethodException, SecurityException {
		MockitoAnnotations.initMocks(this);
		underTest = new RestServiceExecutorInterceptor();
		underTest.setRestOperations(restOperations);
		underTest.setObjectMapper(objectMapper);
		underTest.setMapper(mapper);
		voidMethod = TestService.class.getMethod("voidMethod", Object.class, Object.class, Object.class, Object.class);
		nonVoidMethod = TestService.class.getMethod("nonVoidMethod", Object.class, Object.class, Object.class, Object.class);
		
		underTest.setUrl("http://www.example.com/{path}");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	public void testInvoke() throws Throwable {
		MethodInvocation invocation = Mockito.mock(MethodInvocation.class);
		final ResponseEntity<Object> re = Mockito.mock(ResponseEntity.class);
		
		when(invocation.proceed()).thenThrow(RuntimeException.class);

		Map<String, String> responseBody = new HashMap<>();

		Object expectedResult;

		Object requestBody = new Object();
		Object pathVariable = new Object();
		Object requestParam = new Object();
		Object requestHeader = new Object();
		Object[] args = new Object[] {requestBody, pathVariable, requestParam, requestHeader};

		Map<Integer, RequestParam> requestParams = new HashMap<>();
		requestParams.put(2, new RequestParam("param", true, null));
		Map<Integer, RequestHeader> requestHeaders = new HashMap<>();
		requestHeaders.put(3, new RequestHeader("header", true, null));
		Map<Integer, PathVariable> pathVariables = new HashMap<>();
		pathVariables.put(1, new PathVariable("path"));
		final HttpHeaders headers = new HttpHeaders();
		headers.set("header", requestHeader.toString());
		headers.setAccept(Arrays.asList(underTest.getProduces()));
		headers.setContentType(underTest.getConsumes());

		underTest.setPathVariables(pathVariables);
		underTest.setRequestHeaders(requestHeaders);
		underTest.setRequestParameters(requestParams);
		underTest.setRequestBodyArgument(0);
		underTest.setRequestBodyRequired(true);
		underTest.setMapper(new RequestArgumentsMapperDefault());

		expectedResult = null;
		Class returnType = void.class;
		Class responseType = returnType;
		URI calledUri = new URI("http://www.example.com/"+ pathVariable.toString() + "?param="+requestParam.toString());
		HttpEntity<Object> he = new HttpEntity<Object>(requestBody, headers);
		underTest.setForMethod(voidMethod);
		when(invocation.getMethod()).thenReturn(voidMethod);
		when(invocation.getArguments()).thenReturn(args);
		when(restOperations.exchange(eq(calledUri), eq(HttpMethod.GET), eq(he), eq(responseType))).thenReturn(re);
		when(re.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
		Object result = underTest.invoke(invocation);
		expectedResult.assertEquals(result);
		
		expectedResult = new Object();
		returnType = Object.class;
		responseType = Map.class;
		underTest.setForMethod(nonVoidMethod);
		when(invocation.getMethod()).thenReturn(nonVoidMethod);
		when(invocation.getArguments()).thenReturn(args);
		when(restOperations.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(responseType)))
				.thenReturn(re);
		when(re.getStatusCode()).thenReturn(HttpStatus.OK);
		when(re.getBody()).thenReturn(responseBody);
		when(re.getHeaders()).thenReturn(new HttpHeaders());
		when(objectMapper.convertValue(responseBody, Object.class)).thenReturn(expectedResult);
		result = underTest.invoke(invocation);
		expectedResult.assertEquals(result);
	}

	@Test
	public void testMapResult() {
		Class<Object> returnType = Object.class;
		Object expectedResult = new Object();
		@SuppressWarnings("unchecked")
		ResponseEntity<Object> re = Mockito.mock(ResponseEntity.class);

		Map<String, Object> body = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		headers.set("header1", "value1");
		headers.set("header2", "value2");
		headers.add("header2", "value2_2");

		when(re.getBody()).thenReturn(body);
		when(re.getHeaders()).thenReturn(headers);
		when(objectMapper.convertValue(body, returnType)).thenReturn(expectedResult);

		Object result = underTest.mapResult(objectMapper, re, returnType, true);
		expectedResult.assertEquals(result);
		(body.size() == 2).assertTrue();
		body.get("header1").assertEquals("value1");
		body.get("header2").assertEquals("value2, value2_2");
		
		body = new HashMap<>();

		when(re.getBody()).thenReturn(body);
		when(re.getHeaders()).thenReturn(headers);
		when(objectMapper.convertValue(body, returnType)).thenReturn(expectedResult);

		result = underTest.mapResult(objectMapper, re, returnType, false);
		expectedResult.assertEquals(result);
		(body.size() == 0).assertTrue();
	}

	@Test
	public void testAssertResponseOk() throws URISyntaxException {
		URI uri = new URI("http://www.example.com");
		boolean isVoid;
		@SuppressWarnings("unchecked")
		ResponseEntity<Object> re = Mockito.mock(ResponseEntity.class);

		when(re.getStatusCode()).thenReturn(HttpStatus.OK);
		isVoid = false;
		underTest.assertResponseOk(re, isVoid, uri);

		when(re.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
		isVoid = true;
		underTest.assertResponseOk(re, isVoid, uri);

		RuntimeException e;

		e = null;
		isVoid = true;
		when(re.getStatusCode()).thenReturn(HttpStatus.OK);
		try {
			underTest.assertResponseOk(re, isVoid, uri);
		} catch (RuntimeException ex) {
			e = ex;
		}
		e.assertNotNull();

		e = null;
		isVoid = false;
		when(re.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
		try {
			underTest.assertResponseOk(re, isVoid, uri);
		} catch (RuntimeException ex) {
			e = ex;
		}
		e.assertNotNull();

		e = null;
		isVoid = false;
		when(re.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
		try {
			underTest.assertResponseOk(re, isVoid, uri);
		} catch (RuntimeException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testBuildUri() throws Exception {
		String startUrl = "http://www.example.com/{nekaj}";
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("param", "value");
		Map<String, Object> pathVariables = new HashMap<>();
		pathVariables.put("nekaj", "nekajDrugo");
		String expectedUrlString = "http://www.example.com/nekajDrugo?param=value";
		URI expectedUri = new URI(expectedUrlString);
		URI result = underTest.buildUri(startUrl, requestParams, pathVariables);
		expectedUri.assertEquals(result);
	}

	@Test
	public void testExtractRequestParams() {
		boolean requestBodyRequired = true;

		Map<String, Object> requestParamValues = new HashMap<>();
		Map<String, Object> pathVariableValues = new HashMap<>();

		Map<Integer, PathVariable> pathVariables = new HashMap<>();
		Map<Integer, RequestHeader> requestHeaders = new HashMap<>();
		Map<Integer, RequestParam> requestParams = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		Object requestBody = new Object();
		Object requestParam = new Object();
		Object pathVariable = new Object();
		Object requestHeader = new Object();
		Object expectedResult = new Object();
		Object[] args = new Object[] {requestBody, requestHeader, requestParam, pathVariable};

		when(mapper.extractRequestBody(requestBody, requestBodyRequired)).thenReturn(expectedResult);
		when(mapper.fillRequestHeader(1, requestHeader, headers, requestHeaders)).thenReturn(true);
		when(mapper.fillRequestParam(2, requestParam, requestParamValues, requestParams)).thenReturn(true);
		when(mapper.fillPathVariable(3, pathVariable, pathVariableValues, pathVariables)).thenReturn(true);

		Object result = underTest.extractRequestParams(requestParamValues, pathVariableValues, headers, args, 0,
				requestBodyRequired, requestParams, pathVariables, requestHeaders);
		expectedResult.assertSame(result);

		verify(mapper).fillRequestHeader(1, requestHeader, headers, requestHeaders);
		verify(mapper).fillRequestHeader(2, requestParam, headers, requestHeaders);
		verify(mapper).fillRequestHeader(3, pathVariable, headers, requestHeaders);
		verify(mapper).extractRequestBody(requestBody, requestBodyRequired);
		reset(mapper);

		requestBody = null;
		args[0] = requestBody;
		requestBodyRequired = false;
		when(mapper.extractRequestBody(requestBody, requestBodyRequired)).thenReturn(expectedResult);
		when(mapper.fillRequestHeader(1, requestHeader, headers, requestHeaders)).thenReturn(true);
		when(mapper.fillRequestParam(2, requestParam, requestParamValues, requestParams)).thenReturn(true);
		when(mapper.fillPathVariable(3, pathVariable, pathVariableValues, pathVariables)).thenReturn(true);

		result = underTest.extractRequestParams(requestParamValues, pathVariableValues, headers, args, 0,
				requestBodyRequired, requestParams, pathVariables, requestHeaders);
		expectedResult.assertSame(result);

		verify(mapper).fillRequestHeader(1, requestHeader, headers, requestHeaders);
		verify(mapper).fillRequestHeader(2, requestParam, headers, requestHeaders);
		verify(mapper).fillRequestHeader(3, pathVariable, headers, requestHeaders);
		verify(mapper).extractRequestBody(requestBody, requestBodyRequired);
		reset(mapper);

	}

	@Test
	public void testExtractRequestParamsInvalidRequestBody() {
		boolean requestBodyRequired = true;

		Map<String, Object> requestParamValues = new HashMap<>();
		Map<String, Object> pathVariableValues = new HashMap<>();

		Map<Integer, PathVariable> pathVariables = new HashMap<>();
		Map<Integer, RequestHeader> requestHeaders = new HashMap<>();
		Map<Integer, RequestParam> requestParams = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		Object requestParam = new Object();
		Object pathVariable = new Object();
		Object requestHeader = new Object();
		Object[] args = new Object[] {requestHeader, requestParam, pathVariable};

		when(mapper.fillRequestHeader(1, requestHeader, headers, requestHeaders)).thenReturn(true);
		when(mapper.fillRequestParam(2, requestParam, requestParamValues, requestParams)).thenReturn(true);
		when(mapper.fillPathVariable(3, pathVariable, pathVariableValues, pathVariables)).thenReturn(true);

		IllegalArgumentException e;

		e = null;
		try {
			underTest.extractRequestParams(requestParamValues, pathVariableValues, headers, args, 3,
					requestBodyRequired, requestParams, pathVariables, requestHeaders);
		} catch (IllegalArgumentException ex) {
			e = ex;
		}
		e.assertNotNull();
	}
}
