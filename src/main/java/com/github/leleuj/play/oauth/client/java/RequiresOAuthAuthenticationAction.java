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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.StringUtils;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.OAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

import com.github.leleuj.play.oauth.client.OAuthConfiguration;
import com.github.leleuj.play.oauth.client.OAuthConstants;
import com.github.leleuj.play.oauth.client.OAuthController;

/**
 * This action checks if the user is not "OAuth authenticated" and starts the OAuth authentication process if necessary.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class RequiresOAuthAuthenticationAction extends Action<Result> {
    
    private static final Logger logger = LoggerFactory.getLogger(RequiresOAuthAuthenticationAction.class);
    
    private static final Method providerTypeMethod;
    
    private static final Method targetUrlMethod;
    
    static {
        try {
            providerTypeMethod = RequiresOAuthAuthentication.class.getDeclaredMethod(OAuthConstants.PROVIDER_TYPE);
            targetUrlMethod = RequiresOAuthAuthentication.class.getDeclaredMethod(OAuthConstants.TARGET_URL);
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Result call(final Context context) throws Throwable {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(this.configuration);
        final String providerType = (String) invocationHandler.invoke(this.configuration, providerTypeMethod, null);
        logger.debug("providerType : {}", providerType);
        final String targetUrl = (String) invocationHandler.invoke(this.configuration, targetUrlMethod, null);
        logger.debug("targetUrl : {}", targetUrl);
        final String sessionId = context.session().get(OAuthConstants.OAUTH_SESSION_ID);
        logger.debug("sessionId : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            final UserProfile userProfile = (UserProfile) Cache.get(sessionId);
            logger.debug("userProfile : {}", userProfile);
            if (userProfile != null) return this.delegate.call(context);
        }
        // save requested url to session
        final String savedRequestUrl = OAuthController.getRedirectUrl(targetUrl, context.request().uri());
        logger.debug("save url before redirectUrl : {}", savedRequestUrl);
        context.session().put(OAuthConstants.OAUTH_REQUESTED_URL, savedRequestUrl);
        // get provider
        final OAuthProvider provider = OAuthConfiguration.getProvidersDefinition().findProvider(providerType);
        logger.debug("provider : {}", provider);
        // and compute authorization url
        final String redirectUrl = provider.getAuthorizationUrl(new JavaUserSession(context.session()));
        logger.debug("redirectUrl to : {}", redirectUrl);
        return redirect(redirectUrl);
    }
}
