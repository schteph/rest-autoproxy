package hr.schteph.common.rest.autoproxy;

import hr.schteph.common.rest.autoproxy.model.PathVariable;
import hr.schteph.common.rest.autoproxy.model.RequestHeader;
import hr.schteph.common.rest.autoproxy.model.RequestParam;

import java.util.Map;

import lombok.Setter;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ValueConstants;

/**
 * @author scvitanovic
 */
@Setter
public class RequestArgumentsMapperDefault implements RequestArgumentsMapper {
	
	private RequestArgumentsValidator validator = new RequestArgumentsValidatorDefault();
	
	@Override
	public boolean fillRequestHeader(Integer key, Object object, HttpHeaders headerValues,
			Map<Integer, RequestHeader> requestHeaders)
	{
		if (!requestHeaders.containsKey(key)) {
			return false;
		}
		RequestHeader rh = requestHeaders.get(key);
		String value = convertArgumentToString(object, rh.isRequired(), rh.getDefaultValue(), "Request header", rh.getValue());
		headerValues.set(rh.getValue(), value);
		return true;
	}

	@Override
	public boolean fillRequestParam(Integer key, Object value, Map<String, Object> requestParamValues,
			Map<Integer, RequestParam> requestParams)
	{
		if (!requestParams.containsKey(key)) {
			return false;
		}
		RequestParam rp = requestParams.get(key);
		validator.validateArgument(value, rp.isRequired(), "Request param", rp.getValue());
		Object realValue = value;
		if (realValue == null && !ValueConstants.DEFAULT_NONE.equals(rp.getDefaultValue())) {
			realValue = rp.getDefaultValue();
		}
		requestParamValues.put(rp.getValue(), realValue);
		return true;
	}

	@Override
	public boolean fillPathVariable(Integer key, Object value, Map<String, Object> pathVariableValues,
			Map<Integer, PathVariable> pathVariables)
	{
		if (!pathVariables.containsKey(key)) {
			return false;
		}
		PathVariable pv = pathVariables.get(key);
		validator.validateArgument(value, true, "Path variable", pv.getValue());
		pathVariableValues.put(pv.getValue(), value);
		return true;
	}

	@Override
	public Object extractRequestBody(Object object, boolean requestBodyRequired) {
		Object retVal = object;
		validator.validateRequestBody(retVal, requestBodyRequired);
		return retVal;
	}

	public String convertArgumentToString(Object arg, boolean required, String defaultValue, String type, String name)
	{
		validator.validateArgument(arg, required, type, name);
		String value;
		if (arg == null) {
			if (!ValueConstants.DEFAULT_NONE.equals(defaultValue)) {
				value = defaultValue;
			} else {
				value = null;
			}
		} else {
			value = arg.toString();
		}
		return value;
	}
}
