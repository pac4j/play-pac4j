package org.pac4j.play.store;

import org.pac4j.core.context.session.SessionStore;
import org.pac4j.play.PlayWebContext;

/**
 * To store data in session.
 * Extending the SessionStore is necessary for dependency injection to work.
 * @author Ilias Elkhalloufi and Wessel Bakker
 * @since 2.5.0
 */
public interface PlaySessionStore extends SessionStore<PlayWebContext> {
}
