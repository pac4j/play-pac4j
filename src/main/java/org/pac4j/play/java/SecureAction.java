package org.pac4j.play.java;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.Pac4jExecutionContext;
import org.pac4j.play.store.PlaySessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.core.j.HttpExecutionContext;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
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
    
    final private Pac4jExecutionContext ec;

    @Inject
    public SecureAction(final Config config, final PlaySessionStore playSessionStore, Pac4jExecutionContext ec) {
        this.config = config;
        this.config.setSessionStore(playSessionStore);
        final SecurityLogic configSecurityLogic = config.getSecurityLogic();
        if (configSecurityLogic != null) {
            this.securityLogic = configSecurityLogic;
        }
        this.sessionStore = playSessionStore;
        this.ec = ec;
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
        final HttpActionAdapter<Result, PlayWebContext> actionAdapter = config.getHttpActionAdapter();
        final Executor pacEc = HttpExecutionContext.fromThread((Executor)ec);
        
        return CompletableFuture.supplyAsync(() ->
        	securityLogic.perform(playWebContext, config, (webCtx, parameters) -> {
	            // when called from Scala
	            if (delegate == null) {
	                return null;
	            } else {
	                return delegate.call(ctx).toCompletableFuture().get();
	            }
            }, actionAdapter, clients, authorizers, null, multiProfile), pacEc);
        
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

    public SecurityLogic<Result, PlayWebContext> getSecurityLogic() {
        return securityLogic;
    }

    public void setSecurityLogic(final SecurityLogic<Result, PlayWebContext> securityLogic) {
        this.securityLogic = securityLogic;
    }
}
