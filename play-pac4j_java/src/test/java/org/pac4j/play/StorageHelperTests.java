package org.pac4j.play;

import org.junit.Assert;
import org.junit.Test;

public class StorageHelperTests {

    private static final String CACHE_KEY = "cacheKey";

    @Test
    public void getCacheKey_whenNullCacheKeyPrefix_returnsCacheKey() {
        Config.setCacheKeyPrefix(null);
        Assert.assertEquals(CACHE_KEY, StorageHelper.getCacheKey(CACHE_KEY));
    }

    @Test
    public void getCacheKey_whenBlankCacheKeyPrefix_returnsCacheKey() {
        Config.setCacheKeyPrefix("  ");
        Assert.assertEquals(CACHE_KEY, StorageHelper.getCacheKey(CACHE_KEY));
    }

    @Test
    public void getCacheKey_whenNonBlankCacheKeyPrefix_returnsCacheKeyWithPrefix() {
        String keyPrefix = "keyPrefix";
        Config.setCacheKeyPrefix(keyPrefix);
        Assert.assertEquals(keyPrefix + ":" + CACHE_KEY, StorageHelper.getCacheKey(CACHE_KEY));
    }

}
