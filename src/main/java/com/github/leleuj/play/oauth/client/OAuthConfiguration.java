/*
  Copyright 2012 Jerome Leleu

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
package com.github.leleuj.play.oauth.client;

import java.util.ArrayList;
import java.util.List;

import org.scribe.up.provider.OAuthProvider;
import org.scribe.up.provider.ProvidersDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gathers the OAuth configuration.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class OAuthConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthConfiguration.class);
    
    private final static String DEFAULT_URL = "/";
    
    private static String defaultErrorUrl = DEFAULT_URL;
    
    private static String defaultSuccessUrl = DEFAULT_URL;
    
    private static String defaultLogoutUrl = DEFAULT_URL;
    
    private static ProvidersDefinition providersDefinition;
    
    // 1 hour = 3600 seconds
    private static final int DEFAULT_CACHE_TIMEOUT = 3600;
    private static int cacheTimeout = DEFAULT_CACHE_TIMEOUT;
    
    /**
     * This method is used to initialize the providers definition.
     * 
     * @param baseUrl
     * @param providers
     */
    public static void init(final String baseUrl, final OAuthProvider... providers) {
        logger.debug("init with baseUrl : {}", baseUrl);
        final List<OAuthProvider> newProviders = new ArrayList<OAuthProvider>();
        for (final OAuthProvider provider : providers) {
            newProviders.add(provider);
            logger.debug("init with provider : {}", provider);
        }
        providersDefinition = new ProvidersDefinition();
        providersDefinition.setProviders(newProviders);
        providersDefinition.setBaseUrl(baseUrl);
        providersDefinition.init();
    }
    
    public static String getDefaultErrorUrl() {
        return defaultErrorUrl;
    }
    
    public static void setDefaultErrorUrl(final String defaultErrorUrl) {
        OAuthConfiguration.defaultErrorUrl = defaultErrorUrl;
    }
    
    public static String getDefaultSuccessUrl() {
        return defaultSuccessUrl;
    }
    
    public static void setDefaultSuccessUrl(final String defaultSuccessUrl) {
        OAuthConfiguration.defaultSuccessUrl = defaultSuccessUrl;
    }
    
    public static String getDefaultLogoutUrl() {
        return defaultLogoutUrl;
    }
    
    public static void setDefaultLogoutUrl(final String defaultLogoutUrl) {
        OAuthConfiguration.defaultLogoutUrl = defaultLogoutUrl;
    }
    
    public static int getCacheTimeout() {
        return cacheTimeout;
    }
    
    public static void setCacheTimeout(final int cacheTimeout) {
        OAuthConfiguration.cacheTimeout = cacheTimeout;
    }
    
    public static ProvidersDefinition getProvidersDefinition() {
        return providersDefinition;
    }
}
