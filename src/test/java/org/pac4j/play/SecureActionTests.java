package org.pac4j.play;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.client.*;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.MockCredentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.TestsHelper;
import org.pac4j.play.java.SecureAction;
import play.mvc.Result;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Tests {@link SecureAction}.
 *
 * @author Jerome Leleu
 * @since 2.2.0
 */
public final class SecureActionTests extends AbstractWebTests {

    private SecureAction action;

    private String clients;

    private String authorizers;

    private boolean multiProfile;

    @Before
    public void setUp() {
        super.setUp();
        action = new SecureAction(config);
        clients = null;
        authorizers = null;
        multiProfile = false;
    }

    private Result call() throws Exception {
        try {
            return action.internalCall(ctx, clients, authorizers, multiProfile).get(1L, TimeUnit.SECONDS);
        } catch (final Exception e) {
            throw e;
        } catch (final Throwable error) {
            throw new RuntimeException(error);
        }
    }

    @Test
    public void testMissingConfig() throws Exception {
        action.setConfig(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "config cannot be null");
    }

    @Test
    public void testMissingHttpActionAdapter() throws Exception {
        config.setHttpActionAdapter(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "config.httpActionAdapter cannot be null");
    }

    @Test
    public void testMissingClients() throws Exception {
        config.setClients(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "configClients cannot be null");
    }

    @Test
    public void testNotAuthenticated() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        final Result result = call();
        assertEquals(401, result.status());
    }

    @Test
    public void testAlreadyAuthenticatedAndAuthorized() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(ID);
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, profile);
        webContext.setSessionAttribute(Pac4jConstants.USER_PROFILES, profiles);
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        authorizers = NAME;
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        config.addAuthorizer(NAME, (context, prof) -> ID.equals(((CommonProfile) prof.get(0)).getId()));
        final Result result = call();
        assertNull(result);
    }

    @Test
    public void testAlreadyAuthenticatedNotAuthorized() throws Exception {
        final CommonProfile profile = new CommonProfile();
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, profile);
        webContext.setSessionAttribute(Pac4jConstants.USER_PROFILES, profiles);
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        authorizers = NAME;
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        config.addAuthorizer(NAME, (context, prof) -> ID.equals(((CommonProfile) prof.get(0)).getId()));
        final Result result = call();
        assertEquals(403, result.status());
    }

    @Test
    public void testAuthorizerThrowsRequiresHttpAction() throws Exception {
        final CommonProfile profile = new CommonProfile();
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, profile);
        webContext.setSessionAttribute(Pac4jConstants.USER_PROFILES, profiles);
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        authorizers = NAME;
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        config.addAuthorizer(NAME, (context, prof) -> { throw RequiresHttpAction.status("bad request", 400, webContext); } );
        final Result result = call();
        assertEquals(400, result.status());
    }

    @Test
    public void testDoubleDirectClient() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final CommonProfile profile2 = new CommonProfile();
        profile2.setId(VALUE);
        final DirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), profile);
        final DirectClient directClient2 = new MockDirectClient(VALUE, new MockCredentials(), profile2);
        config.setClients(new Clients(CALLBACK_URL, directClient, directClient2));
        clients = NAME + "," + VALUE;
        final Result result = call();
        assertNull(result);
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(1, profiles.size());
        assertTrue(profiles.containsValue(profile));
    }

    @Test
    public void testDirectClientThrowsRequiresHttpAction() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final DirectClient directClient = new MockDirectClient(NAME, () -> { throw RequiresHttpAction.status("bad request", 400, webContext); }, profile);
        config.setClients(new Clients(CALLBACK_URL, directClient));
        clients = NAME;
        final Result result = call();
        assertEquals(400, result.status());
    }

    @Test
    public void testDoubleDirectClientSupportingMultiProfile() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final CommonProfile profile2 = new CommonProfile();
        profile2.setId(VALUE);
        final DirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), profile);
        final DirectClient directClient2 = new MockDirectClient(VALUE, new MockCredentials(), profile2);
        config.setClients(new Clients(CALLBACK_URL, directClient, directClient2));
        clients = NAME + "," + VALUE;
        multiProfile = true;
        final Result result = call();
        assertNull(result);
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(2, profiles.size());
        assertTrue(profiles.containsValue(profile));
        assertTrue(profiles.containsValue(profile2));
    }

    @Test
    public void testDoubleDirectClientChooseDirectClient() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final CommonProfile profile2 = new CommonProfile();
        profile2.setId(VALUE);
        final DirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), profile);
        final DirectClient directClient2 = new MockDirectClient(VALUE, new MockCredentials(), profile2);
        config.setClients(new Clients(CALLBACK_URL, directClient, directClient2));
        clients = NAME + "," + VALUE;
        requestParameters.put(Clients.DEFAULT_CLIENT_NAME_PARAMETER, new String[] { VALUE });
        multiProfile = true;
        final Result result = call();
        assertNull(result);
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(1, profiles.size());
        assertTrue(profiles.containsValue(profile2));
    }

    @Test
    public void testDoubleDirectClientChooseBadDirectClient() {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final CommonProfile profile2 = new CommonProfile();
        profile2.setId(VALUE);
        final DirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), profile);
        final DirectClient directClient2 = new MockDirectClient(VALUE, new MockCredentials(), profile2);
        config.setClients(new Clients(CALLBACK_URL, directClient, directClient2));
        clients = NAME;
        requestParameters.put(Clients.DEFAULT_CLIENT_NAME_PARAMETER, new String[] { VALUE });
        multiProfile = true;
        TestsHelper.expectException(() -> call(), TechnicalException.class, "Client not allowed: " + VALUE);
    }

    @Test
    public void testRedirectByIndirectClient() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, RedirectAction.redirect(PAC4J_URL), new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        clients = NAME;
        final Result result = call();
        assertEquals(303, result.status());
        assertEquals(PAC4J_URL, result.redirectLocation());
    }

    @Test
    public void testDoubleIndirectClientOneChosen() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, RedirectAction.redirect(PAC4J_URL), new MockCredentials(), new CommonProfile());
        final IndirectClient indirectClient2 = new MockIndirectClient(VALUE, RedirectAction.redirect(PAC4J_BASE_URL), new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient, indirectClient2));
        clients = NAME + "," + VALUE;
        requestParameters.put(Clients.DEFAULT_CLIENT_NAME_PARAMETER, new String[] { VALUE });
        final Result result = call();
        assertEquals(303, result.status());
        assertEquals(PAC4J_BASE_URL, result.redirectLocation());
    }

    @Test
    public void testDoubleIndirectClientBadOneChosen() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, RedirectAction.redirect(PAC4J_URL), new MockCredentials(), new CommonProfile());
        final IndirectClient indirectClient2 = new MockIndirectClient(VALUE, RedirectAction.redirect(PAC4J_BASE_URL), new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient, indirectClient2));
        clients = NAME;
        requestParameters.put(Clients.DEFAULT_CLIENT_NAME_PARAMETER, new String[] { VALUE });
        TestsHelper.expectException(() -> call(), TechnicalException.class, "Client not allowed: " + VALUE);
    }
}
