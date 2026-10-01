package org.pac4j.framework.adapter;

import lombok.val;
import org.pac4j.core.adapter.DefaultFrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.context.PlayContextFactory;
import org.pac4j.play.http.PlayHttpActionAdapter;

/**
 * Play framework adapter.
 *
 * @author Jerome LELEU
 * @since 12.0.0
 */
public class FrameworkAdapterImpl extends DefaultFrameworkAdapter {

    @Override
    public void applyDefaultSettingsIfUndefined(final Config config) {
        CommonHelper.assertNotNull("config", config);

        config.setWebContextFactoryIfUndefined(PlayContextFactory.INSTANCE);
        if (config.getSessionStoreFactory() == null) {
            val message = "Please create a SessionStore and define it in the config: " +
                          "'config.setSessionStoreFactory(p -> mySessionStore);' in Java or " +
                          "'config.setSessionStoreFactory(new SessionStoreFactory { override def " +
                          " newSessionStore(parameters: FrameworkParameters): SessionStore = mySessionStore });' in Scala!";
            throw new TechnicalException(message);
        }
        config.setHttpActionAdapterIfUndefined(PlayHttpActionAdapter.INSTANCE);

        super.applyDefaultSettingsIfUndefined(config);
    }

    @Override
    public String toString() {
        return "Play";
    }
}
