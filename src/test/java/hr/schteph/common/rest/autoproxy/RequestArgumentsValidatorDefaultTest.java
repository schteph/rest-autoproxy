package hr.schteph.common.rest.autoproxy;

import hr.schteph.common.rest.autoproxy.RequestArgumentsValidatorDefault;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import lombok.experimental.ExtensionMethod;

/**
 * @author scvitanovic
 */
@ExtensionMethod({Assert.class})
public class RequestArgumentsValidatorDefaultTest {

	private RequestArgumentsValidatorDefault underTest;
	
	@Before
	public void before() {
		underTest = new RequestArgumentsValidatorDefault();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidateInvalidBody() {
		underTest.validateRequestBody(null, true);
	}
	
	@Test
	public void testValidateValidBody() {
		underTest.validateRequestBody(new Object(), true);
		underTest.validateRequestBody(null, false);
	}
	
	@Test
	public void testValidateValidArgument() {
		String type = "type";
		String name = "name";
		
		Object arg = "non-null";
		
		underTest.validateArgument(arg, true, type, name);
		underTest.validateArgument(arg, false, type, name);
		underTest.validateArgument(null, false, type, name);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidateInvalidArgument() {
		String type = "type";
		String name = "name";
		underTest.validateArgument(null, true, type, name);
	}
}
