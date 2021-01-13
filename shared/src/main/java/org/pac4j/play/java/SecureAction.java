package org.pac4j.play.java;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.context.PlayContextFactory;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * <p>This filter protects an URL.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.0.0
 */
public class SecureAction extends Action<Result> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private SecurityLogic securityLogic;
    
    protected final static Method CLIENTS_METHOD;

    protected final static Method AUTHORIZERS_METHOD;

    protected final static Method MATCHERS_METHOD;

    static {
        try {
            CLIENTS_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.CLIENTS);
            AUTHORIZERS_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.AUTHORIZERS);
            MATCHERS_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.MATCHERS);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new TechnicalException(e);
        }
    }

    final private Config config;

    final private SessionStore sessionStore;

    @Inject
    public SecureAction(final Config config, final SessionStore sessionStore) {
        this.config = config;
        this.sessionStore = sessionStore;
    }

    @Override
    public CompletionStage<Result> call(final Http.Request req) {
        try {
          final InvocationHandler invocationHandler = Proxy.getInvocationHandler(configuration);
          final String clients = getStringParam(invocationHandler, CLIENTS_METHOD, null);
          final String authorizers = getStringParam(invocationHandler, AUTHORIZERS_METHOD, null);
          final String matchers = getStringParam(invocationHandler, MATCHERS_METHOD, null);

          final WebContext context = FindBest.webContextFactory(null, config, PlayContextFactory.INSTANCE).newContext(req);

          return internalCall(req, context, sessionStore, clients, authorizers, matchers);
        } catch(Throwable t) {
          throw new RuntimeException(t);
        }        
    }

    public CompletionStage<Result> call(final WebContext webContext, final SessionStore sessionStore, final String clients, final String authorizers, final String matchers) throws Throwable {
        return internalCall(null, webContext, sessionStore, clients, authorizers, matchers);
    }

    protected CompletionStage<Result> internalCall(final Http.Request req, final WebContext webContext, final SessionStore sessionStore, final String clients, final String authorizers, final String matchers) throws Throwable {

        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(null, config, PlayHttpActionAdapter.INSTANCE);
        final SecurityLogic bestLogic = FindBest.securityLogic(securityLogic, config, DefaultSecurityLogic.INSTANCE);

        final HttpActionAdapter actionAdapterWrapper = (action, webCtx) -> CompletableFuture.completedFuture(bestAdapter.adapt(action, webCtx));

        return (CompletionStage<Result>) bestLogic.perform(webContext, sessionStore, config, (webCtx, session, profiles, parameters) -> {
	            // when called from Scala
	            if (delegate == null) {
	                return CompletableFuture.completedFuture(null);
	            } else {
	                final PlayWebContext playWebContext = (PlayWebContext) webCtx;
	                return delegate.call(playWebContext.supplementRequest(req));
	            }
            }, actionAdapterWrapper, clients, authorizers, matchers);
    }

    protected String getStringParam(final InvocationHandler invocationHandler, final Method method, final String defaultValue) throws Throwable {
        String value = (String) invocationHandler.invoke(configuration, method, null);
        if (value == null) {
            value = defaultValue;
        }
        logger.debug("String param: {}: {}", method.getName(), value);
        return value;
    }

    protected boolean getBooleanParam(final InvocationHandler invocationHandler, final Method method, final boolean defaultValue) throws Throwable {
        Boolean value = (Boolean) invocationHandler.invoke(configuration, method, null);
        if (value == null) {
            value = defaultValue;
        }
        logger.debug("Boolean param: {}: {}", method.getName(), value);
        return value;
    }

    public SecurityLogic getSecurityLogic() {
        return securityLogic;
    }

    public void setSecurityLogic(final SecurityLogic securityLogic) {
        this.securityLogic = securityLogic;
    }
}
