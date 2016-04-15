package org.pac4j.play.cas.logout;

import org.pac4j.cas.logout.NoLogoutHandler;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlayCacheStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.Cache;

/**
 * This class handles logout requests from a CAS server using the Play Cache.
 * 
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class PlayCacheLogoutHandler extends NoLogoutHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public PlayCacheLogoutHandler() {}

    public void destroySession(WebContext context) {
        final PlayWebContext webContext = (PlayWebContext) context;
        final String logoutRequest = context.getRequestParameter("logoutRequest");
        logger.debug("logoutRequest: {}", logoutRequest);
        final String ticket = CommonHelper.substringBetween(logoutRequest, "SessionIndex>", "</");
        logger.debug("extract ticket: {}", ticket);
        final String sessionId = (String) Cache.get(ticket);
        Cache.remove(ticket);
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
        Cache.set(ticket, sessionId, playCacheStore.getProfileTimeout());
    }
}
