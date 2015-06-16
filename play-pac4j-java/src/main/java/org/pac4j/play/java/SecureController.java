package org.pac4j.play.java;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.CallbackController;
import org.pac4j.play.Config;
import org.pac4j.play.SecurityCallbackController;
import org.pac4j.play.StorageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is the Java controller to retrieve the user profile or the redirection url to start the authentication process.
 *
 * @author Hugo Valk
 * @since 1.5.0
 */
public class SecureController extends SecurityCallbackController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaController.class);

    /**
     * This method returns the url of the provider where the user must be redirected for authentication.
     * The current requested url is saved into session to be restored after authentication.
     *
     * @param clientName
     * @return the url of the provider where to redirect the user
     */
    protected RedirectAction getRedirectAction(final String clientName) {
        return getRedirectAction(clientName, null);
    }

    /**
     * This method returns the url of the provider where the user must be redirected for authentication.
     * The input <code>targetUrl</code> (or the current requested url if <code>null</code>) is saved into session to be restored after
     * authentication.
     *
     * @param clientName
     * @param targetUrl
     * @return the url of the provider where to redirect the user
     */
    protected RedirectAction getRedirectAction(final String clientName, final String targetUrl) {
        // get or create session id
        String sessionId = StorageHelper.getOrCreationSessionId(session());
        // requested url to save
        final String requestedUrlToSave = CallbackController.defaultUrl(targetUrl, request().uri());
        LOGGER.debug("requestedUrlToSave : {}", requestedUrlToSave);
        StorageHelper.saveRequestedUrl(sessionId, clientName, requestedUrlToSave);
        // clients
        Clients clients = Config.getClients();
        // no clients -> misconfiguration ?
        if (clients == null) {
            throw new TechnicalException("No client defined. Use Config.setClients(clients)");
        }
        // redirect to the provider for authentication
        JavaWebContext webContext = new JavaWebContext(request(), response(), session());
        RedirectAction action = null;
        try {
            action = ((BaseClient) clients.findClient(clientName)).getRedirectAction(webContext, false, false);
        } catch (RequiresHttpAction e) {
            // should not happen
        }
        LOGGER.debug("redirectAction : {}", action);
        return action;
    }

    /**
     * This method returns the user profile if the user is authenticated or <code>null</code> otherwise.
     *
     * @return the user profile if the user is authenticated or <code>null</code> otherwise
     */
    protected CommonProfile getUserProfile() {
        // get the session id
        final String sessionId = session(Pac4jConstants.SESSION_ID);
        LOGGER.debug("sessionId for profile : {}", sessionId);
        CommonProfile profile = null;
        if (StringUtils.isNotBlank(sessionId)) {
            // get the user profile
            profile = StorageHelper.getProfile(sessionId);
        }
        if (profile == null) {
            // Try to get the User Profile from the current request (stateless flow)
            return (CommonProfile) ctx().args.get(Pac4jConstants.USER_PROFILE);
        } else {
            return profile;
        }
    }
}
