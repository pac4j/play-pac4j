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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.play.CallbackController;
import org.pac4j.play.Config;
import org.pac4j.play.Constants;
import org.pac4j.play.StorageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.SimpleResult;

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

    private static final Method isAjaxMethod;
    
    private static final Method requireAnyRoleMethod;
    
    private static final Method requireAllRolesMethod;

    static {
        try {
            clientNameMethod = RequiresAuthentication.class.getDeclaredMethod(Constants.CLIENT_NAME);
            targetUrlMethod = RequiresAuthentication.class.getDeclaredMethod(Constants.TARGET_URL);
            isAjaxMethod = RequiresAuthentication.class.getDeclaredMethod(Constants.IS_AJAX);
            requireAnyRoleMethod = RequiresAuthentication.class.getDeclaredMethod(Constants.REQUIRE_ANY_ROLE);
            requireAllRolesMethod = RequiresAuthentication.class.getDeclaredMethod(Constants.REQUIRE_ALL_ROLES);
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Promise<Result> call(final Context context) throws Throwable {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(this.configuration);
        final String clientName = (String) invocationHandler.invoke(this.configuration, clientNameMethod, null);
        logger.debug("clientName : {}", clientName);
        final String targetUrl = (String) invocationHandler.invoke(this.configuration, targetUrlMethod, null);
        logger.debug("targetUrl : {}", targetUrl);
        final Boolean isAjax = (Boolean) invocationHandler.invoke(this.configuration, isAjaxMethod, null);
        logger.debug("isAjax : {}", isAjax);
        final String requireAnyRole = (String) invocationHandler.invoke(this.configuration, requireAnyRoleMethod, null);
        logger.debug("requireAnyRole : {}", requireAnyRole);
        final String requireAllRoles = (String) invocationHandler.invoke(this.configuration, requireAllRolesMethod, null);
        logger.debug("requireAllRoles : {}", requireAllRoles);

        // get or create session id
        final String sessionId = StorageHelper.getOrCreationSessionId(context.session());
        logger.debug("sessionId : {}", sessionId);
        final CommonProfile profile = StorageHelper.getProfile(sessionId);
        logger.debug("profile : {}", profile);

        // has a profile
        if (profile != null) {
            boolean access = true;
            if (requireAnyRole != null) {
                final String[] expectedRoles = StringUtils.split(requireAnyRole, ",");
                // not the expected role -> 403
                if (!profile.hasAnyRole(expectedRoles)) {
                    access = false;
                }
            } else if (requireAllRoles != null) {
                final String[] expectedRoles = StringUtils.split(requireAllRoles, ",");
                // not all the expected roles -> 403
                if (!profile.hasAllRoles(expectedRoles)) {
                    access = false;
                }                
            }
            if (access) {
                return this.delegate.call(context);
            } else {
                return Promise.promise(new Function0<Result>() {
                    public Result apply() {
                        return forbidden(Config.getErrorPage403()).as(Constants.HTML_CONTENT_TYPE);
                    }
                });
            }
        }

        // requested url to save
        final String requestedUrlToSave = CallbackController.defaultUrl(targetUrl, context.request().uri());
        logger.debug("requestedUrlToSave : {}", requestedUrlToSave);
        StorageHelper.saveRequestedUrl(sessionId, clientName, requestedUrlToSave);
        // get client
        final Client<Credentials, UserProfile> client = Config.getClients().findClient(clientName);
        logger.debug("client : {}", client);
        Promise<Result> promise = Promise.promise(new Function0<Result>() {
            @SuppressWarnings("rawtypes")
            public Result apply() {
                try {
                    // and compute redirection url
                    JavaWebContext webContext = new JavaWebContext(context.request(), context.response(), context
                            .session());
                    final RedirectAction action = ((BaseClient) client).getRedirectAction(webContext, true, isAjax);
                    logger.debug("redirectAction : {}", action);
                    return convertToPromise(action);
                } catch (final RequiresHttpAction e) {
                    // requires some specific HTTP action
                    final int code = e.getCode();
                    logger.debug("requires HTTP action : {}", code);
                    if (code == HttpConstants.UNAUTHORIZED) {
                        return unauthorized(Config.getErrorPage401()).as(Constants.HTML_CONTENT_TYPE);
                    } else if (code == HttpConstants.FORBIDDEN) {
                        return forbidden(Config.getErrorPage403()).as(Constants.HTML_CONTENT_TYPE);
                    }
                    final String message = "Unsupported HTTP action : " + code;
                    logger.error(message);
                    throw new TechnicalException(message);
                }
            }
        });
        return promise;
    }

    private Result convertToPromise(RedirectAction action) {
        switch (action.getType()) {
        case REDIRECT:
            return redirect(action.getLocation());
        case SUCCESS:
            return ok(action.getContent()).as(Constants.HTML_CONTENT_TYPE);
        default:
            throw new TechnicalException("Unsupported RedirectAction type " + action.getType());
        }
    }
}
