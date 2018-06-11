package org.pac4j.play.store;

import com.google.inject.Inject;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The Cookie storage uses directly the Play Session cookie (JWT encrypted) for
 * storage, allowing for a stateless backend.
 *
 * @author Nolan Barth
 * @since 2.6.2
 */
public class PlayCookieStore implements PlaySessionStore {

    private static final Logger logger = LoggerFactory.getLogger(PlayCookieStore.class);

    private final String tokenName = "pac4j";
    private final String keyPrefix = "pac4j_";

    public static final JavaSerializationHelper JAVA_SERIALIZATION_HELPER = new JavaSerializationHelper();

    @Inject
    public PlayCookieStore() {
    }

    @Override
    public String getOrCreateSessionId(final PlayWebContext context) {
        return tokenName;
    }

    @Override
    public Object get(final PlayWebContext context, final String key) {
        final Http.Session session = context.getJavaSession();
        String sessionValue = session.get(keyPrefix + key);
        if (sessionValue == null) {
            return null;
        } else {
            return JAVA_SERIALIZATION_HELPER.unserializeFromBytes(uncompressBase64ToBytes(sessionValue)); // FIXME: add IEncoder
        }
    }

    @Override
    public void set(final PlayWebContext context, final String key, final Object value) {
        Object clearedValue = value;
        if (key.contentEquals(Pac4jConstants.USER_PROFILES)) {
            clearedValue = clearUserProfiles(value);
        }
        logger.debug("PlayCookieStore.set, key = {}, value = {}", key, clearedValue);

        final Http.Session session = context.getJavaSession();
        String serialized = compressBytesToBase64(JAVA_SERIALIZATION_HELPER.serializeToBytes((Serializable) clearedValue)); // FIXME: add IEncoder
        if (serialized != null) {
            logger.debug("PlayCookieStore.set, key = {}, serialized token size = {}", key, serialized.length());
        }
        session.put(keyPrefix + key, serialized);
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

    // http://lifelongprogrammer.blogspot.com/2013/11/java-use-zip-stream-and-base64-to-compress-big-string.html
    /**
     * When client receives the zipped base64 string, it first decode base64
     * String to byte array, then use ZipInputStream to revert the byte array to a
     * string.
     */
    public static byte[] uncompressBase64ToBytes(String zippedBase64Str) {
        byte[] bytes = Base64.decodeBase64(zippedBase64Str);
        GZIPInputStream zi = null;
        try {
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            return IOUtils.toByteArray(zi);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(zi);
        }
    }

    /**
     * At server side, use ZipOutputStream to zip text to byte array, then convert
     * byte array to base64 string, so it can be trasnfered via http request.
     */
    public static String compressBytesToBase64(byte[] srcBytes) {
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos = null;
        try {
            zos = new GZIPOutputStream(rstBao);
            zos.write(srcBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        IOUtils.closeQuietly(zos);

        byte[] bytes = rstBao.toByteArray();
        return Base64.encodeBase64String(bytes);
    }


}
