package hr.schteph.common.rest.autoproxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * If annotated on a type, all the methods will map response headers to the
 * response.
 * 
 * @author scvitanovic
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MapResponseHeaders {
	boolean value() default true;
}
