package org.pac4j.play.http;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static play.mvc.Results.*;

/**
 * Default implementation.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public class DefaultHttpActionAdapter implements HttpActionAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object adapt(final int code, final WebContext context) {
        final PlayWebContext webContext = (PlayWebContext) context;
        logger.debug("requires HTTP action: {}", code);
        if (code == HttpConstants.UNAUTHORIZED) {
            return unauthorized("authentication required");
        } else if (code == HttpConstants.FORBIDDEN) {
            return forbidden("forbidden");
        } else if (code == HttpConstants.TEMP_REDIRECT) {
            return redirect(webContext.getResponseLocation());
        } else if (code == HttpConstants.OK) {
            final String content = webContext.getResponseContent();
            logger.debug("render: {}", content);
            return ok(content).as(HttpConstants.HTML_CONTENT_TYPE);
        }
        final String message = "Unsupported HTTP action: " + code;
        logger.error(message);
        throw new TechnicalException(message);
    }
}
