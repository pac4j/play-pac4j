/*
  Copyright 2012 - 2013 Jerome Leleu

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
package org.pac4j.play.scala;

import java.io.IOException;
import java.util.Map;

import org.pac4j.core.context.WebContext;
import org.pac4j.play.Constants;
import org.pac4j.play.StorageHelper;

import play.api.mvc.Session;
import scala.Option;

/**
 * This class is the Scala web context for Play. Most of the methods are not implemented.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class ScalaWebContext implements WebContext {
    
    private final Session session;
    
    public ScalaWebContext(final Session session) {
        this.session = session;
    }
    
    public String getRequestHeader(final String name) {
        throw new IllegalArgumentException("getRequestHeader not implemented");
    }
    
    public String getRequestMethod() {
        throw new IllegalArgumentException("getRequestMethod not implemented");
    }
    
    public String getRequestParameter(final String name) {
        throw new IllegalArgumentException("getRequestParameter not implemented");
    }
    
    public Map<String, String[]> getRequestParameters() {
        throw new IllegalArgumentException("getRequestParameters not implemented");
    }
    
    public Object getSessionAttribute(final String key) {
        throw new IllegalArgumentException("getSessionAttribute not implemented");
    }
    
    public void setResponseStatus(final int code) {
        throw new IllegalArgumentException("setResponseStatus not implemented");
    }
    
    public void setSessionAttribute(final String key, final Object value) {
        Option<String> sessionId = this.session.get(Constants.SESSION_ID);
        if (sessionId.isDefined()) {
            StorageHelper.save(sessionId.get(), key, value);
        }
    }
    
    public void writeResponseContent(final String content) throws IOException {
        throw new IllegalArgumentException("writeResponseContent not implemented");
    }
    
    public void setResponseHeader(final String key, final String value) {
        throw new IllegalArgumentException("setResponseHeader not implemented");
    }
}
