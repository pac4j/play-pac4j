/*
  Copyright 2012 - 2014 pac4j organization

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

import org.pac4j.core.client.Clients;
import org.pac4j.core.context.BaseConfig;

/**
 * This class gathers all the configuration.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class Config extends BaseConfig {

    // 1 hour = 3600 seconds
    private static int profileTimeout = 3600;

    // 1 minute = 60 second
    private static int sessionTimeout = 60;

    // all the clients
    private static Clients clients;

    private static String cacheKeyPrefix = "";

    public static int getProfileTimeout() {
        return profileTimeout;
    }

    public static void setProfileTimeout(final int profileTimeout) {
        Config.profileTimeout = profileTimeout;
    }

    public static int getSessionTimeout() {
        return sessionTimeout;
    }

    public static void setSessionTimeout(final int sessionTimeout) {
        Config.sessionTimeout = sessionTimeout;
    }

    public static Clients getClients() {
        return clients;
    }

    public static void setClients(final Clients clients) {
        Config.clients = clients;
    }

    /**
     * Gets the prefix used for all cache operations
     *
     * @return the prefix
     * @since 1.1.2
     */
    public static String getCacheKeyPrefix() {
        return cacheKeyPrefix;
    }

    /**
     * Sets the prefix to use for all cache operations
     *
     * @param cacheKeyPrefix
     * @since 1.1.2
     */
    public static void setCacheKeyPrefix(String cacheKeyPrefix) {
        Config.cacheKeyPrefix = cacheKeyPrefix;
    }

}
