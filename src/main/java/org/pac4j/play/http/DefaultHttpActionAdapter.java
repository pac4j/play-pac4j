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
package org.pac4j.play.http;

import java.util.Map;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http.Context;
import play.mvc.Http.Response;

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
            return redirect(getLocation(webContext));
        } else if (code == HttpConstants.OK) {
            final String content = webContext.getResponseContent();
            logger.debug("render: {}", content);
            return ok(content).as(HttpConstants.HTML_CONTENT_TYPE);
        }
        final String message = "Unsupported HTTP action: " + code;
        logger.error(message);
        throw new TechnicalException(message);
    }

    private String getLocation(final PlayWebContext webContext){
      final Context context = webContext.getJavaContext();
      final Response response = context.response();
      final Map<String, String> headers = response.getHeaders();

      return headers.get(HttpConstants.LOCATION_HEADER);
    }
}
