package hr.schteph.common.rest.autoproxy;

import static org.mockito.Mockito.when;
import hr.schteph.common.rest.autoproxy.RequestArgumentsMapper;
import hr.schteph.common.rest.autoproxy.RequestArgumentsValidator;
import hr.schteph.common.rest.autoproxy.RestAutoproxyFactoryImpl;
import hr.schteph.common.rest.autoproxy.RestServiceExecutorInterceptor;
import hr.schteph.common.rest.autoproxy.interfaces.InvalidRestService;
import hr.schteph.common.rest.autoproxy.interfaces.NonResponseHeadersRestService;
import hr.schteph.common.rest.autoproxy.interfaces.TestRestService;
import hr.schteph.common.rest.autoproxy.model.PathVariable;
import hr.schteph.common.rest.autoproxy.model.RequestHeader;
import hr.schteph.common.rest.autoproxy.model.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lombok.experimental.ExtensionMethod;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author scvitanovic
 */
@ExtensionMethod({Assert.class})
public class RestAutoproxyFactoryImplTest {
	private RestAutoproxyFactoryImpl	underTest;

	@Mock
	private RequestArgumentsMapper				mapper;

	@Mock
	private RequestArgumentsValidator			validator;

	@Mock
	private ObjectMapper						objectMapper;

	@Mock
	private RestOperations						restOperations;

	private String								baseUrl	= "http://www.example.com";

	@Before
	public void before() {
		underTest = new RestAutoproxyFactoryImpl();
		MockitoAnnotations.initMocks(this);
		underTest.setMapper(mapper);
		underTest.setValidator(validator);
		underTest.setObjectMapper(objectMapper);
		underTest.setRestOperations(restOperations);
		underTest.setBaseUrl(baseUrl);
	}
	
	@Test
	public void testAfterPropertiesSet() throws Exception {
		RestAutoproxyFactoryImpl underTest = new RestAutoproxyFactoryImpl();
		MockitoAnnotations.initMocks(this);
		underTest.setMapper(mapper);
		underTest.setValidator(validator);
		underTest.setObjectMapper(objectMapper);
		underTest.setRestOperations(restOperations);
		underTest.setBaseUrl(baseUrl);
		Class<?> serviceInterface = TestRestService.class;
		Object proxy = underTest.createFor(serviceInterface);
		proxy.assertNotNull();
		Assert.assertTrue(serviceInterface.isInstance(proxy));
	}
	
