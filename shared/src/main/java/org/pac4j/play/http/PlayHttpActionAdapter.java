package org.pac4j.play.http;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.CommonHelper;
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

    protected Map<Integer, Result> results = new HashMap<>();

    @Override
    public Result adapt(final HttpAction action, final PlayWebContext context) {
        if (action != null) {
            final int code = action.getCode();
            logger.debug("requires HTTP action: {}", code);

            final Result predefinedResult = results.get(code);
            if (predefinedResult != null) {
                logger.debug("using pre-defined result: {}", predefinedResult);
                return predefinedResult;
            }

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

            final Result result = new Result(code, headers, httpEntity);
            final String contentType = context.getResponseContentType();
            if (contentType != null) {
                return result.as(contentType);
            } else {
                return result;
            }
        }

        throw new TechnicalException("No action provided");
    }

    public Map<Integer, Result> getResults() {
        return results;
    }

    public void setResults(final Map<Integer, Result> results) {
        CommonHelper.assertNotNull("results", results);
        this.results = results;
    }
}
