package org.pac4j.play;

import java.util.*;

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.JavaSerializationHelper;
import play.api.mvc.RequestHeader;
import play.core.j.JavaHelpers$;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Http.Session;
import play.mvc.Http.Context;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This class is the web context for Play (used both for Java and Scala).</p>
 * <p>"Session objects" are managed by the defined {@link SessionStore}.</p>
 * <p>"Request attributes" are saved/restored to/from the context.</p>
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class PlayWebContext implements WebContext {

    public final static String SB64_PREFIX = "{sb64}";

    public final static JavaSerializationHelper JAVA_SERIALIZATION_HELPER = new JavaSerializationHelper();

    protected final Context context;

    protected final Request request;

    protected final Response response;

    protected final Session session;

    protected SessionStore<PlayWebContext> sessionStore;

    protected String responseContent = "";

    public PlayWebContext(final Context context, final SessionStore<PlayWebContext> sessionStore) {
        this.context = context;
        this.request = context.request();
        this.response = context.response();
        this.session = context.session();
        setSessionStore(sessionStore);
    }

    public PlayWebContext(final RequestHeader requestHeader, final SessionStore<PlayWebContext> sessionStore) {
        this(JavaHelpers$.MODULE$.createJavaContext(requestHeader, JavaHelpers$.MODULE$.createContextComponents()), sessionStore);
    }

    @Override
    public SessionStore getSessionStore() {
        return this.sessionStore;
    }

    @Override
    public void setSessionStore(final SessionStore sessionStore) {
        assertNotNull("sessionStore", sessionStore);
        this.sessionStore = sessionStore;
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
     * Get the Java context.
     *
     * @return the Java context.
     */
    public Context getJavaContext() {
        return this.context;
    }

    @Override
    public void setResponseStatus(final int code) {}

    @Override
    public void writeResponseContent(final String content) {
        if (content != null) {
            this.responseContent += content;
        }
    }

    /**
     * Get the response content.
     *
     * @return the response content
     */
    public String getResponseContent() {
        return this.responseContent;
    }

    @Override
    public String getRequestHeader(final String name) {
        return request.getHeader(name);
    }

    @Override
    public String getRequestMethod() {
        return request.method();
    }

    @Override
    public String getRequestParameter(final String name) {
        final Map<String, String[]> parameters = getRequestParameters();
        final String[] values = parameters.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        final Http.RequestBody body = request.body();
        final Map<String, String[]> formParameters;
        if (body != null) {
            formParameters = body.asFormUrlEncoded();
        } else {
            formParameters = new HashMap<>();
        }
        final Map<String, String[]> urlParameters = request.queryString();
        final Map<String, String[]> parameters = new HashMap<>();
        if (formParameters != null) {
            parameters.putAll(formParameters);
        }
        if (urlParameters != null) {
            parameters.putAll(urlParameters);
        }
        return parameters;
    }

    @Override
    public void setResponseHeader(final String name, final String value) {
        response.setHeader(name, value);
    }

    @Override
    public String getServerName() {
        String[] split = request.host().split(":");
        return split[0];
    }

    @Override
    public int getServerPort() {
        String[] split = request.host().split(":");
        String portStr = split.length > 1 ? split[1] : "80";
        return Integer.parseInt(portStr);
    }

    @Override
    public String getScheme() {
        if (request.secure()) {
            return "https";
        } else {
            return "http";
        }
    }

    @Override
    public boolean isSecure() { return request.secure(); }

    @Override
    public String getFullRequestURL() {
        return getScheme() + "://" + request.host() + request.uri();
    }

    @Override
    public String getRemoteAddr() {
        return request.remoteAddress();
    }

    @Override
    public Object getRequestAttribute(final String name) {
        Object value = context.args.get(name);
        // this is a hack: we try to get the profiles as a serialized value because of the SecurityFilter
        if (Pac4jConstants.USER_PROFILES.equals(name) && value != null && value instanceof String) {
            final String sValue = (String) value;
            if (sValue.startsWith(SB64_PREFIX)) {
                value = JAVA_SERIALIZATION_HELPER.unserializeFromBase64(sValue.substring(SB64_PREFIX.length()));
            }
        }
        return value;
    }

    @Override
    public void setRequestAttribute(final String name, final Object value) {
        context.args.put(name, value);
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        final List<Cookie> cookies = new ArrayList<>();
        final Http.Cookies httpCookies = request.cookies();
        httpCookies.forEach(httpCookie -> {
            final Cookie cookie = new Cookie(httpCookie.name(), httpCookie.value());
            if(httpCookie.domain() != null) {
            	cookie.setDomain(httpCookie.domain());
            }
            cookie.setHttpOnly(httpCookie.httpOnly());
            if(httpCookie.maxAge() != null) {
                cookie.setMaxAge(httpCookie.maxAge());
            }
            cookie.setPath(httpCookie.path());
            cookie.setSecure(httpCookie.secure());
            cookies.add(cookie);
        });
        return cookies;
    }

    @Override
    public String getPath() {
        return request.path();
    }

    @Override
    public void addResponseCookie(final Cookie cookie) {
    	final Duration maxAge = cookie.getMaxAge() == -1 ? null : Duration.of(cookie.getMaxAge(), ChronoUnit.SECONDS);
    	final Http.Cookie responseCookie =
    			Http.Cookie.builder(cookie.getName(), cookie.getValue())
    			  .withMaxAge(maxAge)
    			  .withPath(cookie.getPath())
    			  .withDomain(cookie.getDomain())
    			  .withSecure(cookie.isSecure())
    			  .withHttpOnly(cookie.isHttpOnly())
    			  .build();
        response.setCookie(responseCookie);
    }

    @Override
    public void setResponseContentType(final String content) {
        response.setContentType(content);
    }
}
