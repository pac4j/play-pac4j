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

/**
 * This controller is the core class to handle OAuth authentication and user profile retrieval. Applications must inherit from this
 * controller.
 * <p />
 * Protected methods are meant to be used in the Application sub class to handle HTTP requests.<br />
 * Others public methods can be used in views (*.scala.html) or in the routes file.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class OAuthController extends Controller {
    
    protected static final Logger logger = LoggerFactory.getLogger(OAuthController.class);
    
    private final static String OAUTH_SESSION_ID = "oauthSessionId";
    
    private final static String OAUTH_REQUESTED_URL = "oauthRequestedUrl";
    
    private final static String REDIRECT_URL_LOGOUT_PARAMETER_NAME = "url";
    
    private final static String DEFAULT_URL = "/";
    
    private static String defaultErrorUrl = DEFAULT_URL;
    
    private static String defaultSuccessUrl = DEFAULT_URL;
    
    private static String defaultLogoutUrl = DEFAULT_URL;
    
    private static ProvidersDefinition providersDefinition;
    
    // 1 hour = 3600 seconds
    private static final int DEFAULT_CACHE_TIMEOUT = 3600;
    private static int cacheTimeout = DEFAULT_CACHE_TIMEOUT;
    
    /**
     * This method is used to initialize the providers definition. It must be called from a static initializer in the Application
     * (inheriting from this controller).
     * 
     * @param baseUrl
     * @param providers
     */
    protected static void init(final String baseUrl, final OAuthProvider... providers) {
        final List<OAuthProvider> newProviders = new ArrayList<OAuthProvider>();
        for (final OAuthProvider provider : providers) {
            newProviders.add(provider);
        }
        providersDefinition = new ProvidersDefinition();
        providersDefinition.setProviders(newProviders);
        providersDefinition.setBaseUrl(baseUrl);
        providersDefinition.init();
    }
    
    /**
     * This method must be used to test if the user is OAuth authenticated.
     * 
     * @return if the user is OAuth authenticated
     */
    protected static boolean isAuthent() {
        final String sessionId = session(OAUTH_SESSION_ID);
        logger.debug("isAuthent for sessionId : {}", sessionId);
        return sessionId != null;
    }
    
    /**
     * This method returns the url of the OAuth provider where the user must be redirected for authentication.<br />
     * The current requested url is saved into session to be restored after OAuth authentication.
     * 
     * @param provider
     * @return the url of the OAuth provider where to redirect the user
     */
    public static String redirectUrl(final OAuthProvider provider) {
        return redirectUrl(provider, null);
    }
    
    /**
     * This method redirects the user to the OAuth provider for authentication.<br />
     * The current requested url is saved into session to be restored after OAuth authentication.
     * 
     * @param provider
     * @return the redirection to the OAuth provider
     */
    protected static Result redirectTo(final OAuthProvider provider) {
        return redirect(redirectUrl(provider));
    }
    
    /**
     * This method returns the url of the OAuth provider where the user must be redirected for authentication.<br />
     * The input <code>targetUrl</code> (or the current requested url if <code>null</code>) is saved into session to be restored after OAuth
     * authentication.
     * 
     * @param provider
     * @param targetUrl
     * @return the url of the OAuth provider where to redirect the user
     */
    public static String redirectUrl(final OAuthProvider provider, final String targetUrl) {
        // save requested url to session
        final String savedRequestUrl = getRedirectUrl(targetUrl, request().uri());
        logger.debug("save url before redirectUrl : {}", savedRequestUrl);
        session(OAUTH_REQUESTED_URL, savedRequestUrl);
        // redirect to the OAuth provider for authentication
        final String redirectUrl = provider.getAuthorizationUrl(new PlayUserSession(session()));
        logger.debug("redirectUrl to : {}", redirectUrl);
        return redirectUrl;
    }
    
    /**
     * This method redirects the user to the OAuth provider for authentication.<br />
     * The input <code>targetUrl</code> (or the current requested url if <code>null</code>) is saved into session to be restored after OAuth
     * authentication.
     * 
     * @param provider
     * @param targetUrl
     * @return the redirection to the OAuth provider
     */
    protected static Result redirectTo(final OAuthProvider provider, final String targetUrl) {
        return redirect(redirectUrl(provider, targetUrl));
    }
    
    /**
     * This method returns the OAuth user profile if the user is OAuth authenticated or null otherwise.
     * 
     * @return the OAuth user profile if the user is OAuth authenticated or <code>null</code> otherwise
     */
    protected static UserProfile profile() {
        // get the session id
        final String sessionId = session(OAUTH_SESSION_ID);
        logger.debug("profile for sessionId : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            // get the user profile in cache
            final UserProfile userProfile = (UserProfile) Cache.get(sessionId);
            logger.debug("userProfile : {}", userProfile);
            return userProfile;
        }
        return null;
    }
    
    /**
     * This methods checks if the user needs to be redirected to the OAuth provider for (re-)authentication.
     * 
     * @return if the user should be redirected to the OAuth provider
     */
    protected static boolean needsRedirect() {
        return (!isAuthent() || profile() == null);
    }
    
    /**
     * This method handles the callback call from the OAuth provider to finish the OAuth process authentication. The profile of the
     * authenticated user is retrieved and the originally request url (or the specific saved url) is restored.
     * 
     * @return the redirection to the saved request
     */
    public static Result callback() {
        // parameters in url
        final Map<String, String[]> parameters = request().queryString();
        // get the provider from its type
        final OAuthProvider provider = providersDefinition.findProvider(parameters);
        logger.debug("provider : {}", provider);
        // no provider, redirect to default url
        if (provider == null) return redirect(defaultErrorUrl);
        // get credential
        final OAuthCredential credential = provider.getCredential(new PlayUserSession(session()), parameters);
        logger.debug("credential : {}", credential);
        // get user profile
        final UserProfile userProfile = provider.getUserProfile(credential);
        logger.debug("userProfile : {}", userProfile);
        // user profile = null -> error
        if (userProfile == null) throw new IllegalArgumentException("User profile retrieval failed with null profile");
        // get current sessionId
        String sessionId = session(OAUTH_SESSION_ID);
        // if null, generate a new one
        if (sessionId == null) {
            // generate id for OAuth session
            sessionId = java.util.UUID.randomUUID().toString();
            logger.debug("generated sessionId : {}", sessionId);
            // and save session
            session(OAUTH_SESSION_ID, sessionId);
        }
        // save userProfile in cache
        Cache.set(sessionId, userProfile, cacheTimeout);
        // retrieve saved request and redirect
        return redirect(getRedirectUrl(session(OAUTH_REQUESTED_URL), defaultSuccessUrl));
    }
    
    /**
     * This method logouts the user from OAuth authentication.
     */
    protected static void logout() {
        // get the session id
        final String sessionId = session(OAUTH_SESSION_ID);
        logger.debug("logout sessionId : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            // remove user profile from cache
            Cache.set(sessionId, null, 0);
            logger.debug("remove user profile and sessionId : {}", sessionId);
        }
        session().remove(OAUTH_SESSION_ID);
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
     * {@link #REDIRECT_URL_LOGOUT_PARAMETER_NAME} parameter name or to the <code>defaultLogoutUrl</code>.
     * 
     * @return the redirection to the "logout url"
     */
    public static Result logoutAndRedirect() {
        logout();
        // parameters in url
        final Map<String, String[]> parameters = request().queryString();
        String[] values = parameters.get(REDIRECT_URL_LOGOUT_PARAMETER_NAME);
        String value = null;
        if (values != null && values.length == 1) {
            value = values[0];
        }
        return redirect(getRedirectUrl(value, defaultLogoutUrl));
    }
    
    /**
     * This method returns the redirect url from a specified url with a default url.
     * 
     * @param url
     * @param defaultUrl
     * @return the redirect url
     */
    private static String getRedirectUrl(final String url, final String defaultUrl) {
        logger.debug("compute redirectUrl from url : {} / defaultUrl : {}", url, defaultUrl);
        String redirectUrl = defaultUrl;
        if (StringUtils.isNotBlank(url)) {
            redirectUrl = url;
        }
        return redirectUrl;
    }
    
    /**
     * This method sets the default url after a successfull authentication.
     * 
     * @param defaultUrl
     */
    public static void setDefaultSuccessUrl(final String defaultUrl) {
        defaultSuccessUrl = defaultUrl;
    }
    
    /**
     * This method sets the default url if an error happens during authentication.
     * 
     * @param defaultUrl
     */
    public static void setDefaultErrorUrl(final String defaultUrl) {
        defaultErrorUrl = defaultUrl;
    }
    
    /**
     * This method sets the default url after logout.
     * 
     * @param defaultUrl
     */
    public static void setDefaultLogoutUrl(final String defaultUrl) {
        defaultLogoutUrl = defaultUrl;
    }
    
    /**
     * This method sets the timeout for the cache.
     * 
     * @param timeout
     */
    public static void setCacheTimeout(final int timeout) {
        cacheTimeout = timeout;
    }
}
