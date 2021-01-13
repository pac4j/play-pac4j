package org.pac4j.play.deadbolt2;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.CommonHelper;
import play.libs.concurrent.HttpExecutionContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple handler cache to store {@link Pac4jHandler}. The deadbolt key is the pac4j clients parameter,
 * the default key being a <code>null</code> clients parameter.
 *
 * @author Jerome Leleu
 * @since 2.6.0
 */
@Singleton
public class Pac4jHandlerCache implements HandlerCache {

    private final Map<String, DeadboltHandler> handlers = new HashMap<>();

    private Config config;

    private HttpExecutionContext httpExecutionContext;

    private SessionStore sessionStore;

    private DeadboltHandler defaultHandler;

    private final Pac4jRoleHandler roleHandler;

    @Inject
    public Pac4jHandlerCache(final Config config, final HttpExecutionContext httpExecutionContext, final SessionStore sessionStore, final Pac4jRoleHandler roleHandler) {
        this.config = config;
        this.httpExecutionContext = httpExecutionContext;
        this.sessionStore = sessionStore;
        this.roleHandler = roleHandler;
        defaultHandler = new Pac4jHandler(config, httpExecutionContext, null, sessionStore, roleHandler);
        handlers.put("defaultHandler", defaultHandler);
    }

    @Override
    public DeadboltHandler apply(final String clients) {
        DeadboltHandler handler = handlers.get(clients);
        if (handler == null) {
            handler = getAndBuildHandler(clients);
        }
        return handler;
    }

    protected synchronized DeadboltHandler getAndBuildHandler(final String clients) {
        DeadboltHandler handler = handlers.get(clients);
        if (handler == null) {
            handler = new Pac4jHandler(config, httpExecutionContext, clients, sessionStore, roleHandler);
        }
        return handler;
    }

    @Override
    public DeadboltHandler get() {
        return defaultHandler;
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "handlers", handlers, "config", config,
                "httpExecutionContext", httpExecutionContext, "playSessionStore", sessionStore);
    }
}
