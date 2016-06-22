package org.pac4j.play.store;

import org.pac4j.core.context.session.SessionStore;
import org.pac4j.play.PlayWebContext;

/**
 *
 * To store data in session.
 * Extending the SessionStore is necessary for dependency injection to work.
 * @author Ilias Elkhalloufi & Wessel Bakker
 * @since 1.9.0
 *
 */
public interface PlaySessionStore extends SessionStore<PlayWebContext> {

}
