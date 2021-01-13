package org.pac4j.play.store;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.util.serializer.JavaSerializer;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A session store which only uses the Play Session cookie for storage, allowing for a stateless backend.
 *
 * @author Vidmantas Zemleris
 * @since 6.1.0
 */
@Singleton
public class PlayCookieSessionStore implements SessionStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayCookieSessionStore.class);

    private final String tokenName = "pac4j";
    private final String keyPrefix = "pac4j_";
    private DataEncrypter dataEncrypter = new ShiroAesDataEncrypter();

    public static final JavaSerializer JAVA_SER_HELPER = new JavaSerializer();

    public PlayCookieSessionStore() {}

    public PlayCookieSessionStore(final DataEncrypter dataEncrypter) {
        this.dataEncrypter = dataEncrypter;
    }

    @Override
    public Optional<String> getSessionId(final WebContext context, final boolean createSession) {
        return Optional.of(tokenName);
    }

    @Override
    public Optional<Object> get(final WebContext context, final String key) {
        final Http.Session session = ((PlayWebContext) context).getNativeSession();
        final String sessionValue = session.get(keyPrefix + key).orElse(null);
        if (sessionValue == null) {
            LOGGER.debug("get, key = {} -> null", key);
            return Optional.empty();
        } else {
            byte[] inputBytes = Base64.getDecoder().decode(sessionValue);
            final Object value = JAVA_SER_HELPER.decodeFromBytes(uncompressBytes(dataEncrypter.decrypt(inputBytes)));
            LOGGER.debug("get, key = {} -> value = {}", key, value);
            return Optional.ofNullable(value);
        }
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        LOGGER.debug("set, key = {}, value = {}", key, value);
        Object clearedValue = value;
        if (key.contentEquals(Pac4jConstants.USER_PROFILES)) {
            clearedValue = clearUserProfiles(value);
        }

        byte[] javaSerBytes = JAVA_SER_HELPER.encodeToBytes((Serializable) clearedValue);
        String serialized = Base64.getEncoder().encodeToString(dataEncrypter.encrypt(compressBytes(javaSerBytes)));
        if (serialized != null) {
            LOGGER.debug("set, key = {} -> serialized token size = {}", key, serialized.length());
        } else {
            LOGGER.debug("set, key = {} -> null serialized token", key);
        }
        final PlayWebContext playWebContext = (PlayWebContext) context;
        playWebContext.setNativeSession(playWebContext.getNativeSession().adding(keyPrefix + key, serialized));
    }

    @Override
    public boolean destroySession(final WebContext context) {
        return false;
    }

    @Override
    public Optional<Object> getTrackableSession(final WebContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context, final Object trackableSession) {
        return Optional.empty();
    }

    @Override
    public boolean renewSession(final WebContext context) {
        return false;
    }

    protected Object clearUserProfiles(Object value) {
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) value;
        profiles.forEach((name, profile) -> profile.removeLoginData());
        return profiles;
    }

    public static byte[] uncompressBytes(byte [] zippedBytes) {
        final ByteArrayOutputStream resultBao = new ByteArrayOutputStream();
        try (GZIPInputStream zipInputStream = new GZIPInputStream(new ByteArrayInputStream(zippedBytes))) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                resultBao.write(buffer, 0, len);
            }
            return resultBao.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Unable to uncompress session cookie", e);
            return null;
        }
    }

    public static byte[] compressBytes(byte[] srcBytes) {
        final ByteArrayOutputStream resultBao = new ByteArrayOutputStream();
        try (GZIPOutputStream zipOutputStream = new GZIPOutputStream(resultBao)) {
            zipOutputStream.write(srcBytes);
        } catch (IOException e) {
            LOGGER.error("Unable to compress session cookie", e);
            return null;
        }

        return resultBao.toByteArray();
    }
}
