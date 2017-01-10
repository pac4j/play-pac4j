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
import org.pac4j.jwt.config.encryption.EncryptionConfiguration;
import com.google.inject.Inject;

import play.mvc.Http;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * The Cookie storage uses an encrypted JWT inside the Play Session cookie for
 * storage, allowing for a stateless backend.
 * 
 * @author Nolan Barth
 * @since 2.6.2
 */
public class PlayCookieStore implements PlaySessionStore {

	private static final Logger logger = LoggerFactory.getLogger(PlayCookieStore.class);

	private JwtAuthenticator jwtAuthenticator;
	private JwtGenerator<CommonProfile> jwtGenerator;
	private final String tokenName = "pac4j";

	@Inject
	public PlayCookieStore(final EncryptionConfiguration encryptConfig) {
		assertNotNull("encryptConfig", encryptConfig);
		this.jwtAuthenticator = new JwtAuthenticator();
		this.jwtAuthenticator.addEncryptionConfiguration(encryptConfig);
		this.jwtGenerator = new JwtGenerator<CommonProfile>();
		this.jwtGenerator.setEncryptionConfiguration(encryptConfig);
	}

	@Override
	public String getOrCreateSessionId(final PlayWebContext context) {
		return tokenName;
	}

	private String getSessionToken(final PlayWebContext context) {
		final Http.Session session = context.getJavaSession();
		// get current sessionToken using sessionId
		String sessionToken = session.get(tokenName);
		logger.trace("retrieved sessionToken: {}", sessionToken);
		return sessionToken;
	}

	private void setSessionToken(final PlayWebContext context, Map<String, Object> sessionMap) {
		final Http.Session session = context.getJavaSession();
		// save the session as a JWT, replacing the previous instance if
		// necessary.
		if (session.containsKey(tokenName))
			session.remove(tokenName);
		final String sessionToken = jwtGenerator.generate(sessionMap);
		session.put(tokenName, sessionToken);
	}

	private Map<String, Object> getOrCreateSessionMap(final PlayWebContext context) {
		String sessionToken = getSessionToken(context);
		// get the claims, and if it fails, generate the session map
		Map<String, Object> sessionMap;
		try {
			sessionMap = jwtAuthenticator.validateTokenAndGetClaims(sessionToken);
		} catch (TechnicalException | NullPointerException e) {
			sessionMap = new HashMap<String, Object>();
			// Ensures the map will be correctly built into a profile during the
			// 'validate' call wrapped by validateTokenAndGetClaims()
			sessionMap.put(JwtClaims.SUBJECT, tokenName);
		}
		return sessionMap;
	}

	@Override
	public Object get(final PlayWebContext context, final String key) {
		return getOrCreateSessionMap(context).get(key);
	}

	@Override
	public void set(final PlayWebContext context, final String key, final Object value) {
		Map<String, Object> sessionMap = getOrCreateSessionMap(context);
		if (key.contentEquals(Pac4jConstants.USER_PROFILES)) {
			Object clearedProfiles = clearUserProfiles(value);
			sessionMap.put(key, clearedProfiles);
		} else {
			sessionMap.put(key, value);
		}
		setSessionToken(context, sessionMap);
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
