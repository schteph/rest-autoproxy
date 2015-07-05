package hr.schteph.common.rest.autoproxy;

import hr.schteph.common.rest.autoproxy.model.PathVariable;
import hr.schteph.common.rest.autoproxy.model.RequestHeader;
import hr.schteph.common.rest.autoproxy.model.RequestParam;

import java.util.Map;

import org.springframework.http.HttpHeaders;

/**
 * @author scvitanovic
 */
public interface RequestArgumentsMapper {
	public boolean fillRequestHeader(Integer key, Object object, HttpHeaders headerValues,
			Map<Integer, RequestHeader> requestHeaders);

	public boolean fillRequestParam(Integer key, Object value, Map<String, Object> requestParamValues,
			Map<Integer, RequestParam> requestParams);

	public boolean fillPathVariable(Integer key, Object value, Map<String, Object> pathVariableValues,
			Map<Integer, PathVariable> pathVariables);
	
	public Object extractRequestBody(Object object, boolean requestBodyRequired);
}
