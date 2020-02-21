package org.pac4j.play.store;

import javax.inject.Inject;
import com.google.inject.Provider;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.store.AbstractStore;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.JavaSerializationHelper;
import play.cache.SyncCacheApi;

import java.io.Serializable;
import java.util.Optional;

/**
 * Store using the Play Cache.
 *
 * @author Jerome Leleu
 * @since 3.0.0
 */
public class PlayCacheStore<K, O> extends AbstractStore<K, O> {

    public static final JavaSerializationHelper JAVA_SERIALIZATION_HELPER = new JavaSerializationHelper();

    private final SyncCacheApi cache;
    private final Provider<SyncCacheApi> cacheProvider;
    private int timeout;

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
    protected void internalInit() {
        CommonHelper.assertTrue(this.timeout >= 0, "timeout must be greater than zero");
        if (this.cache == null && this.cacheProvider == null) {
            throw new TechnicalException("The cache and the cacheProvider must not both be null");
        }
    }

    @Override
    protected Optional<O> internalGet(final K key) {
        return getCache().getOptional(computeKey(key));
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
            return JAVA_SERIALIZATION_HELPER.serializeToBase64((Serializable) objKey);
        }
    }

    public SyncCacheApi getCache() {
        return cache != null ? cache : cacheProvider.get();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "cache", getCache(), "timeout", timeout);
    }
}
