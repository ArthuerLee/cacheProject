/**
 *  Copyright (c) 2011 Terracotta, Inc.
 *  Copyright (c) 2011 Oracle and/or its affiliates.
 *
 *  All rights reserved. Use is subject to license terms.
 */

package com.hualife.foundation.component.cache;

import java.util.Collection;
import java.util.Map;

import com.hualife.foundation.component.cache.config.CacheConfiguration.CacheLoadType;
import com.hualife.foundation.component.cache.config.model.Asyn;
import com.hualife.foundation.component.cache.config.model.ExtProperties;

/**
 * Used for read-through caching and loading data into a cache.
 * <p/>
 * See CacheWriter which is used for write-through caching.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Greg Luck
 * @author Yannis Cosmadopoulos
 * @since 1.0
 */
public interface CacheLoader<K, V> {
	Map<K, V> preLoad();
    
	/**
     * Loads an object. Application writers should implement this
     * method to customize the loading of cache object. This method is called
     * by the caching service when the requested object is not in the cache.
     * <p/>
     *
     * @param key the key identifying the object being loaded
     * @return The entry for the object that is to be stored in the cache.
     */
    Cache.Entry<K, V> load(Object key);

    /**
     * Loads multiple objects. Application writers should implement this
     * method to customize the loading of cache object. This method is called
     * by the caching service when the requested object is not in the cache.
     * <p/>
     *
     * @param keys keys identifying the values to be loaded
     * @return A Map of objects that are to be stored in the cache.
     */
    Map<K, V> loadAll(Collection<? extends K> keys);
    
    Map<K, V> loadAll();

    /**
     *  Checks whether an object for the key can be loaded. May be used by an implementation
     *  when the actual value is not needed (e.g. {@link Cache#replace(Object, Object)}.
     *
     * @param key the key to check
     * @return true if the key can be used to load a value
     */
    boolean canLoad(Object key);
    
    /**
     * Get extended properties.
     * @return properties
     */
    public ExtProperties getExtProperties();
    
    /**
     * Set extended properties.
     * @param propreties
     */
    public void setExtProperties(ExtProperties propreties);
    
    /**
     * Get cache load type.
     * @return cache load type
     */
    public CacheLoadType getCacheLoadType();
    
    /**
     * set cache load type.
     * @param type cache load type
     */
    public void setCacheLoadType(CacheLoadType type);
    
    Asyn getAsyn();
    
    void setAsyn(Asyn asyn);
}
