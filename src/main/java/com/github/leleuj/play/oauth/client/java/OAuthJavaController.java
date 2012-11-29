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
package com.github.leleuj.play.oauth.client.java;

import org.apache.commons.lang3.StringUtils;
import org.scribe.up.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.mvc.Controller;

import com.github.leleuj.play.oauth.client.OAuthConfiguration;
import com.github.leleuj.play.oauth.client.OAuthConstants;
import com.github.leleuj.play.oauth.client.OAuthController;

/**
 * This controller is the Java controller to retrieve the user profile or the redirect url to start the OAuth authentication process.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class OAuthJavaController extends Controller {
    
    protected static final Logger logger = LoggerFactory.getLogger(OAuthJavaController.class);
    
    /**
     * This method returns the url of the OAuth provider where the user must be redirected for authentication.<br />
     * The current requested url is saved into session to be restored after OAuth authentication.
     * 
     * @param providerType
     * @return the url of the OAuth provider where to redirect the user
     */
    protected static String redirectUrl(final String providerType) {
        return redirectUrl(providerType, null);
    }
    
    /**
     * This method returns the url of the OAuth provider where the user must be redirected for authentication.<br />
     * The input <code>targetUrl</code> (or the current requested url if <code>null</code>) is saved into session to be restored after OAuth
     * authentication.
     * 
     * @param providerType
     * @param targetUrl
     * @return the url of the OAuth provider where to redirect the user
     */
    protected static String redirectUrl(final String providerType, final String targetUrl) {
        // save requested url to session
        final String savedRequestUrl = OAuthController.getRedirectUrl(targetUrl, request().uri());
        logger.debug("save url before redirectUrl : {}", savedRequestUrl);
        session(OAuthConstants.OAUTH_REQUESTED_URL, savedRequestUrl);
        // redirect to the OAuth provider for authentication
        final String redirectUrl = OAuthConfiguration.getProvidersDefinition().findProvider(providerType)
            .getAuthorizationUrl(new JavaUserSession(session()));
        logger.debug("redirectUrl to : {}", redirectUrl);
        return redirectUrl;
    }
    
    /**
     * This method returns the OAuth user profile if the user is "OAuth authenticated" or <code>null</code> otherwise.
     * 
     * @return the OAuth user profile if the user is "OAuth authenticated" or <code>null</code> otherwise
     */
    protected static UserProfile profile() {
        // get the session id
        final String sessionId = session(OAuthConstants.OAUTH_SESSION_ID);
        logger.debug("profile for sessionId : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            // get the user profile in cache
            final UserProfile userProfile = (UserProfile) Cache.get(sessionId);
            logger.debug("userProfile : {}", userProfile);
            return userProfile;
        }
        return null;
    }
}
