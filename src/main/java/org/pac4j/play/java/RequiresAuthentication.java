/*
  Copyright 2012 - 2013 Jerome Leleu

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play.java;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import play.mvc.With;

@With(RequiresAuthenticationAction.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.METHOD, ElementType.TYPE
})
@Inherited
@Documented
/**
 * This annotation protects an action if the user is not authenticated and starts the authentication process if necessary.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public @interface RequiresAuthentication {
    String clientName();
    
    String targetUrl() default "";
}
