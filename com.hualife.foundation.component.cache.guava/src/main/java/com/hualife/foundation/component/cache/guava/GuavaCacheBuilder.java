package com.hualife.foundation.component.cache.guava;

import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheBuilder;
import com.hualife.foundation.component.cache.CacheLoader;
import com.hualife.foundation.component.cache.CacheManager;
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

public class GuavaCacheBuilder<K, V> implements CacheBuilder<K, V> {
	private GuavaCache<K,V> cache = new GuavaCache<K, V>();
	private CacheManager manager;
	
	public GuavaCacheBuilder(String cacheName){
		cache.setName(cacheName);
	}
	/**
	 * CacheBuilder构造方法
	 * @param cacheName cache名称
	 * @param provider 使用API创建Cache时，需要指定相应的CachingProvider，才能对新建的Cache进行管理
	 * API的方式创建缓存，可以通过Caching获取CachingProvider.
	 */
//	public GuavaCacheBuilder(String cacheName, CachingProvider provider){
//		cache.setName(cacheName);
//		if(null == provider) {
//			throw new CacheException("Cache provider can not be null.");
//		}
//		CacheManager manager = provider.createCacheManager(Thread.currentThread().getContextClassLoader());
//		manager.addCache(cache);
//		Caching.addProvider(provider.getClass().getCanonicalName(), provider);
//	}
	
	/**
	 * CacheBuilder构造方法
	 * @param cacheName cache名称
	 * @param providerName Cache配置中已存在的Provider类全名
	 */
//	public GuavaCacheBuilder(String cacheName, String providerName){
//		cache.setName(cacheName);
//		if(null == providerName || "".equals(providerName)) {
//			throw new CacheException("Cache provider can not be null.");
//		}
//		CachingProvider provider = Caching.getProvider(providerName);
//		if(null == provider) {
//			throw new CacheException("The cache provider of " + providerName +" is not exist.");
//		}
//		provider.getCacheManager().addCache(cache);
//	}
	
	@Override
	public Cache<K, V> build() {
		// 创建完Cache之后，加入到CacheManager，此处无法获取CacheManager
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

}
