/*
  Copyright 2012 - 2013 Jerome Leleu

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
package org.pac4j.play.java;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.CallbackController;
import org.pac4j.play.Config;
import org.pac4j.play.Constants;
import org.pac4j.play.StorageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * This action checks if the user is not authenticated and starts the authentication process if necessary.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class RequiresAuthenticationAction extends Action<Result> {
    
    private static final Logger logger = LoggerFactory.getLogger(RequiresAuthenticationAction.class);
    
    private static final Method clientNameMethod;
    
    private static final Method targetUrlMethod;
    
    static {
        try {
            clientNameMethod = RequiresAuthentication.class.getDeclaredMethod(Constants.CLIENT_NAME);
            targetUrlMethod = RequiresAuthentication.class.getDeclaredMethod(Constants.TARGET_URL);
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Result call(final Context context) throws Throwable {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(this.configuration);
        final String clientName = (String) invocationHandler.invoke(this.configuration, clientNameMethod, null);
        logger.debug("clientName : {}", clientName);
        final String targetUrl = (String) invocationHandler.invoke(this.configuration, targetUrlMethod, null);
        logger.debug("targetUrl : {}", targetUrl);
        // get or create session id
        final String sessionId = StorageHelper.getOrCreationSessionId(context.session());
        logger.debug("sessionId : {}", sessionId);
        final CommonProfile profile = StorageHelper.getProfile(sessionId);
        logger.debug("profile : {}", profile);
        // has a profile -> access resource
        if (profile != null) {
            return this.delegate.call(context);
        }
        // no profile -> should try authentication if it has not already been tried
        final String startAuth = (String) StorageHelper.get(sessionId, clientName
                                                                       + Constants.START_AUTHENTICATION_SUFFIX);
        logger.debug("startAuth : {}", startAuth);
        StorageHelper.remove(sessionId, clientName + Constants.START_AUTHENTICATION_SUFFIX);
        if (CommonHelper.isNotBlank(startAuth)) {
            logger.error("not authenticated successfully to access a protected area -> forbidden");
            return forbidden(Config.getErrorPage403()).as(Constants.HTML_CONTENT_TYPE);
        }
        // requested url to save
        final String requestedUrlToSave = CallbackController.defaultUrl(targetUrl, context.request().uri());
        logger.debug("requestedUrlToSave : {}", requestedUrlToSave);
        StorageHelper.saveRequestedUrl(sessionId, clientName, requestedUrlToSave);
        // get client
        final BaseClient client = (BaseClient) Config.getClients().findClient(clientName);
        logger.debug("client : {}", client);
        // and compute redirection url (force immediate redirect)
        final String redirectionUrl = client
            .getRedirectionUrl(new JavaWebContext(context.request(), context.response(), context.session()), true);
        logger.debug("redirectionUrl : {}", redirectionUrl);
        // save that this kind of authentication has already been tried
        StorageHelper.save(sessionId, clientName + Constants.START_AUTHENTICATION_SUFFIX, "true");
        return redirect(redirectionUrl);
    }
}
