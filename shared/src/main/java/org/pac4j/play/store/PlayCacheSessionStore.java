package org.pac4j.play.store;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.SyncCacheApi;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This session store internally uses the {@link PlayCacheStore} which uses the Play Cache, only an identifier is saved into the Play session.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
@Singleton
public class PlayCacheSessionStore implements SessionStore {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PlayCacheSessionStore.class);

    // prefix for the cache
    private String prefix = null;

    // store
    protected PlayCacheStore<String, Map<String, Object>> store;

    protected PlayCacheSessionStore() {}

    @Inject
    public PlayCacheSessionStore(final SyncCacheApi cache) {
        this.store = new PlayCacheStore<>(cache);
        setDefaultTimeout();
    }

    public PlayCacheSessionStore(final Provider<SyncCacheApi> cacheProvider) {
        this.store = new PlayCacheStore<>(cacheProvider);
        setDefaultTimeout();
    }

    String getPrefixedSessionKey(final String sessionId) {
        if (this.prefix != null) {
            return this.prefix + sessionId;
        } else {
            return sessionId;
        }
    }

    @Override
    public Optional<String> getSessionId(final WebContext context, final boolean createSession) {
        // get current sessionId from session or from request
        String sessionId = getSessionIdFromSessionOrRequest(context);
        if (sessionId == null && createSession) {
            // generate id for session
            sessionId = java.util.UUID.randomUUID().toString();
            LOGGER.debug("generated sessionId: {}", sessionId);
            // and save it to session/request
            setSessionIdInSession(context, sessionId);
            context.setRequestAttribute(Pac4jConstants.SESSION_ID, sessionId);
        }
        return Optional.ofNullable(sessionId);
    }

    protected String getSessionIdFromSessionOrRequest(final WebContext context) {
        String sessionId = ((PlayWebContext) context).getNativeSession().get(Pac4jConstants.SESSION_ID).orElse(null);
        LOGGER.debug("retrieved sessionId from session: {}", sessionId);
        if (sessionId == null) {
            sessionId = (String) context.getRequestAttribute(Pac4jConstants.SESSION_ID).orElse(null);
            LOGGER.debug("retrieved sessionId from request: {}", sessionId);
            // re-save it in session if defined
            if (sessionId != null) {
                LOGGER.debug("re-saving sessionId in session: {}", sessionId);
                setSessionIdInSession(context, sessionId);
            }
        }
        return sessionId;
    }

    protected void setSessionIdInSession(final WebContext context, final String sessionId) {
        final PlayWebContext playWebContext = (PlayWebContext) context;
        playWebContext.setNativeSession(playWebContext.getNativeSession().adding(Pac4jConstants.SESSION_ID, sessionId));
    }

    @Override
    public Optional<Object> get(final WebContext context, final String key) {
        final Optional<String> sessionId = getSessionId(context, false);
        if (sessionId.isPresent()) {
            final Optional<Map<String, Object>> values = store.get(getPrefixedSessionKey(sessionId.get()));
            Object value = null;
            if (values != null && values.isPresent()) {
                value = values.get().get(key);
            }
            if (value instanceof Exception) {
                LOGGER.debug("Get value: {} for key: {}", value.toString(), key);
            } else {
                LOGGER.debug("Get value: {} for key: {}", value, key);
            }
            return Optional.ofNullable(value);
        } else {
            LOGGER.debug("Can't get value for key: {}, no session available", key);
            return Optional.empty();
        }
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        final String sessionId = getSessionId(context, true).get();
        String prefixedSessionKey = getPrefixedSessionKey(sessionId);
        Optional<Map<String, Object>> oldValues = store.get(prefixedSessionKey);
        Map<String, Object> values = new HashMap<>();
        if (oldValues != null && oldValues.isPresent()) {
            values = oldValues.get();
        }
        if (value instanceof Exception) {
            LOGGER.debug("Set key: {} with value: {}", key, value.toString());
        } else {
            LOGGER.debug("Set key: {} with value: {}", key, value);
        }
        values.put(key, value);
        store.set(prefixedSessionKey, values);
    }

    @Override
    public boolean destroySession(final WebContext context) {
        final String sessionId = getSessionIdFromSessionOrRequest(context);
        if (sessionId != null) {
            LOGGER.debug("Invalidate session: {}", sessionId);
            ((PlayWebContext) context).setNativeSession(new Http.Session(new HashMap<>()));
            context.setRequestAttribute(Pac4jConstants.SESSION_ID, null);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Object> getTrackableSession(final WebContext context) {
        final String sessionId = getSessionIdFromSessionOrRequest(context);
        LOGGER.debug("Return trackable session: {}", sessionId);
        return Optional.ofNullable(sessionId);
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context, final Object trackableSession) {
        if (trackableSession != null) {
            LOGGER.debug("Rebuild session from trackable session: {}", trackableSession);
            setSessionIdInSession(context, (String) trackableSession);
            context.setRequestAttribute(Pac4jConstants.SESSION_ID, trackableSession);
            return Optional.of(this);
        } else {
            LOGGER.debug("Unable to build session from trackable session");
            return Optional.empty();
        }
    }

    @Override
    public boolean renewSession(final WebContext context) {
        final Optional<String> oldSessionId = getSessionId(context, false);
        final Map<String, Object> oldData = new HashMap<>();
        if (oldSessionId.isPresent()) {
            final Optional<Map<String, Object>> optOldData = store.get(getPrefixedSessionKey(oldSessionId.get()));
            if (optOldData.isPresent()) {
                oldData.putAll(optOldData.get());
            }
        }

        final PlayWebContext playWebContext = (PlayWebContext) context;
        playWebContext.setNativeSession(playWebContext.getNativeSession().removing(Pac4jConstants.SESSION_ID));
        context.setRequestAttribute(Pac4jConstants.SESSION_ID, null);

        final String newSessionId = getSessionId(context, true).get();
        if (oldData.size() > 0) {
            store.set(getPrefixedSessionKey(newSessionId), oldData);
        }

        LOGGER.debug("Renewing session: {} -> {}", oldSessionId, newSessionId);
        return true;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public int getTimeout() {
        return this.store.getTimeout();
    }

    public void setTimeout(final int timeout) {
        this.store.setTimeout(timeout);
    }

    public PlayCacheStore<String, Map<String, Object>> getStore() {
        return store;
    }

    protected void setDefaultTimeout() {
        // 1 hour = 3600 seconds
        this.store.setTimeout(3600);
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "store", store, "prefix", prefix, "timeout", getTimeout());
    }
}
