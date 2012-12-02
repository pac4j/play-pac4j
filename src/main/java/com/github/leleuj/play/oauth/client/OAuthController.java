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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.scribe.up.credential.OAuthCredential;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.OAuthProvider;
import org.scribe.up.provider.ProvidersDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;

import com.github.leleuj.play.oauth.client.java.JavaUserSession;

/**
 * This controller is the class to finish the OAuth authentication process and logout the user.
 * <p />
 * Public methods : {@link #callback()}, {@link #logoutAndOk()} and {@link #logoutAndRedirect()} must be used in the routes file.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class OAuthController extends Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);
    
    /**
     * This method handles the callback call from the OAuth provider to finish the OAuth process authentication. The profile of the
     * authenticated user is retrieved and the originally request url (or the specific saved url) is restored.
     * 
     * @return the redirection to the saved request
     */
    public static Result callback() {
        // providers definition
        ProvidersDefinition providersDefinition = OAuthConfiguration.getProvidersDefinition();
        // not initialized => redirect to error url
        if (providersDefinition == null) return redirect(OAuthConfiguration.getDefaultErrorUrl());
        // parameters in url
        final Map<String, String[]> parameters = request().queryString();
        // get the provider from its type
        final OAuthProvider provider = providersDefinition.findProvider(parameters);
        logger.debug("provider : {}", provider);
        // no provider => redirect to error url
        if (provider == null) return redirect(OAuthConfiguration.getDefaultErrorUrl());
        // get credential
        final OAuthCredential credential = provider.getCredential(new JavaUserSession(session()), parameters);
        logger.debug("credential : {}", credential);
        // no credential, redirect to error url
        if (credential == null) return redirect(OAuthConfiguration.getDefaultErrorUrl());
        // get user profile
        final UserProfile userProfile = provider.getUserProfile(credential);
        logger.debug("userProfile : {}", userProfile);
        // user profile = null -> error
        if (userProfile == null) throw new IllegalArgumentException("User profile retrieval failed with null profile");
        // get current sessionId
        String sessionId = session(OAuthConstants.OAUTH_SESSION_ID);
        logger.debug("retrieved sessionId : {}", sessionId);
        // if null, generate a new one
        if (sessionId == null) {
            // generate id for OAuth session
            sessionId = java.util.UUID.randomUUID().toString();
            logger.debug("generated sessionId : {}", sessionId);
            // and save session
            session(OAuthConstants.OAUTH_SESSION_ID, sessionId);
        }
        // save userProfile in cache
        Cache.set(sessionId, userProfile, OAuthConfiguration.getCacheTimeout());
        // retrieve saved request and redirect
        return redirect(getRedirectUrl(session(OAuthConstants.OAUTH_REQUESTED_URL),
                                       OAuthConfiguration.getDefaultSuccessUrl()));
    }
    
    /**
     * This method logouts the user from OAuth authentication.
     */
    private static void logout() {
        // get the session id
        final String sessionId = session(OAuthConstants.OAUTH_SESSION_ID);
        logger.debug("logout sessionId : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            // remove user profile from cache
            Cache.set(sessionId, null, 0);
            logger.debug("remove user profile and sessionId : {}", sessionId);
        }
        session().remove(OAuthConstants.OAUTH_SESSION_ID);
    }
    
    /**
     * This method logouts the user from OAuth authentication and send him to a blank page.
     * 
     * @return the redirection to the blank page
     */
    public static Result logoutAndOk() {
        logout();
        return ok();
    }
    
    /**
     * This method logouts the user from OAuth authentication and send him to the url defined in the
     * {@link OAuthConstants#REDIRECT_URL_LOGOUT_PARAMETER_NAME} parameter name or to the <code>defaultLogoutUrl</code>.
     * 
     * @return the redirection to the "logout url"
     */
    public static Result logoutAndRedirect() {
        logout();
        // parameters in url
        final Map<String, String[]> parameters = request().queryString();
        final String[] values = parameters.get(OAuthConstants.REDIRECT_URL_LOGOUT_PARAMETER_NAME);
        String value = null;
        if (values != null && values.length == 1) {
            value = values[0];
        }
        return redirect(getRedirectUrl(value, OAuthConfiguration.getDefaultLogoutUrl()));
    }
    
    /**
     * This method returns the redirect url from a specified url with a default url.
     * 
     * @param url
     * @param defaultUrl
     * @return the redirect url
     */
    public static String getRedirectUrl(final String url, final String defaultUrl) {
        logger.debug("compute redirectUrl from url : {} / defaultUrl : {}", url, defaultUrl);
        String redirectUrl = defaultUrl;
        if (StringUtils.isNotBlank(url)) {
            redirectUrl = url;
        }
        return redirectUrl;
    }
}
