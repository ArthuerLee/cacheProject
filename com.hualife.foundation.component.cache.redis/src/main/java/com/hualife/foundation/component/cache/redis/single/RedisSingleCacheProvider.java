package com.hualife.foundation.component.cache.redis.single;

import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.OptionalFeature;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.redis.IRedisSingleCacheProvider;

public class RedisSingleCacheProvider implements IRedisSingleCacheProvider {
	private RedisSingleCacheManager manager = new RedisSingleCacheManager("RedisSingle");
	private String name;
	private String cacheHost;
	
	public RedisSingleCacheProvider(String name){
		this.name = name;
		String cacheHost = ConfigurationFactory.getUserConfiguration().getConfigValue("CacheProvider", name, "CacheHost");
		setCacheHost(cacheHost);
	}
	
	@Override
	public CacheManager createCacheManager() {
		return manager;
	}

	@Override
	public CacheManager getCacheManager() {
		return manager;
	}

	@Override
	public boolean isSupported(OptionalFeature optionalFeature) {
		return false;
	}

	@Override
	public String getCacheHost() {
		return cacheHost;
	}

	@Override
	public void setCacheHost(String cacheHost) {
		this.cacheHost = cacheHost;
	}

	@Override
	public String getName() {
		return name;
	}
	
}
