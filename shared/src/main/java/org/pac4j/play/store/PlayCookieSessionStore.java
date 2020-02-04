package org.pac4j.play.store;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.JavaSerializationHelper;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A PlaySesssionStore which only uses the Play Session cookie for storage, allowing for a stateless backend.
 *
 * @author Vidmantas Zemleris
 * @since 6.1.0
 */
@Singleton
public class PlayCookieSessionStore implements PlaySessionStore {

    private static final Logger logger = LoggerFactory.getLogger(PlayCookieSessionStore.class);

    private final String tokenName = "pac4j";
    private final String keyPrefix = "pac4j_";
    private DataEncrypter dataEncrypter = new ShiroAesDataEncrypter();

    public static final JavaSerializationHelper JAVA_SER_HELPER = new JavaSerializationHelper();

    public PlayCookieSessionStore() {}

    public PlayCookieSessionStore(final DataEncrypter dataEncrypter) {
        this.dataEncrypter = dataEncrypter;
    }

    @Override
    public String getOrCreateSessionId(final PlayWebContext context) {
        return tokenName;
    }

    @Override
    public Optional<Object> get(final PlayWebContext context, final String key) {
        final Http.Session session = context.getJavaSession();
        String sessionValue = session.get(keyPrefix + key);
        if (sessionValue == null) {
            logger.trace("get, key = {} -> null", key);
            return Optional.empty();
        } else {
            byte[] inputBytes = Base64.decodeBase64(sessionValue);
            final Object value = JAVA_SER_HELPER.deserializeFromBytes(uncompressBytes(dataEncrypter.decrypt(inputBytes)));
            logger.trace("get, key = {} -> value = {}", key, value);
            return Optional.of(value);
        }
    }

    @Override
    public void set(final PlayWebContext context, final String key, final Object value) {
        logger.trace("set, key = {}, value = {}", key, value);
        Object clearedValue = value;
        if (key.contentEquals(Pac4jConstants.USER_PROFILES)) {
            clearedValue = clearUserProfiles(value);
        }

        final Http.Session session = context.getJavaSession();

        byte[] javaSerBytes = JAVA_SER_HELPER.serializeToBytes((Serializable) clearedValue);
        String serialized = Base64.encodeBase64String(dataEncrypter.encrypt(compressBytes(javaSerBytes)));
        if (serialized != null) {
            logger.trace("set, key = {} -> serialized token size = {}", key, serialized.length());
        } else {
            logger.trace("set, key = {} -> null serialized token", key);
        }
        session.put(keyPrefix + key, serialized);
    }

    @Override
    public boolean destroySession(PlayWebContext playWebContext) {
        return false;
    }

    @Override
    public Optional<Object> getTrackableSession(PlayWebContext playWebContext) {
        return Optional.empty();
    }

    @Override
    public Optional<SessionStore<PlayWebContext>> buildFromTrackableSession(PlayWebContext playWebContext, Object o) {
        return Optional.empty();
    }

    @Override
    public boolean renewSession(PlayWebContext playWebContext) {
        return false;
    }

    protected Object clearUserProfiles(Object value) {
        if (value instanceof LinkedHashMap<?, ?>) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) value;
            profiles.forEach((name, profile) -> profile.clearSensitiveData());
            return profiles;
        } else {
            CommonProfile profile = (CommonProfile) value;
            profile.clearSensitiveData();
            return profile;
        }
    }

    // based on http://lifelongprogrammer.blogspot.com/2013/11/java-use-zip-stream-and-base64-to-compress-big-string.html
    public static byte[] uncompressBytes(byte [] zippedBytes) {
        try (GZIPInputStream zipInputStream = new GZIPInputStream(new ByteArrayInputStream(zippedBytes))) {
            return IOUtils.toByteArray(zipInputStream);
        } catch (IOException e) {
            logger.error("Unable to uncompress session cookie", e);
            return null;
        }
    }

    public static byte[] compressBytes(byte[] srcBytes) {
        final ByteArrayOutputStream resultBao = new ByteArrayOutputStream();
        try (GZIPOutputStream zipOutputStream = new GZIPOutputStream(resultBao)) {
            zipOutputStream.write(srcBytes);
        } catch (IOException e) {
            logger.error("Unable to compress session cookie", e);
            return null;
        }

        return resultBao.toByteArray();
    }
}
