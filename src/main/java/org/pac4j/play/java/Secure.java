package org.pac4j.play.java;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import play.mvc.With;

@With(SecureAction.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
/**
 * This annotation is used for the {@link SecureAction}.
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public @interface Secure {

    String clients() default "";

    String authorizers() default "";

    String matchers() default "";
}
