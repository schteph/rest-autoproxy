package hr.schteph.common.rest.autoproxy.test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import hr.schteph.common.rest.autoproxy.RequestArgumentsMapperDefault;
import hr.schteph.common.rest.autoproxy.RequestArgumentsValidator;
import hr.schteph.common.rest.autoproxy.model.PathVariable;
import hr.schteph.common.rest.autoproxy.model.RequestHeader;
import hr.schteph.common.rest.autoproxy.model.RequestParam;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.ExtensionMethod;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ValueConstants;

/**
 * @author scvitanovic
 */
@ExtensionMethod({Assert.class})
public class RequestArgumentsMapperDefaultTest {
	private RequestArgumentsMapperDefault	underTest;
	
	@Mock
	private RequestArgumentsValidator validator;

	@Before
	public void before() {
		underTest = new RequestArgumentsMapperDefault();
		MockitoAnnotations.initMocks(this);
		underTest.setValidator(validator);
	}
	
	@Test
	public void testFillRequestHeaders() {
		Integer key = 1;
		Object obj = new Object();
		String expected = obj.toString();
		String requestHeaderName = "name";
		String defaultValue = "default-value";
		HttpHeaders headers = new HttpHeaders();
		Map<Integer, RequestHeader> requestHeaders = new HashMap<>();
		
		String result;
		boolean succ;
		
		succ = underTest.fillRequestHeader(key, obj, headers, requestHeaders);
		(!succ).assertTrue();
		
		RequestHeader rh = new RequestHeader(requestHeaderName, true, null);
		requestHeaders.put(key, rh);
		succ = underTest.fillRequestHeader(key, obj, headers, requestHeaders);
		result = headers.getFirst(requestHeaderName);
		succ.assertTrue();
		expected.assertEquals(result);
		verify(validator).validateArgument(obj, rh.isRequired(), "Request header", rh.getValue());
		reset(validator);
		
		obj = null;
		rh = new RequestHeader(requestHeaderName, false, defaultValue);
		requestHeaders.put(key, rh);
		succ = underTest.fillRequestHeader(key, obj, headers, requestHeaders);
		result = headers.getFirst(requestHeaderName);
		succ.assertTrue();
		defaultValue.assertSame(result);
		verify(validator).validateArgument(obj, rh.isRequired(), "Request header", rh.getValue());
		reset(validator);
		
		rh = new RequestHeader(requestHeaderName, false, null);
		requestHeaders.put(key, rh);
		succ = underTest.fillRequestHeader(key, obj, headers, requestHeaders);
		result = headers.getFirst(requestHeaderName);
		succ.assertTrue();
		null.assertSame(result);
		verify(validator).validateArgument(obj, rh.isRequired(), "Request header", rh.getValue());
		reset(validator);
		
		rh = new RequestHeader(requestHeaderName, false, ValueConstants.DEFAULT_NONE);
		requestHeaders.put(key, rh);
		succ = underTest.fillRequestHeader(key, obj, headers, requestHeaders);
		result = headers.getFirst(requestHeaderName);
		succ.assertTrue();
		null.assertSame(result);
		verify(validator).validateArgument(obj, rh.isRequired(), "Request header", rh.getValue());
		reset(validator);
	}
	
