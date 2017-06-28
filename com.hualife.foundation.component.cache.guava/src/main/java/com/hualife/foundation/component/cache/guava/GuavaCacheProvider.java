package com.hualife.foundation.component.cache.guava;

import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.OptionalFeature;
import com.hualife.foundation.component.cache.provider.CachingProvider;

/**
 *	Guava Cache's provider.
 *	Get cache by:
 *		GuavaCacheManager.INSTANCE().getCache(cacheName);
 */
public class GuavaCacheProvider implements CachingProvider {
	private CacheManager manager = new GuavaCacheManager("Guava");
	private String name;
	
	public GuavaCacheProvider(String name){
		this.name = name;
	}
	
	@Override
	public CacheManager createCacheManager() {
		return manager;
	}

	@Override
	public boolean isSupported(OptionalFeature optionalFeature) {
		return false;
	}

	@Override
	public CacheManager getCacheManager() {
		return manager;
	}

	@Override
	public String getName() {
		return name;
	}
}
