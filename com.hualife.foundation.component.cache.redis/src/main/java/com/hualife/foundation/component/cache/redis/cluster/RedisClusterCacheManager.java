package com.hualife.foundation.component.cache.redis.cluster;

import com.hualife.foundation.component.cache.CacheBuilder;
import com.hualife.foundation.component.cache.redis.AbstractRedisCacheManager;

public class RedisClusterCacheManager extends AbstractRedisCacheManager {

	public RedisClusterCacheManager(String name) {
		super(name);
	}

	@Override
	public <K, V> CacheBuilder<K, V> createCacheBuilder(String cacheName) {
		return new RedisClusterCacheBuilder<K, V>(cacheName);
	}

}
