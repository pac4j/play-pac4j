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

import java.util.Map;

import org.pac4j.core.context.WebContext;
import org.pac4j.play.Constants;
import org.pac4j.play.StorageHelper;

import play.api.mvc.AnyContent;
import play.api.mvc.Request;
import play.api.mvc.Session;
import scala.Option;
import scala.collection.Seq;

/**
 * This class is the Scala web context for Play. Most of the methods are not implemented.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class ScalaWebContext<C> implements WebContext {
    
    private final Request<C> request;
    
    private final Session session;
    
    public ScalaWebContext(final Request<C> request, final Session session) {
        this.request = request;
        this.session = session;
    }
    
    public String getRequestHeader(final String name) {
        throw new IllegalArgumentException("getRequestHeader not implemented");
    }
    
    public String getRequestMethod() {
        throw new IllegalArgumentException("getRequestMethod not implemented");
    }
    
    public String getRequestParameter(final String name) {
        String value = null;
        Option<Seq<String>> values = this.request.queryString().get(name);
        if (values.isDefined()) {
            value = values.get().head();
        }
        if (value == null && this.request instanceof Request) {
            final Option<scala.collection.immutable.Map<String, Seq<String>>> formParameters = ((Request<AnyContent>) (this.request))
                .body().asFormUrlEncoded();
            if (formParameters.isDefined()) {
                values = formParameters.get().get(name);
                if (values.isDefined()) {
                    value = values.get().head();
                }
            }
        }
        return value;
    }
    
    public Map<String, String[]> getRequestParameters() {
        throw new IllegalArgumentException("getRequestParameters not implemented");
    }
    
    public Object getSessionAttribute(final String key) {
        Object value = null;
        final Option<String> sessionId = this.session.get(Constants.SESSION_ID);
        if (sessionId.isDefined()) {
            value = StorageHelper.get(sessionId.get(), key);
        }
        return value;
    }
    
    public void setResponseStatus(final int code) {
        // do nothing
    }
    
    public void setSessionAttribute(final String key, final Object value) {
        final Option<String> sessionId = this.session.get(Constants.SESSION_ID);
        if (sessionId.isDefined()) {
            StorageHelper.save(sessionId.get(), key, value);
        }
    }
    
    public void writeResponseContent(final String content) {
        throw new IllegalArgumentException("writeResponseContent not implemented");
    }
    
    public void setResponseHeader(final String key, final String value) {
        throw new IllegalArgumentException("setResponseHeader not implemented");
    }
    
    public String getServerName() {
        String[] split = this.request.host().split(":");
        return split[0];
    }
    
    public int getServerPort() {
        String[] split = this.request.host().split(":");
        String portStr = (split.length > 1) ? split[1] : "80";
        return Integer.valueOf(portStr);
    }
    
    public String getScheme() {
        // TODO: play api does not expose the scheme, just return http for now
        return "http";
    }
}
