package org.pac4j.play.context;

import org.pac4j.core.context.FrameworkParameters;
import org.pac4j.core.context.WebContextFactory;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.play.PlayWebContext;

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
    public PlayWebContext newContext(final FrameworkParameters parameters) {
        if (parameters instanceof PlayFrameworkParameters playFrameworkParameters) {
            if (playFrameworkParameters.getJavaRequest() != null) {
                return new PlayWebContext(playFrameworkParameters.getJavaRequest());
            } else {
                return new PlayWebContext(playFrameworkParameters.getScalaRequest());
            }
        }
        throw new TechnicalException("Bad parameter type");
    }
}
