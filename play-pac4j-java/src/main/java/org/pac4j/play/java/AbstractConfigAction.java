/*
  Copyright 2012 - 2015 pac4j organization

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

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Result;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * An abstract action which handles configuration.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public abstract class AbstractConfigAction extends Action<Result> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected final static Method CLIENT_NAME_METHOD;

    protected final static Method IS_AJAX_METHOD;

    protected final static Method REQUIRE_ANY_ROLE_METHOD;

    protected final static Method REQUIRE_ALL_ROLES_METHOD;

    protected final static Method USE_SESSION_FOR_DIRECT_CLIENT_METHOD;

    protected final static Method ALLOW_DYNAMIC_CLIENT_SELECTION_METHOD;

    protected final static Method AUTHORIZER_NAME_METHOD;

    static {
        try {
            CLIENT_NAME_METHOD = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.CLIENT_NAME);
            IS_AJAX_METHOD = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.IS_AJAX);
            REQUIRE_ANY_ROLE_METHOD = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.REQUIRE_ANY_ROLE);
            REQUIRE_ALL_ROLES_METHOD = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.REQUIRE_ALL_ROLES);
            USE_SESSION_FOR_DIRECT_CLIENT_METHOD = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.USE_SESSION_FOR_DIRECT_CLIENT);
            ALLOW_DYNAMIC_CLIENT_SELECTION_METHOD = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.ALLOW_DYNAMIC_CLIENT_SELECTION);
            AUTHORIZER_NAME_METHOD = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.AUTHORIZER_NAME);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new TechnicalException(e);
        }
    }

    protected String getStringParam(final InvocationHandler invocationHandler, final Method method, final String defaultValue) throws Throwable {
        String value = (String) invocationHandler.invoke(configuration, method, null);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    protected boolean getBooleanParam(final InvocationHandler invocationHandler, final Method method, final boolean defaultValue) throws Throwable {
        Boolean value = (Boolean) invocationHandler.invoke(configuration, method, null);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
