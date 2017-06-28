package com.hualife.foundation.component.cache.redis.config;

import com.hualife.foundation.component.cache.config.ConfigurationFactory;

import redis.clients.jedis.Jedis;


public class JedisOperation {
	
	public static Jedis getJedisConnection(String redisSingleCacheProvider){
		String cacheHost = ConfigurationFactory.getUserConfiguration().getConfigValue("CacheProvider", redisSingleCacheProvider, "CacheHost");
		return CacheHostManager.getJedis(cacheHost);
	}

	public static void closeJedisConnection(Jedis jedis,String redisSingleCacheProvider){
		String cacheHost = ConfigurationFactory.getUserConfiguration().getConfigValue("CacheProvider", redisSingleCacheProvider, "CacheHost");
		CacheHostManager.getJedisPool(cacheHost).returnResourceObject(jedis);
	}

}
