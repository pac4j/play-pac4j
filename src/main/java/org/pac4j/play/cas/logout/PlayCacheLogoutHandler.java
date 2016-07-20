package org.pac4j.play.cas.logout;

import com.google.inject.Provider;
import org.pac4j.cas.logout.NoLogoutHandler;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlayCacheStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;

import javax.inject.Inject;

/**
 * This class handles logout requests from a CAS server using the Play Cache.
 * 
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class PlayCacheLogoutHandler extends NoLogoutHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    private final CacheApi cache;
    private final Provider<CacheApi> cacheApiProvider;

    @Inject
    public PlayCacheLogoutHandler(final CacheApi cacheApi) {
        this.cacheApiProvider = null;
        this.cache = cacheApi;
    }

    public PlayCacheLogoutHandler(final Provider<CacheApi> cacheApiProvider) {
        this.cache = null;
        this.cacheApiProvider = cacheApiProvider;
    }

    private CacheApi getCache() {
        return cache != null ? cache : cacheApiProvider.get();
    }


    public void destroySession(WebContext context) {
        final PlayWebContext webContext = (PlayWebContext) context;
        final String logoutRequest = context.getRequestParameter("logoutRequest");
        logger.debug("logoutRequest: {}", logoutRequest);
        final String ticket = CommonHelper.substringBetween(logoutRequest, "SessionIndex>", "</");
        logger.debug("extract ticket: {}", ticket);
        final String sessionId = getCache().get(ticket);
        getCache().remove(ticket);
        webContext.getJavaSession().put(Pac4jConstants.SESSION_ID, sessionId);
        final ProfileManager profileManager = new ProfileManager(webContext);
        profileManager.remove(true);
    }

    public void recordSession(WebContext context, String ticket) {
        logger.debug("ticket: {}", ticket);
        final PlayWebContext webContext = (PlayWebContext) context;
        final PlayCacheStore playCacheStore = (PlayCacheStore) webContext.getSessionStore();
        final String sessionId = playCacheStore.getOrCreateSessionId(webContext);
        logger.debug("save sessionId: {}", sessionId);
        getCache().set(ticket, sessionId, playCacheStore.getProfileTimeout());
    }
}
