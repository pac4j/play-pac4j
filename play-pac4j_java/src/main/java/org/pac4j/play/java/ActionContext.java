/*
  Copyright 2012 - 2015 pac4j organization

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

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.play.Config;
import org.pac4j.play.StorageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http.Context;
import play.mvc.Http.Request;

/**
 * <p>Context for the {@link RequiresAuthenticationAction} aggregating the Play {@link Context} and the {@link RequiresAuthentication} annotation.</p>
 * 
 * @author Michael Remond
 * @since 1.4.0
 */
public final class ActionContext {

    private static final Logger logger = LoggerFactory.getLogger(ActionContext.class);

    private static final Method clientNameMethod;

    private static final Method targetUrlMethod;

    private static final Method statelessMethod;

    private static final Method isAjaxMethod;

    private static final Method requireAnyRoleMethod;

    private static final Method requireAllRolesMethod;

    static {
        try {
            clientNameMethod = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.CLIENT_NAME);
            targetUrlMethod = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.TARGET_URL);
            statelessMethod = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.STATELESS);
            isAjaxMethod = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.IS_AJAX);
            requireAnyRoleMethod = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.REQUIRE_ANY_ROLE);
            requireAllRolesMethod = RequiresAuthentication.class.getDeclaredMethod(Pac4jConstants.REQUIRE_ALL_ROLES);
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /* The Play context */
    private final Context ctx;
    /* The play request */
    private final Request request;
    /* The sessionId used to retrieve the session attributes */
    private final String sessionId;
    /* The Play Pac4j Context */
    private final JavaWebContext webContext;
    /* The client name */
    private final String clientName;
    /* The target url parameter */
    private final String targetUrl;
    /* The isAjax parameter */
    private final boolean isAjax;
    /* The stateless parameter */
    private final boolean stateless;
    /* The requireAnyRole parameter */
    private final String requireAnyRole;
    /* The requireAllRoles parameter */
    private final String requireAllRoles;

    /**
     * Private constructor.
     * 
     */
    private ActionContext(Context ctx, Request request, String sessionId, JavaWebContext webContext, String clientName,
            String targetUrl, boolean isAjax, Boolean stateless, String requireAnyRole, String requireAllRoles) {
        this.ctx = ctx;
        this.request = request;
        this.sessionId = sessionId;
        this.webContext = webContext;
        this.clientName = clientName;
        this.targetUrl = targetUrl;
        this.isAjax = isAjax;
        this.stateless = stateless;
        this.requireAnyRole = requireAnyRole;
        this.requireAllRoles = requireAllRoles;
    }

    public Context getCtx() {
        return ctx;
    }

    public Request getRequest() {
        return request;
    }

    public String getSessionId() {
        return sessionId;
    }

    public JavaWebContext getWebContext() {
        return webContext;
    }

    public String getClientName() {
        return clientName;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public boolean isAjax() {
        return isAjax;
    }

    public boolean isStateless() {
        return stateless;
    }

    public String getRequireAnyRole() {
        return requireAnyRole;
    }

    public String getRequireAllRoles() {
        return requireAllRoles;
    }

    /**
     * Build the action context from the Play {@link Context} and the {@link RequiresAuthentication} annotation.
     * 
     * @param ctx
     * @param configuration
     * @return
     */
    public static ActionContext build(Context ctx, Object configuration) {
        JavaWebContext context = new JavaWebContext(ctx.request(), ctx.response(), ctx.session());
        String clientName = null;
        String targetUrl = "";
        Boolean isAjax = false;
        Boolean stateless = false;
        String requireAnyRole = "";
        String requireAllRoles = "";

        if (configuration != null) {
            try {
                final InvocationHandler invocationHandler = Proxy.getInvocationHandler(configuration);
                clientName = (String) invocationHandler.invoke(configuration, clientNameMethod, null);
                targetUrl = (String) invocationHandler.invoke(configuration, targetUrlMethod, null);
                logger.debug("targetUrl : {}", targetUrl);
                isAjax = (Boolean) invocationHandler.invoke(configuration, isAjaxMethod, null);
                logger.debug("isAjax : {}", isAjax);
                stateless = (Boolean) invocationHandler.invoke(configuration, statelessMethod, null);
                logger.debug("stateless : {}", stateless);
                requireAnyRole = (String) invocationHandler.invoke(configuration, requireAnyRoleMethod, null);
                logger.debug("requireAnyRole : {}", requireAnyRole);
                requireAllRoles = (String) invocationHandler.invoke(configuration, requireAllRolesMethod, null);
                logger.debug("requireAllRoles : {}", requireAllRoles);
            } catch (Throwable e) {
                logger.error("Error during configuration retrieval", e);
                throw new TechnicalException(e);
            }
        }
        clientName = (clientName != null) ? clientName : context.getRequestParameter(Config.getClients()
                .getClientNameParameter());
        logger.debug("clientName : {}", clientName);
        String sessionId = (stateless) ? null : StorageHelper.getOrCreationSessionId(ctx.session());

        return new ActionContext(ctx, ctx.request(), sessionId, context, clientName, targetUrl, isAjax, stateless,
                requireAnyRole, requireAllRoles);
    }

}
