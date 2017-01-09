package org.pac4j.play.store;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.jwt.JwtClaims;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import com.google.inject.Inject;

import play.mvc.Http;

/**
 * The Cookie storage uses an encrypted JWT inside the Play Session cookie for
 * storage, allowing for a stateless backend.
 * 
 * @author Nolan Barth
 * @since 2.6.2
 */
public class PlayCookieStore implements PlaySessionStore {

	private static final Logger logger = LoggerFactory.getLogger(PlayCookieStore.class);

	private final static String SEPARATOR = "$";

	// prefix for the cache
	private String prefix = "";

	// 1 hour = 3600 seconds
	private int timeout = 3600;

	private JwtAuthenticator jwtAuthenticator;
	private JwtGenerator<CommonProfile> jwtGenerator;

	@Inject
	public PlayCookieStore(final SecretEncryptionConfiguration secretEncryptConfig) {
		super();
		this.jwtAuthenticator = new JwtAuthenticator();
		this.jwtAuthenticator.addEncryptionConfiguration(secretEncryptConfig);
		this.jwtGenerator = new JwtGenerator<CommonProfile>();
		this.jwtGenerator.setEncryptionConfiguration(secretEncryptConfig);
	}

	String getKey(final String sessionId, final String key) {
		return prefix + SEPARATOR + sessionId + SEPARATOR + key;
	}

	@Override
	public String getOrCreateSessionId(final PlayWebContext context) {
		final Http.Session session = context.getJavaSession();
		// get current sessionId
		String sessionId = session.get(Pac4jConstants.SESSION_ID);
		logger.trace("retrieved sessionId: {}", sessionId);
		// if null, generate a new one
		if (sessionId == null) {
			// generate id for session
			sessionId = java.util.UUID.randomUUID().toString();
			logger.debug("generated sessionId: {}", sessionId);
			// and save it to session
			session.put(Pac4jConstants.SESSION_ID, sessionId);
		}
		return sessionId;
	}

	private String getSessionToken(final PlayWebContext context, String sessionId) {
		final Http.Session session = context.getJavaSession();
		// get current sessionToken using sessionId
		String sessionToken = session.get(sessionId);
		logger.trace("retrieved sessionToken: {}", sessionToken);
		return sessionToken;
	}

	private void setSessionToken(final PlayWebContext context, String sessionId, Map<String, Object> sessionMap) {
		final Http.Session session = context.getJavaSession();
		// save the session as a JWT, replacing the previous instance if
		// necessary.
		if (session.containsKey(sessionId))
			session.remove(sessionId);
		final String sessionToken = jwtGenerator.generate(sessionMap);
		session.put(sessionId, sessionToken);
	}

	private Map<String, Object> getOrCreateSessionMap(final PlayWebContext context, String sessionId) {
		String sessionToken = getSessionToken(context, sessionId);
		// get the claims, and if it fails, generate the session map
		Map<String, Object> sessionMap;
		try {
			sessionMap = jwtAuthenticator.validateTokenAndGetClaims(sessionToken);
		} catch (TechnicalException | NullPointerException e) {
			sessionMap = new HashMap<String, Object>();
			// Ensures the map will be correctly built into a profile during the
			// 'validate' call wrapped by validateTokenAndGetClaims()
			sessionMap.put(JwtClaims.SUBJECT, sessionId);
		}
		return sessionMap;
	}

	@Override
	public Object get(final PlayWebContext context, final String key) {
		String sessionId = getOrCreateSessionId(context);
		return getOrCreateSessionMap(context, sessionId).get(getKey(sessionId, key));
	}

	@Override
	public void set(final PlayWebContext context, final String key, final Object value) {
		String sessionId = getOrCreateSessionId(context);
		Map<String, Object> sessionMap = getOrCreateSessionMap(context, sessionId);
		if (key.contentEquals(Pac4jConstants.USER_PROFILES)) {
			Object clearedProfiles = clearUserProfiles(value);
			sessionMap.put(getKey(sessionId, key), clearedProfiles);
		} else {
			sessionMap.put(getKey(sessionId, key), value);
		}
		setSessionToken(context, sessionId, sessionMap);
	}

	private Object clearUserProfiles(Object value) {
		if (value instanceof LinkedHashMap<?, ?>) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) value;
			profiles.forEach((name, profile) -> {
				profile.clearSensitiveData();
			});
			return profiles;
		} else {
			CommonProfile profile = ((CommonProfile) value);
			profile.clearSensitiveData();
			return profile;
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public JwtAuthenticator getJwtAuthenticator() {
		return jwtAuthenticator;
	}

	public void setJwtAuthenticator(JwtAuthenticator jwtAuthenticator) {
		this.jwtAuthenticator = jwtAuthenticator;
	}

	public JwtGenerator<CommonProfile> getJwtGenerator() {
		return jwtGenerator;
	}

	public void setJwtGenerator(JwtGenerator<CommonProfile> jwtGenerator) {
		this.jwtGenerator = jwtGenerator;
	}
}
