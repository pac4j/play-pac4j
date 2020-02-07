package org.pac4j.play.http;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.http.HttpEntity;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * Specific {@link HttpActionAdapter} for Play.
 *
 * @author Jerome Leleu
 * @since 7.0.0
 */
public class PlayHttpActionAdapter implements HttpActionAdapter<Result, PlayWebContext> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final PlayHttpActionAdapter INSTANCE = new PlayHttpActionAdapter();

    @Override
    public Result adapt(final HttpAction action, final PlayWebContext context) {
        if (action != null) {
            int code = action.getCode();
            logger.debug("requires HTTP action: {}", code);
            Map<String, String> headers = new HashMap<>();
            HttpEntity httpEntity = HttpEntity.NO_ENTITY;

            if (action instanceof WithLocationAction) {
                final WithLocationAction withLocationAction = (WithLocationAction) action;
                headers.put(HttpConstants.LOCATION_HEADER, withLocationAction.getLocation());
            } else if (action instanceof WithContentAction) {
                final WithContentAction withContentAction = (WithContentAction) action;
                final String content = withContentAction.getContent();

                if (content != null) {
                    logger.debug("render: {}", content);
                    httpEntity = HttpEntity.fromString(content, "UTF-8");
                }
            }

            return new Result(code, headers, httpEntity);
        }

        throw new TechnicalException("No action provided");
    }
}
