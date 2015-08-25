/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play.handler;

import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Result;

import javax.inject.Singleton;

import static play.mvc.Results.*;

/**
 * Default implementation.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
@Singleton
public class DefaultHttpActionHandler implements HttpActionHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    public Result handle(int code, PlayWebContext context) {
        logger.debug("requires HTTP action: {}", code);
        if (code == HttpConstants.UNAUTHORIZED) {
            return unauthorized("authentication required");
        } else if (code == HttpConstants.FORBIDDEN) {
            return forbidden("forbidden");
        } else if (code == HttpConstants.TEMP_REDIRECT) {
            return redirect(context.getResponseLocation());
        } else if (code == HttpConstants.OK) {
            final String content = context.getResponseContent();
            logger.debug("render: {}", content);
            return ok(content).as(HttpConstants.HTML_CONTENT_TYPE);
        }
        final String message = "Unsupported HTTP action: " + code;
        logger.error(message);
        throw new TechnicalException(message);
    }

    /**
     * {@inheritDoc}
     */
    public Result handleRedirect(final RedirectAction action) {
        switch (action.getType()) {
            case REDIRECT:
                return redirect(action.getLocation());
            case SUCCESS:
                return ok(action.getContent()).as(HttpConstants.HTML_CONTENT_TYPE);
            default:
                throw new TechnicalException("Unsupported RedirectAction type: " + action.getType());
        }
    }
}
