package org.pac4j.play;

import java.util.*;

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.store.PlaySessionStore;
import play.api.mvc.AnyContentAsFormUrlEncoded;
import play.api.mvc.AnyContentAsText;
import play.api.mvc.Request;
import play.api.mvc.RequestHeader;
import play.libs.typedmap.TypedKey;
import play.mvc.Http;
import play.mvc.Result;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * <p>This class is the web context for Play (used both for Java and Scala).</p>
 * <p>"Session objects" are managed by the defined {@link SessionStore}.</p>
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class PlayWebContext implements WebContext {

    protected static final TypedKey<Map<String, Object>> PAC4J_REQUEST_ATTRIBUTES = TypedKey.create("pac4jRequestAttributes");

    protected Http.RequestHeader javaRequest;

    protected RequestHeader scalaRequest;

    protected String requestContent;

    protected PlaySessionStore sessionStore;

    protected Map<String, String> responseHeaders = new HashMap<>();

    protected List<Http.Cookie> responseCookies = new ArrayList<>();

    protected String responseContentType;

    protected boolean sessionHasChanged;

    protected Http.Session session;

    public PlayWebContext(final Http.RequestHeader javaRequest, final PlaySessionStore sessionStore) {
        CommonHelper.assertNotNull("request", javaRequest);
        CommonHelper.assertNotNull("sessionStore", sessionStore);
        this.javaRequest = javaRequest;
        this.sessionStore = sessionStore;
        this.session = javaRequest.session();
        sessionHasChanged = false;
    }

    public PlayWebContext(final RequestHeader scalaRequest, final PlaySessionStore sessionStore) {
        this(scalaRequest.asJava(), sessionStore);
        this.scalaRequest = scalaRequest;
    }

    @Override
    public SessionStore getSessionStore() {
        return this.sessionStore;
    }

    @Override
    public Optional<String> getRequestHeader(final String name) {
        return javaRequest.header(name);
    }

    @Override
    public String getRequestMethod() {
        return javaRequest.method();
    }

    @Override
    public Optional<String> getRequestParameter(final String name) {
        final Map<String, String[]> parameters = getRequestParameters();
        final String[] values = parameters.get(name);
        if (values != null && values.length > 0) {
            return Optional.of(values[0]);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        final Map<String, String[]> parameters = new HashMap<>();
        final Object body = getBody();
        Map<String, String[]> p = null;
        if (body instanceof Http.RequestBody) {
            p = ((Http.RequestBody) body).asFormUrlEncoded();
        } else if (body instanceof AnyContentAsFormUrlEncoded) {
            p = ScalaCompatibility.parseBody((AnyContentAsFormUrlEncoded) body);
        }
        if (p != null) {
            parameters.putAll(p);
        }
        final Map<String, String[]> urlParameters = javaRequest.queryString();
        if (urlParameters != null) {
            parameters.putAll(urlParameters);
        }
        return parameters;
    }

    protected Object getBody() {
        if (scalaRequest != null && scalaRequest.hasBody() && scalaRequest instanceof Request) {
            return ((Request) scalaRequest).body();
        } else if (javaRequest.hasBody() && javaRequest instanceof Http.Request) {
            return ((Http.Request) javaRequest).body();
        }
        return null;
    }

    @Override
    public void setResponseHeader(final String name, final String value) {
        responseHeaders.put(name, value);
    }

    @Override
    public String getServerName() {
        String[] split = javaRequest.host().split(":");
        return split[0];
    }

    @Override
    public int getServerPort() {
        String defaultPort = javaRequest.secure() ? "443" : "80";

        String[] split = javaRequest.host().split(":");
        String portStr = split.length > 1 ? split[1] : defaultPort;
        return Integer.parseInt(portStr);
    }

    @Override
    public String getScheme() {
        if (javaRequest.secure()) {
            return "https";
        } else {
            return "http";
        }
    }

    @Override
    public boolean isSecure() { return javaRequest.secure(); }

    @Override
    public String getFullRequestURL() {
        return getScheme() + "://" + javaRequest.host() + javaRequest.uri();
    }

    @Override
    public String getRemoteAddr() {
        return javaRequest.remoteAddress();
    }

    @Override
    public Optional<Object> getRequestAttribute(final String name) {
        Map<String, Object> attributes = javaRequest.attrs().getOptional(PAC4J_REQUEST_ATTRIBUTES).orElse(new HashMap<>());
        return Optional.ofNullable(attributes.get(name));
    }

    @Override
    public void setRequestAttribute(final String name, final Object value) {
        Map<String, Object> attributes = javaRequest.attrs().getOptional(PAC4J_REQUEST_ATTRIBUTES).orElse(new HashMap<>());
        attributes.put(name, value);
        javaRequest = javaRequest.addAttr(PAC4J_REQUEST_ATTRIBUTES, attributes);
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        final List<Cookie> cookies = new ArrayList<>();
        final Http.Cookies httpCookies = javaRequest.cookies();
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
        return javaRequest.path();
    }

    @Override
    public void addResponseCookie(final Cookie cookie) {
        final Http.CookieBuilder cookieBuilder =
                Http.Cookie.builder(cookie.getName(), cookie.getValue())
                        .withPath(cookie.getPath())
                        .withDomain(cookie.getDomain())
                        .withSecure(cookie.isSecure())
                        .withHttpOnly(cookie.isHttpOnly());
        // in Play, maxAge: Cookie duration in seconds (null for a transient cookie [value by default], 0 or less for one that expires now)
        // in pac4j, maxAge == -1 -> session cookie, 0 -> expires now, > 0, expires in x seconds
        final int maxAge = cookie.getMaxAge();
        if (maxAge != -1) {
            cookieBuilder.withMaxAge(Duration.of(maxAge, ChronoUnit.SECONDS));
        }
        final Http.Cookie responseCookie = cookieBuilder.build();
        responseCookies.add(responseCookie);
    }

    @Override
    public void setResponseContentType(final String contentType) {
        responseContentType = contentType;
    }

    @Override
    public String getRequestContent() {
        if (requestContent == null) {
            final Object body = getBody();
            if (body instanceof Http.RequestBody) {
                requestContent = ((Http.RequestBody) body).asText();
            } else if (body instanceof AnyContentAsText) {
                requestContent = ((AnyContentAsText) body).asText().getOrElse(null);
            }
        }
        return requestContent;
    }

    public Http.Session getNativeSession() {
        return session;
    }

    public void setNativeSession(final Http.Session session) {
        this.session = session;
        sessionHasChanged = true;
    }

    public Http.Request supplementRequest(final Http.Request request) {
        return request.withAttrs(this.javaRequest.attrs());
    }

    public Http.RequestHeader supplementRequest(final Http.RequestHeader request) {
        return request.withAttrs(this.javaRequest.attrs());
    }

    public Result supplementResponse(final Result result) {
        Result r = result;
        if (responseCookies.size() > 0) {
            r = r.withCookies(responseCookies.toArray(new Http.Cookie[responseCookies.size()]));
            responseCookies.clear();
        }
        if (responseHeaders.size() > 0) {
            for (final Map.Entry<String, String> header : responseHeaders.entrySet()) {
                r = r.withHeader(header.getKey(), header.getValue());
            }
            responseHeaders.clear();
        }
        if (responseContentType != null) {
            r = r.as(responseContentType);
            responseContentType = null;
        }
        if (sessionHasChanged) {
            r = r.withSession(session);
            session = javaRequest.session();
            sessionHasChanged = false;
        }
        return r;
    }
}
