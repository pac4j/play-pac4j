package org.pac4j.play.store;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.Cache;
import play.mvc.Http;

import java.util.LinkedHashMap;

/**
 * The cache storage uses the Play Cache, only an identifier is saved into the Play session.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public final class PlayCacheStore implements SessionStore<PlayWebContext> {

    private static final Logger logger = LoggerFactory.getLogger(PlayCacheStore.class);

    private final static String SEPARATOR = "$";

    // prefix for the cache
    private String prefix = "";

    // 1 hour = 3600 seconds
    private int profileTimeout = 3600;

    // 1 minute = 60 second
    private int sessionTimeout = 60;

    String getKey(final String sessionId, final String key) {
        return prefix + SEPARATOR + sessionId + SEPARATOR + key;
    }

    @Override
    public String getOrCreateSessionId(final PlayWebContext context) {
        final Http.Session session = context.getJavaSession();
        // get current sessionId
        String sessionId = session.get(Pac4jConstants.SESSION_ID);
        logger.trace("retrieved sessionId: {}", sessionId);
        // if null, generate a new one
        if (sessionId == null) {
            // generate id for session
            sessionId = java.util.UUID.randomUUID().toString();
            logger.debug("generated sessionId: {}", sessionId);
            // and save it to session
            session.put(Pac4jConstants.SESSION_ID, sessionId);
        }
        return sessionId;
    }

    @Override
    public Object get(final PlayWebContext context, final String key) {
        final String sessionId = getOrCreateSessionId(context);
        return Cache.get(getKey(sessionId, key));
    }

    @Override
    public void set(final PlayWebContext context, final String key, final Object value) {
        int timeout;
        if (value instanceof CommonProfile || value instanceof LinkedHashMap) {
            timeout = profileTimeout;
        } else {
            timeout = sessionTimeout;
        }
        final String sessionId = getOrCreateSessionId(context);
        Cache.set(getKey(sessionId, key), value, timeout);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getProfileTimeout() {
        return profileTimeout;
    }

    public void setProfileTimeout(int profileTimeout) {
        this.profileTimeout = profileTimeout;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
