package org.pac4j.play.store;

import com.google.inject.Provider;
import lombok.ToString;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import play.cache.SyncCacheApi;

import javax.inject.Inject;

/**
 * Store dedicated to EhCache.
 *
 * @author Jerome Leleu
 * @since 9.0.0
 */
@ToString
public class PlayEhCacheStore<K, O> extends PlayCacheStore<K, O> {

    @Inject
    public PlayEhCacheStore(final SyncCacheApi cacheApi) {
        super(cacheApi);
    }

    public PlayEhCacheStore(final Provider<SyncCacheApi> cacheProvider) {
        super(cacheProvider);
    }

    protected Ehcache getEhcache() {
        return CacheManager.getInstance().getEhcache("play");
    }

    @Override
    protected void internalSet(final K key, final O value) {
        final Element e = new Element(computeKey(key), value);
        // FIX: by default, the Play framework will use the setTimeToLive
        e.setTimeToIdle(getTimeout());
        getEhcache().put(e);
    }
}
