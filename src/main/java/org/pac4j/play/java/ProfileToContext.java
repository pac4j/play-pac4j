package org.pac4j.play.java;

import play.mvc.With;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for the {@link ProfileToContextAction}.
 *
 * @author Sebastian Hardt (s.hardt@micromata.de)
 */
@With(ProfileToContextAction.class)
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProfileToContext
{
}
