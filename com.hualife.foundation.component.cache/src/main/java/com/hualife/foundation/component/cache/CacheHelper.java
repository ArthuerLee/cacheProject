package com.hualife.foundation.component.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hualife.foundation.component.cache.monitor.Monitor;
import com.hualife.foundation.component.cache.monitor.OperationType;


public class CacheHelper {
	public static <K, V> Cache<K, V> getCache(String cacheName){
		 
		return Caching.getCacheManager(cacheName).getCache(cacheName);
	}
	
	public static <V> Set<V> keys(String cacheName, String pattern){
		return (Set<V>) getCache(cacheName).keys(pattern);
	}
	
	public static  <V, K> V get(String cacheName, K k){
		@SuppressWarnings("unchecked")
		V v = (V)getCache(cacheName).get(k);
		Monitor.recordCacheStatistics(cacheName,OperationType.QUERY,v);
		return v;
	}
	
	public static <K, V> void put(String cacheName, K k, V v){
		getCache(cacheName).put(k, v);
	}
	
	public static <K, V> boolean putIfAbsent(String cacheName, K k, V v){
		 
		return getCache(cacheName).putIfAbsent(k, v);
	}
	
	public static <K, V> V getAndRemove(String cacheName, K k){
		return (V) getCache(cacheName).getAndRemove(k);
	}	

	public static <K, V> V getAndReplace(String cacheName, K k, V v){
		return (V) getCache(cacheName).getAndReplace(k, v);
	}	

	public static <K, V> V getAndPut(String cacheName, K k, V v){
		return (V) getCache(cacheName).getAndPut(k, v);
	}	

	public static <K, V> boolean replace(String cacheName, K k, V v){
		return getCache(cacheName).replace(k, v);
	}	

	public static <K, V> boolean replace(String cacheName, K k, V oldV, V v){
		return getCache(cacheName).replace(k,oldV,v);
	}	

	public static <K> boolean containsKey(String cacheName, K k){
		return getCache(cacheName).containsKey(k);
	}	

	public static <K> boolean remove(String cacheName, K k){
		return getCache(cacheName).remove(k);
	}	

	public static <K, V> boolean remove(String cacheName, K k, V v){
		return getCache(cacheName).remove(k,v);
	}	

	public static <K, V> void putAll(String cacheName, Map<K, V> map){
		getCache(cacheName).putAll((Map<Object, Object>) map);
	}	

	public static <K> void removeAll(String cacheName, Collection<? extends K> keys){
		getCache(cacheName).removeAll(keys);
	}

	public static <K, V> Map<K,V> getAll(String cacheName, Collection<? extends K> keys){
		return (Map<K, V>) getCache(cacheName).getAll(keys);
	}
	
	public static <K> String incr(String cacheName, K key) {
		// TODO Auto-generated method stub
		return getCache(cacheName).incr(key);
	}
}
