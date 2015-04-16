/*
  Copyright 2012 - 2014 Jerome Leleu

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
package org.pac4j.play;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.mvc.Http.Session;

/**
 * This class is an helper to store/retrieve objects (from cache).
 * 
 * @author Jerome Leleu
 * @since 1.1.0
 */
public final class StorageHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageHelper.class);
    
    /**
     * Get a session identifier and generates it if no session exists.
     * 
     * @param session the current session
     * @return the session identifier
     */
    public static String getOrCreationSessionId(final Session session) {
        // get current sessionId
        String sessionId = session.get(Constants.SESSION_ID);
        logger.debug("retrieved sessionId : {}", sessionId);
        // if null, generate a new one
        if (sessionId == null) {
            // generate id for session
            sessionId = generateSessionId();
            logger.debug("generated sessionId : {}", sessionId);
            // and save it to session
            session.put(Constants.SESSION_ID, sessionId);
        }
        return sessionId;
    }
    
    /**
     * Generate a session identifier.
     * 
     * @return a session identifier
     */
    public static String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * Get the profile from storage.
     * 
     * @param sessionId the current session identifier
     * @return the user profile
     */
    public static CommonProfile getProfile(final String sessionId) {
        if (sessionId != null) {
            return (CommonProfile) get(sessionId);
        }
        return null;
    }
    
    /**
     * Save a user profile in storage.
     * 
     * @param sessionId the current session identifier
     * @param profile a user profile
     */
    public static void saveProfile(final String sessionId, final CommonProfile profile) {
        if (sessionId != null) {
            save(sessionId, profile, Config.getProfileTimeout());
        }
    }
    
    /**
     * Remove a user profile from storage.
     * 
     * @param sessionId the current session identifier
     */
    public static void removeProfile(final String sessionId) {
        if (sessionId != null) {
            remove(sessionId);
        }
    }
    
    /**
     * Get a requested url from storage.
     * 
     * @param sessionId the current session identifier
     * @param clientName the client name
     * @return the requested url
     */
    public static String getRequestedUrl(final String sessionId, final String clientName) {
        return (String) get(sessionId, clientName + Constants.SEPARATOR + Constants.REQUESTED_URL);
    }
    
    /**
     * Save a requested url to storage.
     * 
     * @param sessionId the current session identifier
     * @param clientName the client name
     * @param requestedUrl the original requested url
     */
    public static void saveRequestedUrl(final String sessionId, final String clientName, final String requestedUrl) {
        save(sessionId, clientName + Constants.SEPARATOR + Constants.REQUESTED_URL, requestedUrl);
    }
    
    /**
     * Get an object from storage.
     * 
     * @param sessionId the current session identifier
     * @param key a key
     * @return the object
     */
    public static Object get(final String sessionId, final String key) {
        if (sessionId != null) {
            return get(sessionId + Constants.SEPARATOR + key);
        }
        return null;
    }
    
    /**
     * Save an object in storage.
     * 
     * @param sessionId the current session identifier
     * @param key a key
     * @param value a value to store
     */
    public static void save(final String sessionId, final String key, final Object value) {
        if (sessionId != null) {
            save(sessionId + Constants.SEPARATOR + key, value, Config.getSessionTimeout());
        }
    }
    
    /**
     * Remove an object in storage.
     * 
     * @param sessionId the current session identifier
     * @param key a key
     */
    public static void remove(final String sessionId, final String key) {
        remove(sessionId + Constants.SEPARATOR + key);
    }
    
    /**
     * Get an object from storage.
     * 
     * @param key a key
     * @return the object
     */
    public static Object get(final String key) {
        return Cache.get(getCacheKey(key));
    }
    
    /**
     * Save an object in storage.
     * 
     * @param key a key
     * @param value a value to store
     * @param timeout the timeout
     */
    public static void save(final String key, final Object value, final int timeout) {
        Cache.set(getCacheKey(key), value, timeout);
    }
    
    /**
     * Remove an object from storage.
     * 
     * @param key a key
     */
    public static void remove(final String key) {
        Cache.remove(getCacheKey(key));
    }

    static String getCacheKey(final String key) {
        return (StringUtils.isNotBlank(Config.getCacheKeyPrefix()))
                ? Config.getCacheKeyPrefix() + ":" + key
                : key;
    }
}
