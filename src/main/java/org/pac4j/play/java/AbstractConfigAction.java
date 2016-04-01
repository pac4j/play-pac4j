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

    protected final static Method CLIENTS_METHOD;

    protected final static Method AUTHORIZERS_METHOD;

    protected final static Method MULTI_PROFILE_METHOD;

    static {
        try {
            CLIENTS_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.CLIENTS);
            AUTHORIZERS_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.AUTHORIZERS);
            MULTI_PROFILE_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.MULTI_PROFILE);
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
