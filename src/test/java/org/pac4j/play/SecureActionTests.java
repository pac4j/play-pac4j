package org.pac4j.play;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.TestsHelper;
import org.pac4j.play.java.SecureAction;

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
    }

    private void call() throws Exception {
        try {
            action.internalCall(ctx, clients, authorizers, multiProfile);
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
}
