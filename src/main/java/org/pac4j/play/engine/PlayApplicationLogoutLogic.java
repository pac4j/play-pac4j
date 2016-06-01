package org.pac4j.play.engine;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.engine.DefaultApplicationLogoutLogic;
import org.pac4j.play.PlayWebContext;
import play.mvc.Result;

/**
 * Specific application logout logic for Play.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
public class PlayApplicationLogoutLogic extends DefaultApplicationLogoutLogic<Result, PlayWebContext> {

    @Override
    protected void postLogout(final PlayWebContext context) {
        context.getJavaContext().session().remove(Pac4jConstants.SESSION_ID);
    }
}
