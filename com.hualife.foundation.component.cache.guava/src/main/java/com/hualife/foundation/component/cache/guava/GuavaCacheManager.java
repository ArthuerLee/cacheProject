package com.hualife.foundation.component.cache.guava;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheBuilder;
import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.OptionalFeature;

public class GuavaCacheManager implements CacheManager {
//	private static ILogger logger = LoggerFactory.getLogger(GuavaCacheManager.class);
	private static Map<String, Cache> caches = new ConcurrentHashMap<String, Cache>();
//	private static Map<String, CacheConfiguration> cache_configurations_map = new ConcurrentHashMap<String, CacheConfiguration>();

	/**
     * Default name if not specified in the configuration
     */
    public static final String DEFAULT_NAME = "__DEFAULT__";
    private String name;
    private Status status;
    
	/**
     * The Singleton Instance.
     */
    private static CacheManager singleton;
    
    public GuavaCacheManager(String name) {
    	this.name = name;
    }
    
    public static CacheManager INSTANCE(){
    	if(null == singleton) {
    		singleton = new GuavaCacheManager(DEFAULT_NAME);
    	}
    	return singleton;
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
	public <K, V> CacheBuilder<K, V> createCacheBuilder(String cacheName) {
		return new GuavaCacheBuilder<K, V>(cacheName);
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

}
