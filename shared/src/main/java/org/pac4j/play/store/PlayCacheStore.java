package org.pac4j.play.store;

import com.google.inject.Provider;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.store.AbstractStore;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.serializer.JsonSerializer;
import org.pac4j.core.util.serializer.Serializer;
import play.cache.SyncCacheApi;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Store using the Play Cache.
 *
 * @author Jerome Leleu
 * @since 3.0.0
 */
@ToString
public class PlayCacheStore<K, O> extends AbstractStore<K, O> {

    private final SyncCacheApi cache;
    private final Provider<SyncCacheApi> cacheProvider;

    @Getter
    @Setter
    private int timeout;

    @Getter
    @Setter
    private Serializer serializer = new JsonSerializer();

    @Inject
    public PlayCacheStore(final SyncCacheApi cacheApi) {
        this.cacheProvider = null;
        this.cache = cacheApi;
    }

    public PlayCacheStore(final Provider<SyncCacheApi> cacheProvider) {
        this.cache = null;
        this.cacheProvider = cacheProvider;
    }

    @Override
    protected void internalInit(final boolean forceReinit) {
        CommonHelper.assertTrue(this.timeout >= 0, "timeout must be greater than zero");
        if (this.cache == null && this.cacheProvider == null) {
            throw new TechnicalException("The cache and the cacheProvider must not both be null");
        }
    }

    @Override
    protected Optional<O> internalGet(final K key) {
        return getCache().get(computeKey(key));
    }

    @Override
    protected void internalSet(final K key, final O value) {
        getCache().set(computeKey(key), value, this.timeout);
    }

    @Override
    protected void internalRemove(final K key) {
        getCache().remove(computeKey(key));
    }

    protected String computeKey(final Object objKey) {
        if (objKey instanceof String) {
            return (String) objKey;
        } else {
            return serializer.serializeToString(objKey);
        }
    }

    public SyncCacheApi getCache() {
        return cache != null ? cache : cacheProvider.get();
    }
}
