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
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import com.google.inject.Inject;

import play.cache.CacheApi;
import play.mvc.Http;

public class PlayCookieStore implements PlaySessionStore {
	
	private static final Logger logger = LoggerFactory.getLogger(PlayCookieStore.class);

    private final static String SEPARATOR = "$";

    // prefix for the cache
    private String prefix = "";

    // 1 hour = 3600 seconds
    private int timeout = 3600;
    
    private static boolean useCache = false;
    private static SecretSignatureConfiguration secretSignatureConfig;
    private static SecretEncryptionConfiguration secretEncryptConfig;
    private JwtAuthenticator jwtAuthenticator;
    private JwtGenerator<CommonProfile> jwtGenerator;
    private final CacheApi cache;
    
	@Inject
	public PlayCookieStore(final CacheApi cache) {
		super();
		this.jwtAuthenticator = new JwtAuthenticator(secretSignatureConfig, secretEncryptConfig);
		this.jwtGenerator = new JwtGenerator<CommonProfile>(secretSignatureConfig, secretEncryptConfig);
		this.cache = cache;
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
		//save the session as a JWT, replacing the previous instance if necessary.
		if(session.containsKey(sessionId))
			session.remove(sessionId);
		final String sessionToken = jwtGenerator.generate(sessionMap);
		session.put(sessionId, sessionToken);
		if(useCache) {
			cache.set(getKey(sessionId, "MapCache"), sessionMap);
			cache.set(getKey(sessionId, "TokenCache"), sessionToken);
		}
	}
	
	private Map<String, Object> getOrCreateSessionMap(final PlayWebContext context, String sessionId){
		String sessionToken = getSessionToken(context, sessionId);
		//get the claims, and if it fails, generate the session map
		Map<String, Object> sessionMap;
		try {
			if(useCache && sessionToken.contentEquals(cache.get(getKey(sessionId, "TokenCache")))) {
				sessionMap = (Map<String, Object>) cache.getOrElse(getKey(sessionId, "MapCache"), () -> {
					return jwtAuthenticator.validateTokenAndGetClaims(sessionToken);
				});
			}
			else {
				sessionMap = jwtAuthenticator.validateTokenAndGetClaims(sessionToken);
			}
		}
		catch(TechnicalException|NullPointerException e) {
			sessionMap = new HashMap<String, Object>();
			//Ensures the map will be correctly built into a profile during the 'validate' call wrapped by validateTokenAndGetClaims()
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
		if(key.contentEquals(Pac4jConstants.USER_PROFILES)) {
			if (value instanceof LinkedHashMap<?,?>) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>)value;
				profiles.forEach((name, profile) ->{
					profile.clearSensitiveData();
				});
				sessionMap.put(getKey(sessionId,key), profiles);
			}
			else {
				CommonProfile profile = ((CommonProfile)value);
				profile.clearSensitiveData();
				sessionMap.put(getKey(sessionId,key), profile);
			}
		}
		else {
			sessionMap.put(getKey(sessionId,key), value);
		}
		setSessionToken(context, sessionId, sessionMap);
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

	public static SecretSignatureConfiguration getSecretSignatureConfig() {
		return secretSignatureConfig;
	}

	public static void setSecretSignatureConfig(SecretSignatureConfiguration secretSignatureConfig) {
		PlayCookieStore.secretSignatureConfig = secretSignatureConfig;
	}

	public static SecretEncryptionConfiguration getSecretEncryptConfig() {
		return secretEncryptConfig;
	}

	public static void setSecretEncryptConfig(SecretEncryptionConfiguration secretEncryptConfig) {
		PlayCookieStore.secretEncryptConfig = secretEncryptConfig;
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

	public static boolean isUseCache() {
		return useCache;
	}

	public static void setUseCache(boolean useCache) {
		PlayCookieStore.useCache = useCache;
	}

}
