package org.pac4j.play.java;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.config.Pac4jPlayConfig;
import org.pac4j.play.context.PlayFrameworkParameters;
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
@Slf4j
public class SecureAction extends Action<Result> {

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

          return internalCall(new PlayFrameworkParameters(req), clients, authorizers, matchers);
        } catch(Throwable t) {
          throw new RuntimeException(t);
        }        
    }

    public CompletionStage<Result> call(final PlayFrameworkParameters parameters, final String clients, final String authorizers, final String matchers) throws Throwable {
        return internalCall(parameters, clients, authorizers, matchers);
    }

    protected CompletionStage<Result> internalCall(final PlayFrameworkParameters parameters, final String clients, final String authorizers, final String matchers) throws Throwable {

        Pac4jPlayConfig.applyPlaySettingsIfUndefined(config, sessionStore);

        final HttpActionAdapter actionAdapterWrapper = (action, webCtx) -> CompletableFuture.completedFuture(config.getHttpActionAdapter().adapt(action, webCtx));

        val configSecurity = new Config()
                .withClients(config.getClients())
                .withAuthorizers(config.getAuthorizers())
                .withMatchers(config.getMatchers())
                .withSecurityLogic(config.getSecurityLogic())
                .withCallbackLogic(config.getCallbackLogic())
                .withLogoutLogic(config.getLogoutLogic())
                .withWebContextFactory(config.getWebContextFactory())
                .withSessionStoreFactory(config.getSessionStoreFactory())
                .withProfileManagerFactory(config.getProfileManagerFactory())
                .withHttpActionAdapter(actionAdapterWrapper);

        return (CompletionStage<Result>) configSecurity.getSecurityLogic().perform(configSecurity, (webCtx, session, profiles, p) -> {
	            // when called from Scala
	            if (delegate == null) {
	                return CompletableFuture.completedFuture(null);
	            } else {
	                final PlayWebContext playWebContext = (PlayWebContext) webCtx;
	                return delegate.call((Http.Request) playWebContext.supplementRequest(parameters.getJavaRequest()));
	            }
            }, clients, authorizers, matchers, parameters);
    }

    protected String getStringParam(final InvocationHandler invocationHandler, final Method method, final String defaultValue) throws Throwable {
        String value = (String) invocationHandler.invoke(configuration, method, null);
        if (value == null) {
            value = defaultValue;
        }
        LOGGER.debug("String param: {}: {}", method.getName(), value);
        return value;
    }

    protected boolean getBooleanParam(final InvocationHandler invocationHandler, final Method method, final boolean defaultValue) throws Throwable {
        Boolean value = (Boolean) invocationHandler.invoke(configuration, method, null);
        if (value == null) {
            value = defaultValue;
        }
        LOGGER.debug("Boolean param: {}: {}", method.getName(), value);
        return value;
    }
}
