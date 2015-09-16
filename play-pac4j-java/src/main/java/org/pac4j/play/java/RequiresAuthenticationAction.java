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

import org.pac4j.core.authorization.AuthorizationChecker;
import org.pac4j.core.authorization.DefaultAuthorizationChecker;
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
import org.pac4j.play.http.HttpActionAdapter;
import org.pac4j.play.store.DataStore;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import play.mvc.Result;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * <p>This filter protects a resource (authentication + authorization).</p>
 * <ul>
 *  <li>If a stateful / indirect client is used, it relies on the session to get the user profile (after the {@link org.pac4j.play.CallbackController} has terminated the authentication process)</li>
 *  <li>If a stateless / direct client is used, it validates the provided credentials from the request and retrieves the user profile if the authentication succeeds.</li>
 * </ul>
 * <p>Then, authorizations are checked before accessing the resource.</p>
 * <p>Forbidden or unauthorized errors can be returned. An authentication process can be started (redirection to the identity provider) in case of an indirect client.</p>
 * <p>The configuration can be provided via annotation parameters: <code>clientName</code> and <code>authorizerName</code>.</p>
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
    protected HttpActionAdapter httpActionAdapter;

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizationChecker authorizationChecker = new DefaultAuthorizationChecker();

    public RequiresAuthenticationAction() {
    }

    public RequiresAuthenticationAction(final Config config, final DataStore dataStore, final HttpActionAdapter httpActionAdapter) {
        this.config = config;
        this.dataStore = dataStore;
        this.httpActionAdapter = httpActionAdapter;
    }

    @Override
    public Promise<Result> call(final Context ctx) throws Throwable {

        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(configuration);
        final String clientName = getStringParam(invocationHandler, CLIENT_NAME_METHOD, null);
        final String authorizerName = getStringParam(invocationHandler, AUTHORIZER_NAME_METHOD, null);

        return internalCall(ctx, clientName, authorizerName);
    }

    public Promise<Result> internalCall(final Context ctx, final String clientName, final String authorizerName) throws Throwable {

        final PlayWebContext context =  new PlayWebContext(ctx, dataStore);

        CommonHelper.assertNotNull("config", config);
        final Clients clients = config.getClients();
        CommonHelper.assertNotNull("clients", clients);
        logger.debug("clientName: {}", clientName);
        final Client client = clientFinder.find(clients, context, clientName);
        logger.debug("client: {}", client);

        final boolean useSession = useSession(context, client);
        logger.debug("useSession: {}", useSession);

        final Promise<UserProfile> promiseProfile = Promise.promise(() -> {

            final ProfileManager manager = new ProfileManager(context);
            UserProfile profile = manager.get(useSession);
            logger.debug("profile: {}", profile);

            if (profile == null && client instanceof DirectClient) {
                final Credentials credentials;
                try {
                    credentials = client.getCredentials(context);
                    logger.debug("credentials: {}", credentials);
                } catch (final RequiresHttpAction e) {
                    throw new TechnicalException("Unexpected HTTP action", e);
                }
                profile = client.getUserProfile(credentials, context);
                logger.debug("profile: {}", profile);
                if (profile != null) {
                    manager.save(useSession, profile);
                }
            }

            return profile;
        });

        return promiseProfile.flatMap(new Function<UserProfile, Promise<Result>>() {

            @Override
            public Promise<Result> apply(UserProfile profile) throws Throwable {
                if (profile != null) {
                    logger.debug("authorizerName: {}", authorizerName);
                    if (authorizationChecker.isAuthorized(context, profile, authorizerName, config.getAuthorizers())) {
                        // when called from Scala
                        if (delegate == null) {
                            return Promise.pure(null);
                        } else {
                            return delegate.call(ctx);
                        }
                    } else {
                        return Promise.pure(httpActionAdapter.handle(HttpConstants.FORBIDDEN, context));
                    }
                } else {
                    if (client instanceof IndirectClient) {
                        saveRequestedUrl(context);
                        return Promise.promise(() -> redirectToIdentityProvider(client, context));
                    } else {
                        return Promise.pure(httpActionAdapter.handle(HttpConstants.UNAUTHORIZED, context));
                    }
                }
            }
        });
    }

    protected boolean useSession(final WebContext context, final Client client) {
        return client == null || client instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected Result redirectToIdentityProvider(final Client client, final PlayWebContext context) {
        try {
            final RedirectAction action = ((IndirectClient) client).getRedirectAction(context, true);
            logger.debug("redirectAction: {}", action);
            return httpActionAdapter.handleRedirect(action);
        } catch (final RequiresHttpAction e) {
            return httpActionAdapter.handle(e.getCode(), context);
        }
    }
}