	@Test
	public void testCreate() throws NoSuchMethodException, SecurityException {
		Class<?> serviceInterface = TestRestService.class;
		Method m;

		Map<Integer, RequestParam> params = new HashMap<>();
		Map<Integer, RequestHeader> headers = new HashMap<>();
		Map<Integer, PathVariable> paths = new HashMap<>();
		
		params.put(2, new RequestParam("param", false, "defaultParam"));
		headers.put(3, new RequestHeader("header", false, "defaultHeader"));
		paths.put(1, new PathVariable("path"));
		String expectedUrl = baseUrl + "/test" + "/testMethod";

		m = serviceInterface.getDeclaredMethod("testMethod", Object.class, String.class, String.class, String.class);

		RestServiceExecutorInterceptor rsei = underTest.create(serviceInterface, m);
		MediaType.APPLICATION_XML.assertEquals(rsei.getConsumes());
		MediaType.APPLICATION_XHTML_XML.assertEquals(rsei.getProduces());
		RequestMethod.PUT.toString().assertEquals(rsei.getRequestMethod().toString());
		params.assertEquals(rsei.getRequestParameters());
		headers.assertEquals(rsei.getRequestHeaders());
		paths.assertEquals(rsei.getPathVariables());
		expectedUrl.assertEquals(rsei.getUrl());
		restOperations.assertSame(rsei.getRestOperations());
		objectMapper.assertSame(rsei.getObjectMapper());
		mapper.assertSame(rsei.getMapper());
		validator.assertSame(rsei.getValidator());
		Assert.assertEquals(0, rsei.getRequestBodyArgument());
		Assert.assertFalse(rsei.isRequestBodyRequired());
		m.assertSame(rsei.getForMethod());
		Assert.assertFalse(rsei.isMapResponseHeaders());

		Exception e;
		
		serviceInterface = InvalidRestService.class;
		m = serviceInterface.getDeclaredMethod("nonBodyTestMethod", String.class, String.class, String.class);
		e = null;
		try {
			underTest.create(serviceInterface, m);
		} catch (IllegalStateException ex) {
			e = ex;
		}
		e.assertNotNull();
		
		m = serviceInterface.getDeclaredMethod("testMethod", Object.class, String.class, String.class, String.class);
		e = null;
		try {
			underTest.create(serviceInterface, m);
		} catch (IllegalStateException ex) {
			e = ex;
		}
		e.assertNotNull();
	
		e = null;
		try {
			underTest.create(null, m);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
		
		e = null;
		try {
			underTest.create(serviceInterface, null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testShouldMapResponseHeaders() throws NoSuchMethodException, SecurityException {
		Class<?> serviceInterface = TestRestService.class;
		Method m;

		boolean result;

		m = serviceInterface.getDeclaredMethod("testMethod", Object.class, String.class, String.class, String.class);
		result = underTest.shouldMapResponseHeaders(serviceInterface, m);
		Assert.assertFalse(result);

		m = serviceInterface.getDeclaredMethod("nonBodyTestMethod", String.class, String.class, String.class);
		result = underTest.shouldMapResponseHeaders(serviceInterface, m);
		Assert.assertTrue(result);
		
		serviceInterface = NonResponseHeadersRestService.class;
		m = serviceInterface.getDeclaredMethod("testMethod", Object.class, String.class, String.class, String.class);
		result = underTest.shouldMapResponseHeaders(serviceInterface, m);
		Assert.assertTrue(result);

		m = serviceInterface.getDeclaredMethod("nonBodyTestMethod", String.class, String.class, String.class);
		result = underTest.shouldMapResponseHeaders(serviceInterface, m);
		Assert.assertFalse(result);
		
		NullPointerException e;
		e = null;
		try {
			underTest.shouldMapResponseHeaders(null, m);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
		
		e = null;
		try {
			underTest.shouldMapResponseHeaders(serviceInterface, null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testDoWithRequestBody() throws NoSuchMethodException, SecurityException {
		Method m = TestRestService.class.getDeclaredMethod("testMethod", Object.class, String.class, String.class,
				String.class);
		RestServiceExecutorInterceptor rsei = new RestServiceExecutorInterceptor();
		int expected;
		int result;

		expected = 1;
		result = underTest.doWithRequestBody(m, rsei);
		Assert.assertEquals(expected, result);
		Assert.assertFalse(rsei.isRequestBodyRequired());
		Assert.assertEquals(0, rsei.getRequestBodyArgument());

		m = TestRestService.class.getDeclaredMethod("nonBodyTestMethod", String.class, String.class, String.class);
		expected = 0;
		result = underTest.doWithRequestBody(m, rsei);
		Assert.assertEquals(expected, result);
		Assert.assertFalse(rsei.isRequestBodyRequired());
		Assert.assertEquals(-1, rsei.getRequestBodyArgument());

		NullPointerException e;
		e = null;
		try {
			underTest.doWithRequestBody(null, rsei);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
		e = null;
		try {
			underTest.doWithRequestBody(m, null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testConstructRequestHeaders() throws NoSuchMethodException, SecurityException {
		Method m = TestRestService.class.getDeclaredMethod("testMethod", Object.class, String.class, String.class,
				String.class);

		RequestHeader expected = new RequestHeader("header", false, "defaultHeader");
		Integer expectedKey = 3;

		Map<Integer, RequestHeader> result = underTest.constructRequestHeaders(m);
		(result.size() == 1).assertTrue();
		RequestHeader pv = result.get(expectedKey);
		pv.assertNotNull();
		expected.assertEquals(pv);

		NullPointerException e;
		e = null;
		try {
			underTest.constructRequestHeaders(null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testConstructRequestParams() throws NoSuchMethodException, SecurityException {
		Method m = TestRestService.class.getDeclaredMethod("testMethod", Object.class, String.class, String.class,
				String.class);

		RequestParam expected = new RequestParam("param", false, "defaultParam");
		Integer expectedKey = 2;

		Map<Integer, RequestParam> result = underTest.constructRequestParams(m);
		(result.size() == 1).assertTrue();
		RequestParam pv = result.get(expectedKey);
		pv.assertNotNull();
		expected.assertEquals(pv);

		NullPointerException e;
		e = null;
		try {
			underTest.constructRequestParams(null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testConstructPathVariables() throws NoSuchMethodException, SecurityException {
		Method m = TestRestService.class.getDeclaredMethod("testMethod", Object.class, String.class, String.class,
				String.class);

		PathVariable expected = new PathVariable("path");
		Integer expectedKey = 1;

		Map<Integer, PathVariable> result = underTest.constructPathVariables(m);
		(result.size() == 1).assertTrue();
		PathVariable pv = result.get(expectedKey);
		pv.assertNotNull();
		expected.assertEquals(pv);

		NullPointerException e;
		e = null;
		try {
			underTest.constructPathVariables(null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testConstructMethod() {
		RequestMapping brm = Mockito.mock(RequestMapping.class);
		RequestMapping mrm = Mockito.mock(RequestMapping.class);
		RequestMethod expectedMethod;
		RequestMethod result;

		RequestMethod[] brmMethod = new RequestMethod[] {RequestMethod.DELETE};
		RequestMethod[] mrmMethod = new RequestMethod[] {RequestMethod.PUT};

		when(mrm.method()).thenReturn(mrmMethod);
		expectedMethod = RequestMethod.PUT;
		result = underTest.constructMethod(brm, mrm);
		expectedMethod.assertEquals(result);

		mrmMethod = new RequestMethod[0];
		when(brm.method()).thenReturn(brmMethod);
		when(mrm.method()).thenReturn(mrmMethod);
		expectedMethod = RequestMethod.DELETE;
		result = underTest.constructMethod(brm, mrm);
		expectedMethod.assertEquals(result);

		mrmMethod = null;
		when(brm.method()).thenReturn(brmMethod);
		when(mrm.method()).thenReturn(mrmMethod);
		expectedMethod = RequestMethod.DELETE;
		result = underTest.constructMethod(brm, mrm);
		expectedMethod.assertEquals(result);

		mrmMethod = null;
		brmMethod = new RequestMethod[0];
		when(brm.method()).thenReturn(brmMethod);
		when(mrm.method()).thenReturn(mrmMethod);
		expectedMethod = RequestMethod.GET;
		result = underTest.constructMethod(brm, mrm);
		expectedMethod.assertEquals(result);

		mrmMethod = null;
		brmMethod = null;
		when(brm.method()).thenReturn(brmMethod);
		when(mrm.method()).thenReturn(mrmMethod);
		expectedMethod = RequestMethod.GET;
		result = underTest.constructMethod(brm, mrm);
		expectedMethod.assertEquals(result);

		mrmMethod = null;
		brmMethod = null;
		when(mrm.method()).thenReturn(mrmMethod);
		expectedMethod = RequestMethod.GET;
		result = underTest.constructMethod(null, mrm);
		expectedMethod.assertEquals(result);

		NullPointerException e = null;
		try {
			underTest.constructMethod(null, null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testConstructConsumes() {
		RequestMapping brm = Mockito.mock(RequestMapping.class);
		RequestMapping mrm = Mockito.mock(RequestMapping.class);
		MediaType expectedConsumes;
		MediaType result;

		String[] brmConsumes = new String[] {MediaType.APPLICATION_ATOM_XML_VALUE};
		String[] mrmConsumes = new String[] {MediaType.APPLICATION_FORM_URLENCODED_VALUE};

		when(mrm.consumes()).thenReturn(mrmConsumes);
		expectedConsumes = MediaType.APPLICATION_FORM_URLENCODED;
		result = underTest.constructConsumes(brm, mrm);
		expectedConsumes.assertEquals(result);

		mrmConsumes = new String[0];
		when(brm.consumes()).thenReturn(brmConsumes);
		when(mrm.consumes()).thenReturn(mrmConsumes);
		expectedConsumes = MediaType.APPLICATION_ATOM_XML;
		result = underTest.constructConsumes(brm, mrm);
		expectedConsumes.assertEquals(result);

		mrmConsumes = null;
		when(brm.consumes()).thenReturn(brmConsumes);
		when(mrm.consumes()).thenReturn(mrmConsumes);
		expectedConsumes = MediaType.APPLICATION_ATOM_XML;
		result = underTest.constructConsumes(brm, mrm);
		expectedConsumes.assertEquals(result);

		mrmConsumes = null;
		brmConsumes = new String[0];
		when(brm.consumes()).thenReturn(brmConsumes);
		when(mrm.consumes()).thenReturn(mrmConsumes);
		expectedConsumes = MediaType.APPLICATION_JSON;
		result = underTest.constructConsumes(brm, mrm);
		expectedConsumes.assertEquals(result);

		mrmConsumes = null;
		brmConsumes = null;
		when(brm.consumes()).thenReturn(brmConsumes);
		when(mrm.consumes()).thenReturn(mrmConsumes);
		expectedConsumes = MediaType.APPLICATION_JSON;
		result = underTest.constructConsumes(brm, mrm);
		expectedConsumes.assertEquals(result);

		mrmConsumes = null;
		brmConsumes = null;
		when(mrm.consumes()).thenReturn(mrmConsumes);
		expectedConsumes = MediaType.APPLICATION_JSON;
		result = underTest.constructConsumes(null, mrm);
		expectedConsumes.assertEquals(result);

		NullPointerException e = null;
		try {
			underTest.constructConsumes(null, null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testConstructProduces() {
		RequestMapping brm = Mockito.mock(RequestMapping.class);
		RequestMapping mrm = Mockito.mock(RequestMapping.class);
		MediaType expectedProduces;
		MediaType result;

		String[] brmProduces = new String[] {MediaType.APPLICATION_ATOM_XML_VALUE};
		String[] mrmProduces = new String[] {MediaType.APPLICATION_FORM_URLENCODED_VALUE};

		when(mrm.produces()).thenReturn(mrmProduces);
		expectedProduces = MediaType.APPLICATION_FORM_URLENCODED;
		result = underTest.constructProduces(brm, mrm);
		expectedProduces.assertEquals(result);

		mrmProduces = new String[0];
		when(brm.produces()).thenReturn(brmProduces);
		when(mrm.produces()).thenReturn(mrmProduces);
		expectedProduces = MediaType.APPLICATION_ATOM_XML;
		result = underTest.constructProduces(brm, mrm);
		expectedProduces.assertEquals(result);

		mrmProduces = null;
		when(brm.produces()).thenReturn(brmProduces);
		when(mrm.produces()).thenReturn(mrmProduces);
		expectedProduces = MediaType.APPLICATION_ATOM_XML;
		result = underTest.constructProduces(brm, mrm);
		expectedProduces.assertEquals(result);

		mrmProduces = null;
		brmProduces = new String[0];
		when(brm.produces()).thenReturn(brmProduces);
		when(mrm.produces()).thenReturn(mrmProduces);
		expectedProduces = MediaType.APPLICATION_JSON;
		result = underTest.constructProduces(brm, mrm);
		expectedProduces.assertEquals(result);

		mrmProduces = null;
		brmProduces = null;
		when(brm.produces()).thenReturn(brmProduces);
		when(mrm.produces()).thenReturn(mrmProduces);
		expectedProduces = MediaType.APPLICATION_JSON;
		result = underTest.constructProduces(brm, mrm);
		expectedProduces.assertEquals(result);

		mrmProduces = null;
		brmProduces = null;
		when(mrm.produces()).thenReturn(mrmProduces);
		expectedProduces = MediaType.APPLICATION_JSON;
		result = underTest.constructProduces(null, mrm);
		expectedProduces.assertEquals(result);

		NullPointerException e = null;
		try {
			underTest.constructProduces(null, null);
		} catch (NullPointerException ex) {
			e = ex;
		}
		e.assertNotNull();
	}

	@Test
	public void testConstructUrl() {
		RequestMapping brm = Mockito.mock(RequestMapping.class);
		RequestMapping mrm = Mockito.mock(RequestMapping.class);

		String[] brmUrl = new String[] {"/brmUrl"};
		String[] mrmUrl = new String[] {"/mrmUrl"};

		String expectedUrl = baseUrl + brmUrl[0] + mrmUrl[0];
		when(brm.value()).thenReturn(brmUrl);
		when(mrm.value()).thenReturn(mrmUrl);
		String result = underTest.constructUrl(baseUrl, brm, mrm);
		expectedUrl.assertEquals(result);

		expectedUrl = baseUrl + mrmUrl[0];

		brmUrl = new String[0];
		when(brm.value()).thenReturn(brmUrl);
		when(mrm.value()).thenReturn(mrmUrl);
		result = underTest.constructUrl(baseUrl, brm, mrm);
		expectedUrl.assertEquals(result);

		brmUrl = null;
		when(brm.value()).thenReturn(brmUrl);
		when(mrm.value()).thenReturn(mrmUrl);
		result = underTest.constructUrl(baseUrl, brm, mrm);
		expectedUrl.assertEquals(result);

		brm = null;
		when(mrm.value()).thenReturn(mrmUrl);
		result = underTest.constructUrl(baseUrl, brm, mrm);
		expectedUrl.assertEquals(result);
	}

	@Test
	public void testInvalidConstructUrl() {
		RequestMapping mrm = Mockito.mock(RequestMapping.class);

		IllegalStateException e;

		NullPointerException ne = null;
		try {
			underTest.constructUrl(baseUrl, null, null);
		} catch (NullPointerException ex) {
			ne = ex;
		}
		ne.assertNotNull();

		ne = null;
		try {
			underTest.constructUrl(null, null, null);
		} catch (NullPointerException ex) {
			ne = ex;
		}
		ne.assertNotNull();

		when(mrm.value()).thenReturn(null);
		e = null;
		try {
			underTest.constructUrl(baseUrl, null, mrm);
		} catch (IllegalStateException ex) {
			e = ex;
		}
		e.assertNotNull();

		when(mrm.value()).thenReturn(new String[0]);
		e = null;
		try {
			underTest.constructUrl(baseUrl, null, mrm);
		} catch (IllegalStateException ex) {
			e = ex;
		}
		e.assertNotNull();

	}

	@Test
	public void testGetFirstOrNull() {
		String[] strings;
		String result;
		String expectedResult;

		strings = null;
		expectedResult = null;
		result = underTest.getFirstStringOrNull(strings);
		expectedResult.assertEquals(result);

		strings = new String[0];
		expectedResult = null;
		result = underTest.getFirstStringOrNull(strings);
		expectedResult.assertEquals(result);

		strings = new String[] {"test"};
		expectedResult = "test";
		result = underTest.getFirstStringOrNull(strings);
		expectedResult.assertEquals(result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindAnnotation() {
		RequestMapping rm = Mockito.mock(RequestMapping.class);
		Annotation other = Mockito.mock(Annotation.class);
		Annotation expected = rm;
		Annotation[] anns;
		Annotation result;

		anns = new Annotation[] {other, rm};
		when((Class<Annotation>) other.annotationType()).thenReturn(Annotation.class);
		when((Class<RequestMapping>) rm.annotationType()).thenReturn(RequestMapping.class);
		result = underTest.findAnnotation(anns, RequestMapping.class);
		expected.assertSame(result);

		anns = new Annotation[] {other};
		when((Class<Annotation>) other.annotationType()).thenReturn(Annotation.class);
		when((Class<RequestMapping>) rm.annotationType()).thenReturn(RequestMapping.class);
		result = underTest.findAnnotation(anns, RequestMapping.class);
		result.assertNull();

		NullPointerException ne;

		ne = null;
		try {
			underTest.findAnnotation(null, RequestMapping.class);
		} catch (NullPointerException ex) {
			ne = ex;
		}
		ne.assertNotNull();

		ne = null;
		try {
			underTest.findAnnotation(anns, null);
		} catch (NullPointerException ex) {
			ne = ex;
		}
		ne.assertNotNull();
	}
	
}
