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
import org.pac4j.play.PlayWebContext;
import play.mvc.Result;

/**
 * Handle HTTP action for Play.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public interface HttpActionHandler {

    /**
     * Handle HTTP action.
     *
     * @param code the HTTP status code
     * @param context the web context
     * @return the Play result
     */
    Result handle(int code, PlayWebContext context);

    /**
     * Handle HTTP action for redirection use cases.
     *
     * @param action the pac4j action to perform
     * @return the Play result
     */
    Result handleRedirect(RedirectAction action);
}
