package org.pac4j.play.java;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.pac4j.play.store.PlaySessionStore;
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

    private SecurityLogic<CompletionStage<Result>, PlayWebContext> securityLogic;
    
    protected final static Method CLIENTS_METHOD;

    protected final static Method AUTHORIZERS_METHOD;

    protected final static Method MATCHERS_METHOD;

    protected final static Method MULTI_PROFILE_METHOD;

    static {
        try {
            CLIENTS_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.CLIENTS);
            AUTHORIZERS_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.AUTHORIZERS);
            MATCHERS_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.MATCHERS);
            MULTI_PROFILE_METHOD = Secure.class.getDeclaredMethod(Pac4jConstants.MULTI_PROFILE);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new TechnicalException(e);
        }
    }

    final private Config config;

    final private PlaySessionStore sessionStore;

    @Inject
    public SecureAction(final Config config, final PlaySessionStore playSessionStore) {
        this.config = config;
        this.sessionStore = playSessionStore;
    }

    @Override
    public CompletionStage<Result> call(final Http.Request req) {
        try {
          final InvocationHandler invocationHandler = Proxy.getInvocationHandler(configuration);
          final String clients = getStringParam(invocationHandler, CLIENTS_METHOD, null);
          final String authorizers = getStringParam(invocationHandler, AUTHORIZERS_METHOD, null);
          final String matchers = getStringParam(invocationHandler, MATCHERS_METHOD, null);
          final boolean multiProfile = getBooleanParam(invocationHandler, MULTI_PROFILE_METHOD, false);

          final PlayWebContext playWebContext = new PlayWebContext(req, sessionStore);
          return internalCall(req, playWebContext, clients, authorizers, matchers, multiProfile);
        } catch(Throwable t) {
          throw new RuntimeException(t);
        }        
    }

    public CompletionStage<Result> call(final PlayWebContext webContext, final String clients, final String authorizers, final String matchers, final boolean multiProfile) throws Throwable {
        return internalCall(null, webContext, clients, authorizers, matchers, multiProfile);
    }

    protected CompletionStage<Result> internalCall(final Http.Request req, final PlayWebContext webContext, final String clients, final String authorizers, final String matchers, final boolean multiProfile) throws Throwable {

        final HttpActionAdapter<Result, PlayWebContext> bestAdapter = FindBest.httpActionAdapter(null, config, PlayHttpActionAdapter.INSTANCE);
        final SecurityLogic<CompletionStage<Result>, PlayWebContext> bestLogic = FindBest.securityLogic(securityLogic, config, DefaultSecurityLogic.INSTANCE);


        final HttpActionAdapter<CompletionStage<Result>, PlayWebContext> actionAdapterWrapper = (action, webCtx) -> CompletableFuture.completedFuture(bestAdapter.adapt(action, webCtx));
        return bestLogic.perform(webContext, config, (webCtx, profiles, parameters) -> {
	            // when called from Scala
	            if (delegate == null) {
	                return CompletableFuture.completedFuture(null);
	            } else {
	                return delegate.call(webCtx.supplementRequest(req));
	            }
            }, actionAdapterWrapper, clients, authorizers, matchers, multiProfile);
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

    public SecurityLogic<CompletionStage<Result>, PlayWebContext> getSecurityLogic() {
        return securityLogic;
    }

    public void setSecurityLogic(final SecurityLogic<CompletionStage<Result>, PlayWebContext> securityLogic) {
        this.securityLogic = securityLogic;
    }
}
