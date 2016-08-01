package com.requestserver.cache;

import java.util.Collections;
import java.util.Map;

/**
 * Created by wally.yan on 2016/6/28.
 */

public class CacheEntry {

    /**
     * The key that identifies the cache entry.
     */
    public String key;

    /** The size of the data identified by this CacheEntry. (This is not
     * serialized to disk. */
    public long size;

    /** The data returned from cache. */
    public byte[] data;

    /** ETag for cache coherency. */
    public String etag;

    /** Date of this response as reported by the server. */
    public long serverDate;

    /** The last modified date for the requested object. */
    public long lastModified;

    /** TTL for this record. */
    public long ttl;

    /** Soft TTL for this record. */
    public long softTtl;

    /** Immutable response headers as received from server; must be non-null. */
    public Map<String, String> responseHeaders = Collections.emptyMap();

    /** True if the entry is expired. */
    public boolean isExpired() {
        return this.ttl < System.currentTimeMillis();
    }

    /** True if a refresh is needed from the original data source. */
    public boolean refreshNeeded() {
        return this.softTtl < System.currentTimeMillis();
    }
}
