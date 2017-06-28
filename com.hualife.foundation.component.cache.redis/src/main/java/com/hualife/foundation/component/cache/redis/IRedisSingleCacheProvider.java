package com.hualife.foundation.component.cache.redis;

import com.hualife.foundation.component.cache.provider.CachingProvider;

public interface IRedisSingleCacheProvider extends CachingProvider{
	String getCacheHost();
	
	void setCacheHost(String cacheHost);
}
