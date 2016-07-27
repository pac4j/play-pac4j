package org.pac4j.play.store;

import com.google.inject.Inject;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import play.mvc.Http;

/**
 * The cache storage uses the Play Cache, only an identifier is saved into the Play session.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public class PlayCacheStore implements PlaySessionStore {

    private static final Logger logger = LoggerFactory.getLogger(PlayCacheStore.class);

    private final static String SEPARATOR = "$";

    // prefix for the cache
    private String prefix = "";

    // 1 hour = 3600 seconds
    private int timeout = 3600;

    private final CacheApi cache;

    @Inject
    public PlayCacheStore(final CacheApi cache) {
        this.cache = cache;
    }

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
        return cache.get(getKey(sessionId, key));
    }

    @Override
    public void set(final PlayWebContext context, final String key, final Object value) {
        final String sessionId = getOrCreateSessionId(context);
        cache.set(getKey(sessionId, key), value, timeout);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
