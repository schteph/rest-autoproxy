package hr.schteph.common.rest.autoproxy;

import static org.mockito.Mockito.when;
import hr.schteph.common.rest.autoproxy.model.PathVariable;
import hr.schteph.common.rest.autoproxy.model.RequestHeader;
import hr.schteph.common.rest.autoproxy.model.RequestParam;
import lombok.experimental.ExtensionMethod;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author scvitanovic
 */
@ExtensionMethod({Assert.class})
public class ModelTest {

	@Test
	public void testPathVariable() {
		org.springframework.web.bind.annotation.PathVariable pv = Mockito
				.mock(org.springframework.web.bind.annotation.PathVariable.class);
		when(pv.value()).thenReturn("name");
		PathVariable pathVariable = new PathVariable(pv);
		pathVariable.getValue().assertEquals("name");

		PathVariable pathVariable2 = new PathVariable("name");
		pathVariable2.getValue().assertEquals("name");
		
		pathVariable2.assertEquals(pathVariable);
		Assert.assertEquals(pathVariable.hashCode(), pathVariable2.hashCode());
	}

	@Test
	public void testRequestParam() {
		String name = "name";
		boolean required = true;
		String defaultValue = "default";

		RequestParam result = new RequestParam("name", true, "default");
		name.assertEquals(result.getValue());
		Assert.assertTrue(result.isRequired());
		defaultValue.assertEquals(result.getDefaultValue());

		org.springframework.web.bind.annotation.RequestParam rp = Mockito
				.mock(org.springframework.web.bind.annotation.RequestParam.class);
		when(rp.value()).thenReturn(name);
		when(rp.required()).thenReturn(required);
		when(rp.defaultValue()).thenReturn(defaultValue);
		
		RequestParam result2 = new RequestParam(rp);
		name.assertEquals(result2.getValue());
		Assert.assertTrue(result2.isRequired());
		defaultValue.assertEquals(result2.getDefaultValue());
		
		result2.assertEquals(result);
		Assert.assertEquals(result.hashCode(), result2.hashCode());
	}
	
	@Test
	public void testRequestHeader() {
		String name = "name";
		boolean required = true;
		String defaultValue = "default";

		RequestHeader result = new RequestHeader("name", true, "default");
		name.assertEquals(result.getValue());
		Assert.assertTrue(result.isRequired());
		defaultValue.assertEquals(result.getDefaultValue());

		org.springframework.web.bind.annotation.RequestHeader rp = Mockito
				.mock(org.springframework.web.bind.annotation.RequestHeader.class);
		when(rp.value()).thenReturn(name);
		when(rp.required()).thenReturn(required);
		when(rp.defaultValue()).thenReturn(defaultValue);
		
		RequestHeader result2 = new RequestHeader(rp);
		name.assertEquals(result2.getValue());
		Assert.assertTrue(result2.isRequired());
		defaultValue.assertEquals(result2.getDefaultValue());
		
		result2.assertEquals(result);
		Assert.assertEquals(result.hashCode(), result2.hashCode());
	}
}
