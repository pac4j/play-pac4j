package org.pac4j.play.store;

import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.jwt.JwtClaims;
import org.pac4j.core.util.JavaSerializationHelper;
import org.pac4j.jwt.config.encryption.EncryptionConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * The Cookie storage uses directly the Play Session cookie (JWT encrypted) for
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

    public static final JavaSerializationHelper JAVA_SERIALIZATION_HELPER = new JavaSerializationHelper();

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
        logger.debug("Generated a token of {} characters", sessionToken.length());
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
        String sessionValue = (String) (getOrCreateSessionMap(context).get(key));
        if (sessionValue == null) {
            return null;
        } else {
            return JAVA_SERIALIZATION_HELPER.unserializeFromBase64(uncompressString(sessionValue));
        }
    }

    @Override
    public void set(final PlayWebContext context, final String key, final Object value) {
        Map<String, Object> sessionMap = getOrCreateSessionMap(context);

        Object clearedValue = value;
        if (key.contentEquals(Pac4jConstants.USER_PROFILES)) {
            clearedValue = clearUserProfiles(value);
        }
        logger.debug("PlayCookieStore.set, key = {}, value = {}", key, clearedValue);

        sessionMap.put(key, compressString(JAVA_SERIALIZATION_HELPER.serializeToBase64((Serializable) clearedValue)));
        setSessionToken(context, sessionMap);
    }

    // FIXME: can we implement this?
    @Override
    public boolean destroySession(PlayWebContext playWebContext) {
        return false;
    }

    @Override
    public Object getTrackableSession(PlayWebContext playWebContext) {
        return null;
    }

    @Override
    public SessionStore<PlayWebContext> buildFromTrackableSession(PlayWebContext playWebContext, Object o) {
        return null;
    }

    // FIXME
    @Override
    public boolean renewSession(PlayWebContext playWebContext) {
        return false;
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


    // http://lifelongprogrammer.blogspot.com/2013/11/java-use-zip-stream-and-base64-to-compress-big-string.html
    /**
     * When client receives the zipped base64 string, it first decode base64
     * String to byte array, then use ZipInputStream to revert the byte array to a
     * string.
     */
    public static String uncompressString(String zippedBase64Str) {
        String result = null;

        byte[] bytes = Base64.decodeBase64(zippedBase64Str);
        GZIPInputStream zi = null;
        try {
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            result = IOUtils.toString(zi);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(zi);
        }
        return result;
    }

    /**
     * At server side, use ZipOutputStream to zip text to byte array, then convert
     * byte array to base64 string, so it can be trasnfered via http request.
     */
    public static String compressString(String srcTxt) {
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos = null;
        try {
            zos = new GZIPOutputStream(rstBao);
            zos.write(srcTxt.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        IOUtils.closeQuietly(zos);

        byte[] bytes = rstBao.toByteArray();
        return Base64.encodeBase64String(bytes);
    }

}
