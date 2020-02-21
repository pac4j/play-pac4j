package org.pac4j.play.store;

import com.google.inject.Provider;
import play.cache.SyncCacheApi;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This session store uses the specific {@link PlayEhCacheStore}.
 *
 * @author Jerome Leleu
 * @since 9.0.0
 */
@Singleton
public class PlayEhCacheSessionStore extends PlayCacheSessionStore {

    @Inject
    public PlayEhCacheSessionStore(final SyncCacheApi cache) {
        this.store = new PlayEhCacheStore<>(cache);
        setDefaultTimeout();
    }

    public PlayEhCacheSessionStore(final Provider<SyncCacheApi> cacheProvider) {
        this.store = new PlayEhCacheStore<>(cacheProvider);
        setDefaultTimeout();
    }
}
