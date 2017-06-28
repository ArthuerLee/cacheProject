package com.hualife.foundation.component.cache.redis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.OptionalFeature;

public abstract class AbstractRedisCacheManager implements CacheManager{
	private static Map<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

	/**
     * Default name if not specified in the configuration
     */
    public static final String DEFAULT_NAME = "__DEFAULT__";
    private String name;
    private Status status;
    
    
    public AbstractRedisCacheManager(String name) {
    	this.name = name;
    }
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Status getStatus() {
		while(status == Status.UNINITIALISED) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//ignore
			}
		}
		return status;
	}

	@Override
	public <K, V> Cache<K, V> getCache(String cacheName) {
		return caches.get(cacheName);
	}

	@Override
	public <K, V> Set<Cache<K, V>> getCaches() {
		return new HashSet(caches.values());
	}

	@Override
	public boolean removeCache(String cacheName) throws IllegalStateException {
		Cache cache = caches.remove(cacheName);
		cache.stop();
		return true;
	}

	@Override
	public boolean isSupported(OptionalFeature optionalFeature) {
		return false;
	}

	@Override
	public void shutdown() {
		Iterator<Cache> i = caches.values().iterator();
		while(i.hasNext()) {
			i.next().stop();
		}
		caches.clear();
	}


	@Override
	public <K, V> void addCache(Cache<K, V> cache) {
		caches.put(cache.getName(), cache);
	}

//	public static final String SYSTEM_CACHE_NAME = "__SYSTEM_CACHE_NAME__"; 
    
//    
//	public Cache<String, String> getSystemCache() {
//		if(caches.containsKey(ConfigurationFactory.SYSTEM_CACHE_NAME)) {
//			return caches.get(ConfigurationFactory.SYSTEM_CACHE_NAME);
//		}
//		return null;
//	}

}
