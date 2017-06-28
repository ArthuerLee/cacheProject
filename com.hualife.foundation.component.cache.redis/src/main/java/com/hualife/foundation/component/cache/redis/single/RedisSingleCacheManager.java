package com.hualife.foundation.component.cache.redis.single;

import com.hualife.foundation.component.cache.CacheBuilder;
import com.hualife.foundation.component.cache.redis.AbstractRedisCacheManager;

public class RedisSingleCacheManager extends AbstractRedisCacheManager {

	public RedisSingleCacheManager(String name) {
		super(name);
	}

	@Override
	public <K, V> CacheBuilder<K, V> createCacheBuilder(String cacheName) {
		return new RedisSingleCacheBuilder<K, V>(cacheName);
	}

}
