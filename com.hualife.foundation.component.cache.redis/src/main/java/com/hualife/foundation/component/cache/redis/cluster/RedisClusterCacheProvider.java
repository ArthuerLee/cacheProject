package com.hualife.foundation.component.cache.redis.cluster;

import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.OptionalFeature;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.redis.IRedisClusterCacheProvider;

public class RedisClusterCacheProvider implements IRedisClusterCacheProvider {
	private RedisClusterCacheManager manager = new RedisClusterCacheManager("RedisCluster");
	private String cluster;
	private String name;
	
	public RedisClusterCacheProvider(String name){
		this.name = name;
		String clusterName = ConfigurationFactory.getUserConfiguration().getConfigValue("CacheProvider", name, "CacheCluster");
		setCluster(clusterName);
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
	public String getCluster() {
		return this.cluster;
	}

	@Override
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	@Override
	public String getName() {
		return name;
	}
	
	

}
