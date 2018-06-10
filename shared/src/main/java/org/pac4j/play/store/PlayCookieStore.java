package org.pac4j.play.store;

import com.google.inject.Inject;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.JavaSerializationHelper;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import java.io.Serializable;
import java.util.LinkedHashMap;

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

    private final String session_key_prefix = "pac4j_";

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
        String sessionValue = session.get(session_key_prefix + key);
        if (sessionValue == null) {
            return null;
        } else {
            return JAVA_SERIALIZATION_HELPER.unserializeFromBase64(sessionValue);
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

        session.put(session_key_prefix + key, JAVA_SERIALIZATION_HELPER.serializeToBase64((Serializable) clearedValue));
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

}
