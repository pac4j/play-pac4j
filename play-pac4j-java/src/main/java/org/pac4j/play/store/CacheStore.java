/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play.store;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.Cache;
import play.mvc.Http;

import javax.inject.Singleton;

/**
 * The cache storage uses the Play Cache, only an identifier is saved into the Play session.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
@Singleton
public class CacheStore implements DataStore {

    private static final Logger logger = LoggerFactory.getLogger(CacheStore.class);

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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public Object get(final PlayWebContext context, final String key) {
        final String sessionId = getOrCreateSessionId(context);
        return Cache.get(getKey(sessionId, key));
    }

    /**
     * {@inheritDoc}
     */
    public void set(final PlayWebContext context, final String key, final Object value) {
        int timeout;
        if (value instanceof UserProfile) {
            timeout = profileTimeout;
        } else {
            timeout = sessionTimeout;
        }
        final String sessionId = getOrCreateSessionId(context);
        Cache.set(getKey(sessionId, key), value, timeout);
    }

    /**
     * {@inheritDoc}
     */
    public void invalidate(final PlayWebContext context) {
        context.getJavaSession().remove(Pac4jConstants.SESSION_ID);
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
