package org.pac4j.play.config;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.FrameworkParameters;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.context.session.SessionStoreFactory;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.context.PlayContextFactory;
import org.pac4j.play.http.PlayHttpActionAdapter;

/**
 * Play framework specificities.
 *
 * @author Jerome LELEU
 * @since 12.0.0
 */
public class Pac4jPlayConfig {

    /**
     * Apply the default Play settings if they are not already defined in the configuration.
     *
     * @param config the config
     * @param sessionStore the session store
     */
    public static void applyPlaySettingsIfUndefined(final Config config, final SessionStore sessionStore) {
        CommonHelper.assertNotNull("config", config);
        CommonHelper.assertNotNull("sessionStore", sessionStore);
        config.setWebContextFactoryIfUndefined(PlayContextFactory.INSTANCE);
        config.setSessionStoreFactoryIfUndefined(new SessionStoreFactory() {
            @Override
            public SessionStore newSessionStore(FrameworkParameters frameworkParameters) {
                return sessionStore;
            }
        });
        config.setHttpActionAdapterIfUndefined(PlayHttpActionAdapter.INSTANCE);
    }
}
