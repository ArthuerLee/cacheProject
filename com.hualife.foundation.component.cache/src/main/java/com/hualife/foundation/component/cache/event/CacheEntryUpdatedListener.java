/**
 *  Copyright (c) 2011 Terracotta, Inc.
 *  Copyright (c) 2011 Oracle and/or its affiliates.
 *
 *  All rights reserved. Use is subject to license terms.
 */

package com.hualife.foundation.component.cache.event;

import com.hualife.foundation.component.cache.Cache;

/**
 * Invoked if an existing cache entry is updated,
 * for example through a {@link Cache#put(Object, Object)} operation.
 * It is not invoked by a {@link Cache#remove(Object)} operation.
 *
 * @param <K> the type of keys maintained by the associated cache
 * @param <V> the type of values maintained by the associated cache
 * @see CacheEntryCreatedListener
 * @author Yannis Cosmadopoulos
 * @author Greg Luck
 * @since 1.0
 */
public interface CacheEntryUpdatedListener<K, V> extends CacheEntryListener<K, V> {

    /**
     * Called after the entry has been updated (put into the cache where a previous mapping existed).
     * This method is not called if a batch operation was performed.
     *
     * @param entry The entry just updated.
     * @see #onUpdateAll(Iterable)
     */
    void beforeUpdate(Cache.Entry<K, V> entry);

    void afterUpdate(Cache.Entry<K, V> entry);

}
