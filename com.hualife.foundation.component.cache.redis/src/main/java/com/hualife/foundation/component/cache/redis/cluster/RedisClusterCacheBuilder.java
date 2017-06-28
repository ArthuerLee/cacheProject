package com.hualife.foundation.component.cache.redis.cluster;

import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheBuilder;
import com.hualife.foundation.component.cache.CacheLoader;
import com.hualife.foundation.component.cache.CacheWriter;
import com.hualife.foundation.component.cache.config.CacheConfiguration;
import com.hualife.foundation.component.cache.config.CacheConfiguration.Duration;
import com.hualife.foundation.component.cache.config.CacheConfiguration.ExpiryType;
import com.hualife.foundation.component.cache.event.CacheEntryCreatedListener;
import com.hualife.foundation.component.cache.event.CacheEntryExpiredListener;
import com.hualife.foundation.component.cache.event.CacheEntryListener;
import com.hualife.foundation.component.cache.event.CacheEntryReadListener;
import com.hualife.foundation.component.cache.event.CacheEntryRemovedListener;
import com.hualife.foundation.component.cache.event.CacheEntryUpdatedListener;
import com.hualife.foundation.component.cache.transaction.IsolationLevel;
import com.hualife.foundation.component.cache.transaction.Mode;

public class RedisClusterCacheBuilder<K, V> implements CacheBuilder<K, V> {
	private RedisClusterCache<K,V> cache = new RedisClusterCache<K, V>();
	
	public RedisClusterCacheBuilder(String cacheName){
		cache.setName(cacheName);
	}
	
	@Override
	public Cache<K, V> build() {
//		cache.start();
		return cache;
	}
	
	@Override
	public Cache<K, V> build(CacheConfiguration configuration) {
		cache.setConfiguration(configuration);
		return cache;
	}

	@Override
	public CacheBuilder<K, V> setCacheLoader(CacheLoader<K, V> cacheLoader) {
		cache.setCacheLoader(cacheLoader);
		return this;
	}

	@Override
	public CacheBuilder<K, V> setCacheWriter(CacheWriter<K, V> cacheWriter) {
		cache.setCacheWriter(cacheWriter);
		return this;
	}

	@Override
	public CacheBuilder<K, V> setStoreByValue(boolean storeByValue) {
		return this;
	}

	@Override
	public CacheBuilder<K, V> setTransactionEnabled(
			IsolationLevel isolationLevel, Mode mode) {
		cache.setTransactionEnabled(false);
		return this;
	}

	@Override
	public CacheBuilder<K, V> setStatisticsEnabled(boolean enableStatistics) {
		cache.setStatisticsEnabled(enableStatistics);
		return this;
	}

	@Override
	public CacheBuilder<K, V> setReadThrough(boolean readThrough) {
		return this;
	}

	@Override
	public CacheBuilder<K, V> setWriteThrough(boolean writeThrough) {
		return this;
	}

	@Override
	public CacheBuilder<K, V> setExpiry(ExpiryType type, Duration timeToLive) {
		cache.setExpiryType(type);
		cache.setExpiryTime(timeToLive);
		return this;
	}

	@Override
	public CacheBuilder<K, V> registerCacheEntryListener(
			CacheEntryListener<K, V> cacheEntryListener, boolean synchronous) {
		if(cacheEntryListener instanceof CacheEntryCreatedListener){
			cache.setCreatedListener((CacheEntryCreatedListener)cacheEntryListener);
		}else if(cacheEntryListener instanceof CacheEntryRemovedListener){
			cache.setRemovedListener((CacheEntryRemovedListener)cacheEntryListener);
		}else if(cacheEntryListener instanceof CacheEntryUpdatedListener) {
			cache.setUpdatedListener((CacheEntryUpdatedListener)cacheEntryListener);
		}else if(cacheEntryListener instanceof CacheEntryReadListener){
			cache.setReadListener((CacheEntryReadListener)cacheEntryListener);
		}else if(cacheEntryListener instanceof CacheEntryExpiredListener){
			cache.setExpiryListener((CacheEntryExpiredListener)cacheEntryListener);
		}
		return this;
	}

}
