package org.pac4j.play.java;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.engine.HttpActionAdapterWrapper;
import org.pac4j.play.store.PlaySessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.libs.concurrent.HttpExecutionContext;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter protects an url, based on the {@link #securityLogic}.</p>
 *
 * <p>The configuration can be provided via annotation parameters: <code>clients</code> (list of clients for authentication), <code>authorizers</code> (list of authorizers)
 * and <code>multiProfile</code> (whether multiple profiles should be kept).</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.0.0
 */
public class SecureAction extends Action<Result> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private SecurityLogic<Result, PlayWebContext> securityLogic = new DefaultSecurityLogic<>();
    @Inject
    private HttpExecutionContext ec;

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

    final private Config config;

    final private SessionStore sessionStore;

    @Inject
    public SecureAction(final Config config, final PlaySessionStore playSessionStore) {
        this.config = config;
        this.config.setSessionStore(playSessionStore);
        this.sessionStore = playSessionStore;
    }

    @Override
    public CompletionStage<Result> call(final Context ctx) {
        try{
          final InvocationHandler invocationHandler = Proxy.getInvocationHandler(configuration);
          final String clients = getStringParam(invocationHandler, CLIENTS_METHOD, null);
          final String authorizers = getStringParam(invocationHandler, AUTHORIZERS_METHOD, null);
          final boolean multiProfile = getBooleanParam(invocationHandler, MULTI_PROFILE_METHOD, false);
  
          return internalCall(ctx, clients, authorizers, multiProfile);
        }catch(Throwable t){
          throw new RuntimeException(t);
        }        
    }

    public CompletionStage<Result> internalCall(final Context ctx, final String clients, final String authorizers, final boolean multiProfile) throws Throwable {

        assertNotNull("securityLogic", securityLogic);

        assertNotNull("config", config);
        final PlayWebContext playWebContext = new PlayWebContext(ctx, sessionStore);
        final HttpActionAdapter actionAdapter = config.getHttpActionAdapter();

        return CompletableFuture.supplyAsync(() -> securityLogic.perform(playWebContext, config, (webCtx, parameters) -> {
            // when called from Scala
            if (delegate == null) {
                return CompletableFuture.completedFuture(null);
            } else {
                return delegate.call(ctx);
            }
        }, actionAdapter, clients, authorizers, null, multiProfile), 
        ec.current);
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

    public void setSecurityLogic(SecurityLogic<CompletionStage<Result>, PlayWebContext> securityLogic) {
        this.securityLogic = securityLogic;
    }
}
