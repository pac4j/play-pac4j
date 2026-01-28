package org.pac4j.play;

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.AnyContentAsFormUrlEncoded;
import play.api.mvc.AnyContentAsText;
import play.api.mvc.Request;
import play.api.mvc.RequestHeader;
import play.api.mvc.request.AssignedCell;
import play.api.mvc.request.RequestAttrKey;
import play.libs.typedmap.TypedKey;
import play.mvc.Http;
import play.mvc.Result;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * <p>This class is the web context for Play (used both for Java and Scala).</p>
 * <p>"Session objects" are managed by the defined {@link SessionStore}.</p>
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class PlayWebContext implements WebContext {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected static final TypedKey<Map<String, Object>> PAC4J_REQUEST_ATTRIBUTES = TypedKey.create("pac4jRequestAttributes");

    protected Http.RequestHeader javaRequest;

    protected RequestHeader scalaRequest;

    protected String requestContent;

    protected Map<String, String> responseHeaders = new HashMap<>();

    protected List<Http.Cookie> responseCookies = new ArrayList<>();

    protected String responseContentType;

    protected Http.Session session;

    protected Http.Session initialSession;

    public PlayWebContext(final Http.RequestHeader javaRequest) {
        CommonHelper.assertNotNull("request", javaRequest);
        this.javaRequest = javaRequest;
        this.session = javaRequest.session();
        this.initialSession = this.session;
    }

    public PlayWebContext(final RequestHeader scalaRequest) {
        this(scalaRequest.asJava());
        this.scalaRequest = scalaRequest;
    }

    public Http.RequestHeader getNativeJavaRequest() {
        return javaRequest;
    }

    public RequestHeader getNativeScalaRequest() {
        return scalaRequest;
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
            return ((Request<?>) scalaRequest).body();
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
    public Optional<String> getResponseHeader(final String name) {
        return Optional.ofNullable(responseHeaders.get(name));
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
        // Check if the cookie already exists in the request with the same value
        final Optional<Http.Cookie> existingCookie = javaRequest.cookies().get(cookie.getName());
        if (existingCookie.isPresent()) {
            final Http.Cookie existing = existingCookie.get();
            // If the cookie value hasn't changed, don't add it to response
            if (existing.value().equals(cookie.getValue())) {
                logger.trace("Skip adding response cookie {} as it already exists with same value", cookie.getName());
                return;
            }
        }

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
    }

    public boolean hasResponseModifications() {
        return !responseCookies.isEmpty() || !responseHeaders.isEmpty() || responseContentType != null || hasSessionChanged();
    }

    protected boolean hasSessionChanged() {
        if (session == null && initialSession == null) {
            return false;
        }
        if (session == null || initialSession == null) {
            return true;
        }
        return !session.equals(initialSession);
    }

    public Http.Request supplementRequest(final Http.Request request) {
        logger.trace("supplement request with: {} and session: {}", this.javaRequest.attrs(), session);
        return request.withAttrs(this.javaRequest.attrs()).addAttr(RequestAttrKey.Session().asJava(), new AssignedCell<>(session.asScala()));
    }

    public Http.RequestHeader supplementRequest(final Http.RequestHeader request) {
        logger.trace("supplement request with: {} and session: {}", this.javaRequest.attrs(), session);
        return request.withAttrs(this.javaRequest.attrs()).addAttr(RequestAttrKey.Session().asJava(), new AssignedCell<>(session.asScala()));
    }

    public <A> Request<A> supplementRequest(Request<A> request) {
        logger.trace("supplement request with: {} and session: {}", this.javaRequest.attrs(), session);
        return request.withAttrs(this.javaRequest.attrs().asScala()).addAttr(RequestAttrKey.Session(), new AssignedCell<>(session.asScala()));
    }

    public Result supplementResponse(final Result result) {
        Result r = result;
        if (responseCookies.size() > 0) {
            logger.trace("supplement response with cookies: {}", responseCookies);
            r = r.withCookies(responseCookies.toArray(new Http.Cookie[responseCookies.size()]));
            responseCookies.clear();
        }
        if (responseHeaders.size() > 0) {
            for (final Map.Entry<String, String> header : responseHeaders.entrySet()) {
                logger.trace("supplement response with header: {}", header);
                r = r.withHeader(header.getKey(), header.getValue());
            }
            responseHeaders.clear();
        }
        if (responseContentType != null) {
            logger.trace("supplement response with type: {}", responseContentType);
            r = r.as(responseContentType);
            responseContentType = null;
        }
        if (hasSessionChanged()) {
            logger.trace("supplement response with session: {}", session);
            Map<String, String> originalSession = result.session().data();
            Map<String, String> merged = new HashMap<>(session.data());
            originalSession.forEach(merged::putIfAbsent);
            r = r.withSession(new Http.Session(merged));
        }
        return r;
    }

    public play.api.mvc.Result supplementResponse(final play.api.mvc.Result result) {
        return supplementResponse(result.asJava()).asScala();
    }
}
