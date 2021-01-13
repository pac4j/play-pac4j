package org.pac4j.play.context;

import org.pac4j.core.context.WebContextFactory;
import org.pac4j.play.PlayWebContext;
import play.mvc.Http;

/**
 * Build a Play context from parameters.
 *
 * @author Jerome LELEU
 * @since 11.0.0
 */
public class PlayContextFactory implements WebContextFactory {

    public static final PlayContextFactory INSTANCE = new PlayContextFactory();

    protected PlayContextFactory() {}

    @Override
    public PlayWebContext newContext(final Object... parameters) {
        return new PlayWebContext((Http.Request) parameters[0]);
    }
}
