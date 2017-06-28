/**
 *  Copyright (c) 2011 Terracotta, Inc.
 *  Copyright (c) 2011 Oracle and/or its affiliates.
 *
 *  All rights reserved. Use is subject to license terms.
 */

package com.hualife.foundation.component.cache.provider;

import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.OptionalFeature;

/**
 * Interface that should be implemented by a CacheManager factory provider.
 *
 * It is invoked by the {@link javax.cache.Caching} class to create
 * a {@link CachingProvider}
 * <p/>
 * An implementation of this interface must have a public no-arg constructor.
 * <p/>
 * @see javax.cache.Caching
 *
 * @author Yannis Cosmadopoulos
 * @since 1.0
 */
public interface CachingProvider {
	
	String getName();
    /**
     * Called by the {@link javax.cache.Caching} class when a
     * new CacheManager needs to be created.
     * <p/>
     * The name may be used to associate a configuration with this CacheManager instance.
     *
     * @return a new cache manager.
     * @throws NullPointerException if classLoader or name is null
     * @see javax.cache.Caching#getCacheManager(ClassLoader, String)
     */
	CacheManager createCacheManager();

    /**
     * CachingProvider与CacheManager是一对一的关系.
     * @return
     */
    CacheManager getCacheManager();

    /**
     * Called by the {@link javax.cache.Caching} class when a
     * new CacheManager needs to be created and the ClassLoader is not specified.
     * <p/>
     * Possible strategies include the following:
     *<pre>
     *     Thread.currentThread().getContextClassLoader();
     *     getClass().getClassLoader();
     *</pre>
     * Returns the default classloader to use if
     * @return the default ClassLoader
     * @see javax.cache.Caching#getCacheManager()
     */
//    ClassLoader getDefaultClassLoader();

    /**
     * Indicates whether a optional feature is supported by this implementation
     * @param optionalFeature the feature to check for
     * @return true if the feature is supported
     */
    boolean isSupported(OptionalFeature optionalFeature);
}
