package org.pac4j.play;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.TestsHelper;
import play.mvc.Result;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ApplicationLogoutController}.
 *
 * @author Jerome Leleu
 * @since 2.2.0
 */
public final class ApplicationLogoutControllerTests extends AbstractWebTests {

    private ApplicationLogoutController controller;

    @Before
    public void setUp() {
        super.setUp();
        controller = new ApplicationLogoutController();
        controller.setConfig(config);
    }

    private Result call() {
        return controller.logout();
    }

    @Test
    public void testMissingConfig() {
        controller.setConfig(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "config cannot be null");
    }

    @Test
    public void testBlankLogoutUrlPattern() {
        controller.setLogoutUrlPattern("");
        TestsHelper.expectException(() -> call(), TechnicalException.class, "logoutUrlPattern cannot be blank");
    }

    @Test
    public void testLogout() throws IOException {
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, new CommonProfile());
        webContext.setRequestAttribute(Pac4jConstants.USER_PROFILES, profiles);
        webContext.setSessionAttribute(Pac4jConstants.USER_PROFILES, profiles);
        final Result result = call();
        assertEquals(200, result.status());
        assertEquals("", getBody(result));
        final LinkedHashMap<String, CommonProfile> profiles2 = (LinkedHashMap<String, CommonProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(0, profiles2.size());
        final LinkedHashMap<String, CommonProfile> profiles3 = (LinkedHashMap<String, CommonProfile>) webContext.getSessionAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(0, profiles3.size());
    }

    @Test
    public void testLogoutWithDefaultUrl() {
        controller.setDefaultUrl(CALLBACK_URL);
        final Result result = call();
        assertEquals(303, result.status());
        assertEquals(CALLBACK_URL, result.redirectLocation());
    }


    @Test
    public void testLogoutWithGoodUrl() {
        requestParameters.put(Pac4jConstants.URL, new String[] { PATH });
        final Result result = call();
        assertEquals(303, result.status());
        assertEquals(PATH, result.redirectLocation());
    }

    @Test
    public void testLogoutWithBadUrlNoDefaultUrl() throws IOException {
        requestParameters.put(Pac4jConstants.URL, new String[] { PATH });
        controller.setLogoutUrlPattern(VALUE);
        final Result result = call();
        assertEquals(200, result.status());
        assertEquals("", getBody(result));
    }

    @Test
    public void testLogoutWithBadUrlButDefaultUrl() {
        requestParameters.put(Pac4jConstants.URL, new String[] { PATH });
        controller.setLogoutUrlPattern(VALUE);
        controller.setDefaultUrl(CALLBACK_URL);
        final Result result = call();
        assertEquals(303, result.status());
        assertEquals(CALLBACK_URL, result.redirectLocation());
    }
}
