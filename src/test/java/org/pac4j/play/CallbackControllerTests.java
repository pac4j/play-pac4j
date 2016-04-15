package org.pac4j.play;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.MockDirectClient;
import org.pac4j.core.client.MockIndirectClient;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.MockCredentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.TestsHelper;
import org.pac4j.play.http.DefaultHttpActionAdapter;
import play.mvc.Result;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * Tests {@link CallbackController}.
 *
 * @author Jerome Leleu
 * @since 2.2.0
 */
public final class CallbackControllerTests extends AbstractWebTests {

    private CallbackController controller;

    private CommonProfile profile;

    @Before
    public void setUp() {
        super.setUp();
        controller = new CallbackController();
        controller.setConfig(config);
        profile = new CommonProfile();
        config.setClients(new Clients(CALLBACK_URL, new MockIndirectClient(NAME, null, new MockCredentials(), profile)));
        config.setHttpActionAdapter(new DefaultHttpActionAdapter());
        requestParameters.put(Clients.DEFAULT_CLIENT_NAME_PARAMETER, new String[] { NAME });
    }

    private Result call() {
        return controller.callback();
    }

    @Test
    public void testMissingConfig() throws Exception {
        controller.setConfig(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "config cannot be null");
    }

    @Test
    public void testMissingClients() throws Exception {
        config.setClients(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "clients cannot be null");
    }

    @Test
    public void testBlankDefaultUrl() throws Exception {
        controller.setDefaultUrl("");
        TestsHelper.expectException(() -> call(), TechnicalException.class, "defaultUrl cannot be blank");
    }

    @Test
    public void testDirectClient() throws Exception {
        final MockDirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(directClient));
        TestsHelper.expectException(() -> call(), TechnicalException.class, "only indirect clients are allowed on the callback url");
    }

    @Test
    public void testCallback() throws Exception {
        requestParameters.put(Clients.DEFAULT_CLIENT_NAME_PARAMETER, new String[] { NAME });
        final Result result = call();
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) webContext.getSessionAttribute(Pac4jConstants.USER_PROFILES);
        assertTrue(profiles.containsValue(profile));
        assertEquals(1, profiles.size());
        assertEquals(303, result.status());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, result.redirectLocation());
    }

    @Test
    public void testCallbackWithOriginallyRequestedUrl() throws Exception {
        webContext.setSessionAttribute(Pac4jConstants.REQUESTED_URL, PAC4J_URL);
        requestParameters.put(Clients.DEFAULT_CLIENT_NAME_PARAMETER, new String[] { NAME });
        final Result result = call();
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) webContext.getSessionAttribute(Pac4jConstants.USER_PROFILES);
        assertTrue(profiles.containsValue(profile));
        assertEquals(1, profiles.size());
        assertEquals(303, result.status());
        assertEquals(PAC4J_URL, result.redirectLocation());
    }
}
