package com.hualife.foundation.component.cache.task;

import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.Caching;
import com.hualife.foundation.component.cache.RemoteCache;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.task.ChangeMessage.CacheMessageType;

public class DataSourceChangedNotifier {
	
	private DataSourceChangedNotifier(){}
	
	/**
	 * 数据源变更通知接口
	 * @param message
	 */
	public static <K, V> void notify(ChangeMessage<K, V> message) {
		if(null == message)
			throw new CacheException("message can not be null.");
		String cacheName = message.getCacheName();
		if(StringUtil.isNullOrBlank(cacheName))
			throw new CacheException("cache name can not be null.");
		Boolean isNotify = ConfigurationFactory.getCacheConfiguration(cacheName).getCacheLoader().isNotify();
		if(!isNotify) {
			throw new CacheException("["+cacheName+"] load mode is not notification.");
		}
		CacheManager manager = Caching.getCacheManager(cacheName);
		Cache<Object, Object> cache = manager.getCache(cacheName);
		if(null == cache) {
			throw new CacheException(cacheName + " is not exist.");
		}
		if(cache instanceof RemoteCache) {
			RemoteCache<K, V> remoteCache = (RemoteCache)cache;
			if(!remoteCache.checkSynPrevilige()) {
				return;
			}
		}
		K key = message.getEntry().getKey();
		V value = message.getEntry().getValue();
		if(CacheMessageType.ADDED == message.getType()){
			cache.put(key, value);
		}else if(CacheMessageType.REMOVED == message.getType()) {
			cache.remove(key);
		}else if(CacheMessageType.UPDATED == message.getType()) {
			cache.replace(key, value);
		}else {
			throw new CacheException("the message type of "+message.getType() +"is not support.");
		}
	}
}
