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

import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.authorization.DefaultAuthorizerBuilder;
import org.pac4j.core.client.*;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;

import org.pac4j.play.PlayWebContext;
import org.pac4j.play.handler.HttpActionHandler;
import org.pac4j.play.store.DataStore;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import play.mvc.Result;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * <p>This action aims to protect a (stateful or stateless) resource.</p>
 * <ul>
 *  <li>If statefull, it relies on the web session and on the {@link org.pac4j.play.CallbackController} to terminate the authentication process</li>
 *  <li>If stateless, it validates the provided credentials and forward the request to the underlying resource if the authentication succeeds.</li>
 * </ul>
 * <p>Authorizations are also handled by this action.</p>
 * <p>The configuration can be provided via annotation parameters: <code>clientName</code>, <code>isAjax</code>, <code>requireAnyRole</code>,
 * <code>requireAllRoles</code>, <code>authorizerName</code>, <code>useSessionForDirectClient</code> and <code>allowDynamicClientSelection</code>.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.0.0
 */
public class RequiresAuthenticationAction extends AbstractConfigAction {

    @Inject
    protected Config config;

    @Inject
    protected DataStore dataStore;

    @Inject
    protected HttpActionHandler httpActionHandler;

    public RequiresAuthenticationAction() {
    }

    public RequiresAuthenticationAction(final Config config, final DataStore dataStore, final HttpActionHandler httpActionHandler) {
        this.config = config;
        this.dataStore = dataStore;
        this.httpActionHandler = httpActionHandler;
    }

    @Override
    public Promise<Result> call(final Context ctx) throws Throwable {

        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(configuration);
        final String clientName = getStringParam(invocationHandler, CLIENT_NAME_METHOD, null);

        final String authorizerName = getStringParam(invocationHandler, AUTHORIZER_NAME_METHOD, null);
        final String requireAnyRole = getStringParam(invocationHandler, REQUIRE_ANY_ROLE_METHOD, null);
        final String requireAllRoles = getStringParam(invocationHandler, REQUIRE_ALL_ROLES_METHOD, null);

        final Boolean isAjax = getBooleanParam(invocationHandler, IS_AJAX_METHOD, false);
        final Boolean useSessionForDirectClient = getBooleanParam(invocationHandler, USE_SESSION_FOR_DIRECT_CLIENT_METHOD, false);
        final Boolean allowDynamicClientSelection = getBooleanParam(invocationHandler, ALLOW_DYNAMIC_CLIENT_SELECTION_METHOD, false);

        return internalCall(ctx, clientName, null, authorizerName, requireAnyRole, requireAllRoles, isAjax, useSessionForDirectClient, allowDynamicClientSelection);
    }

    public Promise<Result> internalCall(final Context ctx, final String clientName, final Authorizer authorizer, final String authorizerName, final String requireAnyRole,
                                final String requireAllRoles, final boolean isAjax, final boolean useSessionForDirectClient, final boolean  allowDynamicClientSelection) throws Throwable {

        logger.debug("clientName: {}", clientName);
        logger.debug("authorizer: {}", authorizer);
        logger.debug("authorizerName: {}", authorizerName);
        logger.debug("requireAnyRole: {}", requireAnyRole);
        logger.debug("requireAllRoles: {}", requireAllRoles);
        logger.debug("isAjax: {}", isAjax);
        logger.debug("useSessionForDirectClient: {}", useSessionForDirectClient);
        logger.debug("allowDynamicClientSelection: {}", allowDynamicClientSelection);

        CommonHelper.assertNotNull(Pac4jConstants.CLIENT_NAME, clientName);

        CommonHelper.assertNotNull("config", config);
        final Clients clients = config.getClients();
        CommonHelper.assertNotNull("clients", clients);
        final Map<String, Authorizer> authorizers = config.getAuthorizers();
        CommonHelper.assertNotNull("authorizers", authorizers);
        final Authorizer computedAuthorizer = DefaultAuthorizerBuilder.build(authorizer, authorizerName, authorizers, requireAnyRole, requireAllRoles);
        CommonHelper.assertNotNull("authorizer", computedAuthorizer);

        final PlayWebContext context = new PlayWebContext(ctx, dataStore);
        final ProfileManager manager = new ProfileManager(context);
        final Client client = findClient(context, clientName, allowDynamicClientSelection);
        logger.debug("client: {}", client);
        final boolean isDirectClient = client instanceof DirectClient;

        final Promise<UserProfile> promiseProfile = Promise.promise(() -> {

            UserProfile profile = manager.get(!isDirectClient || useSessionForDirectClient);
            logger.debug("profile: {}", profile);

            if (profile == null && isDirectClient) {
                final Credentials credentials;
                try {
                    credentials = client.getCredentials(context);
                } catch (final RequiresHttpAction e) {
                    throw new TechnicalException("Unexpected HTTP action", e);
                }
                logger.debug("credentials: {}", credentials);

                profile = client.getUserProfile(credentials, context);
                logger.debug("profile: {}", profile);
                if (profile != null) {
                    manager.save(useSessionForDirectClient, profile);
                }
            }

            return profile;
        });

        return promiseProfile.flatMap(new Function<UserProfile, Promise<Result>>() {

            @Override
            public Promise<Result> apply(UserProfile profile) throws Throwable {
                if (profile != null) {
                    if (computedAuthorizer.isAuthorized(context, profile)) {
                        // when called from Scala
                        if (delegate == null) {
                            return Promise.pure(null);
                        } else {
                            return delegate.call(ctx);
                        }
                    } else {
                        return Promise.pure(httpActionHandler.handle(HttpConstants.FORBIDDEN, context));
                    }
                } else {
                    if (isDirectClient) {
                        return Promise.pure(httpActionHandler.handle(HttpConstants.UNAUTHORIZED, context));
                    } else {
                        saveRequestedUrl(context, isAjax);
                        return Promise.promise(() -> redirectToIdentityProvider(client, context, isAjax));
                    }
                }
            }
        });
    }

    protected Client findClient(final WebContext context, final String clientName, final boolean allowDynamicClientSelection) {
        final Clients clients = config.getClients();
        Client client = null;
        if (allowDynamicClientSelection) {
            client = clients.findClient(context);
        }
        if (client == null) {
            client = clients.findClient(clientName);
        }
        return client;
    }

    protected void saveRequestedUrl(final WebContext context, final boolean isAjax) {
        if (!isAjax) {
            final String requestedUrl = context.getFullRequestURL();
            logger.debug("requestedUrl: {}", requestedUrl);
            context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
        }
    }

    protected Result redirectToIdentityProvider(final Client client, final PlayWebContext context, final boolean isAjax) {
        try {
            final RedirectAction action = ((IndirectClient) client).getRedirectAction(context, true, isAjax);
            logger.debug("redirectAction: {}", action);
            return httpActionHandler.handleRedirect(action);
        } catch (final RequiresHttpAction e) {
            return httpActionHandler.handle(e.getCode(), context);
        }
    }
}
