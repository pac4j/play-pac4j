package org.pac4j.play.store;

import com.google.inject.Inject;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import java.io.*;
import java.util.Base64;
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
		try {
			if (sessionValue == null) {
				return null;
			} else {
				return fromString(sessionValue);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
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

		try {
			session.put(session_key_prefix + key, toString((Serializable) clearedValue));
		} catch (IOException e) {
			e.printStackTrace();
		}
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

    /**
     * Read the object from Base64 string.
     * https://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
     */
    private static Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a Base64 string.
     */
    private static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
