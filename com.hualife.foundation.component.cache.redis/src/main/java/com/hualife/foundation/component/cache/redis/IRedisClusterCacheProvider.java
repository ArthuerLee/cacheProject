package com.hualife.foundation.component.cache.redis;

import com.hualife.foundation.component.cache.provider.CachingProvider;

public interface IRedisClusterCacheProvider extends CachingProvider{
	
	String getCluster();
    
    void setCluster(String cluster);
}
