package org.pac4j.play.store;

import lombok.Getter;
import lombok.Setter;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.serializer.JsonSerializer;
import org.pac4j.core.util.serializer.Serializer;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.api.libs.crypto.CookieSigner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A session store which only uses the Play Session cookie for storage, allowing for a stateless backend.
 *
 * @author Vidmantas Zemleris
 * @since 6.1.0
 */
@Singleton
@Getter
@Setter
public class PlayCookieSessionStore implements SessionStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayCookieSessionStore.class);

    private String sessionName = "pac4j";

    private DataEncrypter dataEncrypter;

    private Serializer serializer = new JsonSerializer();

    /**
     * For manual configuration via {@code setDataEncrypter(DataEncrypter)}.
     * The encrypter must use a persistent key shared by all application nodes.
     */
    public PlayCookieSessionStore() {}

    /**
     * Derive a stable encryption key from Play's application secret, using a
     * dedicated signing message so the encryption and cookie-signing keys differ.
     *
     * @param cookieSigner Play's configured cookie signer
     */
    @Inject
    public PlayCookieSessionStore(final CookieSigner cookieSigner) {
        this(deriveDataEncrypter(cookieSigner));
    }

    private static DataEncrypter deriveDataEncrypter(final CookieSigner cookieSigner) {
        CommonHelper.assertNotNull("cookieSigner", cookieSigner);
        try {
            final byte[] keyMaterial = cookieSigner.sign("play-pac4j-cookie-session-encryption").getBytes(StandardCharsets.UTF_8);
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(keyMaterial);
            return new JdkAesDataEncrypter(Arrays.copyOf(digest, 16));
        } catch (final NoSuchAlgorithmException e) {
            throw new TechnicalException("SHA-256 is required for cookie session encryption", e);
        }
    }

    public PlayCookieSessionStore(final DataEncrypter dataEncrypter) {
        CommonHelper.assertNotNull("dataEncrypter", dataEncrypter);
        this.dataEncrypter = dataEncrypter;
    }

    private DataEncrypter configuredDataEncrypter() {
        if (dataEncrypter == null) {
            throw new TechnicalException("Inject PlayCookieSessionStore or configure a DataEncrypter with a persistent shared key");
        }
        return dataEncrypter;
    }

    @Override
    public Optional<String> getSessionId(final WebContext context, final boolean createSession) {
        final Http.Session session = ((PlayWebContext) context).getNativeSession();
        if (session.get(sessionName).isPresent()) {
            return Optional.of(sessionName);
        } else if (createSession) {
            putSessionValues(context, new HashMap<>());
            return Optional.of(sessionName);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Object> get(final WebContext context, final String key) {
        final Map<String, Object> values = getSessionValues(context);
        final Object value = values.get(key);
        if (value instanceof Exception) {
            LOGGER.debug("Get value: {} for key: {}", value.toString(), key);
        } else {
            LOGGER.debug("Get value: {} for key: {}", value, key);
        }
        return Optional.ofNullable(value);
    }

    protected Map<String, Object> getSessionValues(final WebContext context) {
        final Http.Session session = ((PlayWebContext) context).getNativeSession();
        final String sessionValue = session.get(sessionName).orElse(null);
        Map<String, Object> values = null;
        if (sessionValue != null) {
            final DataEncrypter encrypter = configuredDataEncrypter();
            CommonHelper.assertNotNull("serializer", serializer);
            try {
                final byte[] inputBytes = Base64.getDecoder().decode(sessionValue);
                final byte[] uncompressed = uncompressBytes(encrypter.decrypt(inputBytes));
                if (uncompressed != null) {
                    values = (Map<String, Object>) serializer.deserializeFromBytes(uncompressed);
                }
            } catch (final RuntimeException e) {
                // A cookie encrypted with an old key, or malformed data, must not
                // prevent the user from creating a new session.
                LOGGER.debug("Unable to decode session cookie; discarding it", e);
            }
            if (values == null) {
                putSessionValues(context, null);
            }
        }
        if (values != null) {
            return values;
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        if (value instanceof Exception) {
            LOGGER.debug("set key: {} with value: {}", key, value.toString());
        } else {
            LOGGER.debug("set key: {}, with value: {}", key, value);
        }

        final Map<String, Object> values = getSessionValues(context);
        if (value == null) {
            // let's try to save some space by removing the key for a null value
            values.remove(key);
        } else {
            Object clearedValue = value;
            if (Pac4jConstants.USER_PROFILES.equals(key)) {
                clearedValue = clearUserProfiles(value);
            }
            values.put(key, clearedValue);
        }

        putSessionValues(context, values);
    }

    protected void putSessionValues(final WebContext context, final Map<String, Object> values) {
        String serialized = null;
        if (values != null) {
            final byte[] javaSerBytes = serializer.serializeToBytes(values);
            serialized = Base64.getEncoder().encodeToString(configuredDataEncrypter().encrypt(compressBytes(javaSerBytes)));
        }
        if (serialized != null) {
            LOGGER.trace("serialized token size = {}", serialized.length());
        } else {
            LOGGER.trace("-> null serialized token");
        }
        final PlayWebContext playWebContext = (PlayWebContext) context ;
        if (serialized == null) {
            playWebContext.setNativeSession(playWebContext.getNativeSession().removing(sessionName));
        } else {
            playWebContext.setNativeSession(playWebContext.getNativeSession().adding(sessionName, serialized));
        }
    }

    @Override
    public boolean destroySession(final WebContext context) {
        putSessionValues(context, null);
        return true;
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
