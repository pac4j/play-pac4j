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
package org.pac4j.play.java;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.CallbackController;
import org.pac4j.play.Config;
import org.pac4j.play.Constants;
import org.pac4j.play.StorageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is the Java controller to retrieve the user profile or the redirection url to start the authentication process.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class JavaController extends CallbackController {
    
    protected static final Logger logger = LoggerFactory.getLogger(JavaController.class);
    
    /**
     * This method returns the url of the provider where the user must be redirected for authentication.<br />
     * The current requested url is saved into session to be restored after authentication.
     * 
     * @param clientName
     * @return the url of the provider where to redirect the user
     */
    protected static String getRedirectionUrl(final String clientName) {
        return getRedirectionUrl(clientName, null);
    }
    
    /**
     * This method returns the url of the provider where the user must be redirected for authentication.<br />
     * The input <code>targetUrl</code> (or the current requested url if <code>null</code>) is saved into session to be restored after
     * authentication.
     * 
     * @param clientName
     * @param targetUrl
     * @return the url of the provider where to redirect the user
     */
    protected static String getRedirectionUrl(final String clientName, final String targetUrl) {
        // get or create session id
        String sessionId = StorageHelper.getOrCreationSessionId(session());
        // requested url to save
        final String requestedUrlToSave = CallbackController.defaultUrl(targetUrl, request().uri());
        logger.debug("requestedUrlToSave : {}", requestedUrlToSave);
        StorageHelper.saveRequestedUrl(sessionId, clientName, requestedUrlToSave);
        // clients
        Clients clients = Config.getClients();
        // no clients -> misconfiguration ?
        if (clients == null) {
            throw new TechnicalException("No client defined. Use Config.setClients(clients)");
        }
        // redirect to the provider for authentication
        JavaWebContext webContext = new JavaWebContext(request(), response(), session());
        String redirectionUrl = null;
        try {
            redirectionUrl = clients.findClient(clientName).getRedirectionUrl(webContext, false, false);
        } catch (RequiresHttpAction e) {
            // should not happen
        }
        logger.debug("redirectionUrl : {}", redirectionUrl);
        return redirectionUrl;
    }
    
    /**
     * This method returns the user profile if the user is authenticated or <code>null</code> otherwise.
     * 
     * @return the user profile if the user is authenticated or <code>null</code> otherwise
     */
    protected static CommonProfile getUserProfile() {
        // get the session id
        final String sessionId = session(Constants.SESSION_ID);
        logger.debug("sessionId for profile : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            // get the user profile
            final CommonProfile profile = StorageHelper.getProfile(sessionId);
            logger.debug("profile : {}", profile);
            return profile;
        }
        return null;
    }
}
