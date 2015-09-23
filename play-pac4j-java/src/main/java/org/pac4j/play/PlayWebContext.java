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
package org.pac4j.play;

import java.util.*;

import org.pac4j.core.context.BaseResponseContext;

import org.pac4j.core.context.Cookie;
import org.pac4j.play.store.DataStore;
import play.api.mvc.RequestHeader;
import play.core.j.JavaHelpers$;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Http.Session;
import play.mvc.Http.Context;

/**
 * <p>This class is the web context for Play (used both for Java and Scala).</p>
 * <p>"Session objects" are managed by the defined {@link DataStore}.</p>
 * <p>"Request attributes" are saved/restored to/from the context.</p>
 * 
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class PlayWebContext extends BaseResponseContext {

    protected final Context context;

    protected final Request request;

    protected final Response response;

    protected final Session session;

    protected final DataStore dataStore;

    public PlayWebContext(final Context context, final DataStore dataStore) {
        this.context = context;
        this.request = context.request();
        this.response = context.response();
        this.session = context.session();
        this.dataStore = dataStore;
    }

    public PlayWebContext(final RequestHeader requestHeader, final DataStore dataStore) {
        this(JavaHelpers$.MODULE$.createJavaContext(requestHeader), dataStore);
    }

    /**
     * Get the Java session.
     *
     * @return the Java session
     */
    public Session getJavaSession() {
        return session;
    }

    /**
     * Get the Java request.
     *
     * @return the Java request
     */
    public Request getJavaRequest() {
        return request;
    }

    /**
     * Get the Java context.
     *
     * @return the Java context.
     */
    public Context getJavaContext() {
        return this.context;
    }

    /**
     * Return the session storage.
     *
     * @return the session storage
     */
    public DataStore getDataStore() {
        return this.dataStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestHeader(final String name) {
        return request.getHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestMethod() {
        return request.method();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestParameter(final String name) {
        final Map<String, String[]> parameters = getRequestParameters();
        final String[] values = parameters.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String[]> getRequestParameters() {
        final Http.RequestBody body = request.body();
        final Map<String, String[]> formParameters;
        if (body != null) {
            formParameters = body.asFormUrlEncoded();
        } else {
            formParameters = new HashMap<String, String[]>();
        }
        final Map<String, String[]> urlParameters = request.queryString();
        final Map<String, String[]> parameters = new HashMap<String, String[]>();
        if (formParameters != null) {
            parameters.putAll(formParameters);
        }
        if (urlParameters != null) {
            parameters.putAll(urlParameters);
        }
        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getSessionIdentifier() {
        return dataStore.getOrCreateSessionId(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getSessionAttribute(final String key) {
        return dataStore.get(this, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSessionAttribute(final String key, final Object value) {
        dataStore.set(this, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResponseHeader(final String name, final String value) {
        response.setHeader(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerName() {
        String[] split = request.host().split(":");
        return split[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getServerPort() {
        String[] split = request.host().split(":");
        String portStr = (split.length > 1) ? split[1] : "80";
        return Integer.valueOf(portStr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        if (request.secure()) {
            return "https";
        } else {
            return "http";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullRequestURL() {
        return getScheme() + "://" + request.host() + request.uri();
    }

    /**
     * {@inheritDoc}
     */
    public String getRemoteAddr() {
        return request.remoteAddress();
    }

    /**
     * {@inheritDoc}
     */
    public Object getRequestAttribute(String name) {
        return context.args.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public void setRequestAttribute(String name, Object value) {
        context.args.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public void invalidateSession() {
        dataStore.invalidate(this);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Cookie> getRequestCookies() {
        final List<Cookie> cookies = new ArrayList<>();
        final Http.Cookies httpCookies = request.cookies();
        httpCookies.forEach(httpCookie -> {
            final Cookie cookie = new Cookie(httpCookie.name(), httpCookie.value());
            cookie.setDomain(httpCookie.domain());
            cookie.setHttpOnly(httpCookie.httpOnly());
            cookie.setMaxAge(httpCookie.maxAge());
            cookie.setPath(httpCookie.path());
            cookie.setSecure(httpCookie.secure());
            cookies.add(cookie);
        });
        return cookies;
    }
}
