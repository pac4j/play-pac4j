package org.pac4j.play.store;

import javax.inject.Inject;
import com.google.inject.Provider;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.store.AbstractStore;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.PlayWebContext;
import play.cache.CacheApi;

import java.io.Serializable;

/**
 * Store using the Play Cache.
 *
 * @author Jerome Leleu
 * @since 3.0.0
 */
public class PlayCacheStore<K, O> extends AbstractStore<K, O> {

    private final CacheApi cache;
    private final Provider<CacheApi> cacheProvider;
    private int timeout;

    @Inject
    public PlayCacheStore(final CacheApi cacheApi) {
        this.cacheProvider = null;
        this.cache = cacheApi;
    }

    public PlayCacheStore(final Provider<CacheApi> cacheProvider) {
        this.cache = null;
        this.cacheProvider = cacheProvider;
    }

    @Override
    protected void internalInit() {
        CommonHelper.assertTrue(this.timeout >= 0, "timeout must be greater than zero");
        if (this.cache == null && this.cacheProvider == null) {
            throw new TechnicalException("The cache and the cacheProvider must not be both null");
        }
    }

    @Override
    protected O internalGet(final K key) {
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
            return PlayWebContext.JAVA_SERIALIZATION_HELPER.serializeToBase64((Serializable) objKey);
        }
    }

    public CacheApi getCache() {
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
        return CommonHelper.toString(this.getClass(), "cache", getCache(), "timeout", timeout);
    }
}