	@Test
	public void testFillRequestParam() {
		Integer key = 1;
		Object obj = new Object();
		Object expected = obj;
		String requestParamName = "name";
		String defaultValue = "default-value";
		Map<String, Object> requestParamValues = new HashMap<>();
		Map<Integer, RequestParam> requestParams = new HashMap<>();
		
		Object result;
		boolean succ;
		
		succ = underTest.fillRequestParam(key, obj, requestParamValues, requestParams);
		(!succ).assertTrue();
		
		RequestParam pv = new RequestParam(requestParamName, true, null);
		requestParams.put(key, pv);
		succ = underTest.fillRequestParam(key, obj, requestParamValues, requestParams);
		succ.assertTrue();
		result = requestParamValues.get(requestParamName);
		expected.assertSame(result);
		verify(validator).validateArgument(obj, pv.isRequired(), "Request param", pv.getValue());
		reset(validator);
		
		
		obj = null;
		pv = new RequestParam(requestParamName, false, defaultValue);
		requestParams.put(key, pv);
		succ = underTest.fillRequestParam(key, obj, requestParamValues, requestParams);
		succ.assertTrue();
		result = requestParamValues.get(requestParamName);
		defaultValue.assertSame(result);
		verify(validator).validateArgument(obj, pv.isRequired(), "Request param", pv.getValue());
		reset(validator);
		
		pv = new RequestParam(requestParamName, false, null);
		requestParams.put(key, pv);
		succ = underTest.fillRequestParam(key, obj, requestParamValues, requestParams);
		succ.assertTrue();
		result = requestParamValues.get(requestParamName);
		null.assertSame(result);
		verify(validator).validateArgument(obj, pv.isRequired(), "Request param", pv.getValue());
		reset(validator);
		
		pv = new RequestParam(requestParamName, false, ValueConstants.DEFAULT_NONE);
		requestParams.put(key, pv);
		succ = underTest.fillRequestParam(key, obj, requestParamValues, requestParams);
		succ.assertTrue();
		result = requestParamValues.get(requestParamName);
		null.assertSame(result);
		verify(validator).validateArgument(obj, pv.isRequired(), "Request param", pv.getValue());
		reset(validator);
	}

	@Test
	public void testFillPathVariable() {
		Integer key = 1;
		Object obj = new Object();
		Object expected = obj;
		String pathVariableName = "name";
		Map<String, Object> pathVariableValues = new HashMap<>();
		Map<Integer, PathVariable> pathVariables = new HashMap<>();
		
		Object result;
		
		boolean succ = underTest.fillPathVariable(key, obj, pathVariableValues, pathVariables);
		(!succ).assertTrue();
		
		PathVariable pv = new PathVariable(pathVariableName);
		pathVariables.put(key, pv);
		
		succ = underTest.fillPathVariable(key, obj, pathVariableValues, pathVariables);
		result = pathVariableValues.get(pathVariableName);
		succ.assertTrue();
		expected.assertSame(result);
		verify(validator).validateArgument(obj, true, "Path variable", pv.getValue());
		reset(validator);
	}
	
	@Test
	public void testExtractRequestBody() {
		Object object = new Object();
		Object expected = object;
		
		Object result = underTest.extractRequestBody(object, true);
		expected.assertSame(result);
		verify(validator).validateRequestBody(object, true);
		reset(validator);
		result = underTest.extractRequestBody(object, false);
		expected.assertSame(result);
		verify(validator).validateRequestBody(object, false);
		reset(validator);
		
		result = underTest.extractRequestBody(null, false);
		null.assertSame(result);
		verify(validator).validateRequestBody(null, false);
		reset(validator);
	}
	
	@Test
	public void testConvertArgumentToString() {
		String type = "type";
		String name = "name";
		
		Object arg = new Object();
		String expected = arg.toString();
		
		boolean required = true;
		
		String result = underTest.convertArgumentToString(arg, required, null, type, name);
		expected.assertEquals(result);
		verify(validator).validateArgument(arg, required, type, name);
		reset(validator);
		result = underTest.convertArgumentToString(arg, required, "nekaj", type, name);
		expected.assertEquals(result);
		verify(validator).validateArgument(arg, required, type, name);
		reset(validator);
		
		required = false;
		result = underTest.convertArgumentToString(arg, required, null, type, name);
		expected.assertEquals(result);
		verify(validator).validateArgument(arg, required, type, name);
		reset(validator);
		result = underTest.convertArgumentToString(arg, required, "nekaj", type, name);
		expected.assertEquals(result);
		verify(validator).validateArgument(arg, required, type, name);
		reset(validator);
		
		arg = null;
		result = underTest.convertArgumentToString(arg, required, "nekaj", type, name);
		"nekaj".assertEquals(result);
		verify(validator).validateArgument(arg, required, type, name);
		reset(validator);
		
		result = underTest.convertArgumentToString(arg, required, null, type, name);
		null.assertEquals(result);
		verify(validator).validateArgument(arg, required, type, name);
		reset(validator);
		result = underTest.convertArgumentToString(arg, required, ValueConstants.DEFAULT_NONE, type, name);
		null.assertEquals(result);
		verify(validator).validateArgument(arg, required, type, name);
		reset(validator);
	}

}
