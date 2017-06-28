/**
 *  Copyright (c) 2011 Terracotta, Inc.
 *  Copyright (c) 2011 Oracle and/or its affiliates.
 *
 *  All rights reserved. Use is subject to license terms.
 */

package com.hualife.foundation.component.cache;


import java.util.Collection;
import java.util.Map;

import com.hualife.foundation.component.cache.config.CacheConfiguration.CacheWriteType;
import com.hualife.foundation.component.cache.config.model.Asyn;
import com.hualife.foundation.component.cache.config.model.ExtProperties;

/**
 * A CacheWriter is used for write-through and write-behind caching to a underlying resource.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @since 1.0
 */
public interface CacheWriter<K, V> {


    /**
     * Write the specified value under the specified key to the underlying store.
     * This method is intended to support both key/value creation and value update for a specific key.
     *
     * @param entry the entry to be written
     * @throws CacheException
     */
    void write(Cache.Entry<K, V> entry) throws CacheException;
    
    void update(Cache.Entry<K, V> entry) throws CacheException;

    /**
     * Write the specified entries to the underlying store. This method is intended to support both insert and update.
     * If this operation fails (by throwing an exception) after a partial success,
     * the convention is that entries which have been written successfully are to be removed from the specified entries,
     * indicating that the write operation for the entries left in the map has failed or has not been attempted.
     *
     * @param entries the entries to be written
     * @throws CacheException
     */
    void writeAll(Map<K, V> map) throws CacheException;


    /**
     * Delete the cache entry from the store
     *
     * @param key the key that is used for the delete operation
     * @throws CacheException
     */
    void delete(Object key) throws CacheException;


    /**
     * Remove data and keys from the underlying store for the given collection of keys, if present. If this operation fails
     * (by throwing an exception) after a partial success, the convention is that keys which have been erased successfully
     * are to be removed from the specified keys, indicating that the erase operation for the keys left in the collection
     * has failed or has not been attempted.
     *
     * @param entries the entries that have been removed from the cache
     * @throws CacheException
     */
    void deleteAll(Collection<? extends K> keys) throws CacheException;

    /**
     * Get extended properties.
     * @return properties
     */
    ExtProperties getExtProperties();
    
    /**
     * Set extended properties.
     * @param propreties
     */
    void setExtProperties(ExtProperties propreties);
    
    /**
     * Get cache write type.
     * @return cache write type
     */
    CacheWriteType getCacheWriteType();
    
    /**
     * Set cache write type.
     * @param type cache write type
     */
    void setCacheWriteType(CacheWriteType type);
    
    Asyn getAsyn();
    
    void setAsyn(Asyn asyn);
}
