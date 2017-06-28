package com.hualife.foundation.component.cache.guava;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eos.system.utility.StringUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hualife.foundation.component.cache.AbstractCache;
import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.LocalCache;
import com.hualife.foundation.component.cache.config.CacheConfiguration.ExpiryType;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;

public class GuavaCache<K, V> extends AbstractCache<K, V> implements LocalCache<K, V> {
	private Cache<K, V> localCache;
	
	@Override
	public void doStart() {
		long time = Long.MAX_VALUE;
		TimeUnit unit = TimeUnit.SECONDS;
		
		CacheBuilder<Object, Object> newBuilder = CacheBuilder.newBuilder();
		if(null != getExpiryType()) {
			if(null != getExpiryTime()) {
				long trueTime = getExpiryTime().getTimeToLive();
				unit = getExpiryTime().getTimeUnit();
				if(trueTime > 0){
					if(ExpiryType.MODIFIED == getExpiryType()) {
						newBuilder.expireAfterWrite(trueTime, unit);
					}else{
						newBuilder.expireAfterAccess(trueTime, unit);
					}
				}else {
					newBuilder.expireAfterWrite(time, unit);
				}
			}else {
				newBuilder.expireAfterWrite(time, unit);
			}
		}else {
			newBuilder.expireAfterWrite(time, unit);
		}
		String providerName = getCacheProvider().getName();
		String maxSizeString = ConfigurationFactory.getUserConfiguration().getConfigValue("CacheProvider", providerName, "MaxMumSize");
		int maxMumSize = 0;
		if(StringUtil.isNotNullAndBlank(maxSizeString)) {
			maxMumSize = Integer.valueOf(maxSizeString);
		}
		if(maxMumSize > 0){
			newBuilder.maximumSize(maxMumSize);
		}
			
		localCache = newBuilder.build();
	}

	@Override
	public void doStop() {
		removeAll();
	}
	
	@Override
	public Set<K> keySet() {
		return localCache.asMap().keySet();
	}

	@Override
	public synchronized void putValue(K key, V value) {
		localCache.put(key, value);
	}

	@Override
	public V getValue(K key) {
		return localCache.getIfPresent(key);
	}

	@Override
	public V atomicGetAndRemoveValue(K key) {
		synchronized (localCache) {
			V value = localCache.getIfPresent(key);
			localCache.invalidate(key);;
			return value;
		}
	}

	@Override
	public V atomicGetAndReplaceValue(K key, V value) {
		synchronized (localCache) {
			V val = localCache.getIfPresent(key);
			if(null == val) {
				return null;
			}
			localCache.put(key, value);
			return val;
		}
	}

	@Override
	public boolean atomicCheckAndReplaceValue(K key, V oldValue, V newValue) throws CacheException {
		synchronized (localCache) {
			V value = getValue(key);
			if(null != value && oldValue.equals(value)){
				localCache.put(key, newValue);
				return true;
			}
			return false;
		}
	}

	@Override
	public void putAllValue(Map<K, V> map) {
		synchronized (localCache) {
			localCache.putAll(map);;
		}
	}

	@Override
	public void removeAllValue(Collection<? extends K> keys) {
		synchronized (localCache) {
			localCache.invalidateAll(keys);
		}
	}

	@Override
	public Map<K, V> getAllValue(Collection<? extends K> keys) {
		return localCache.getAllPresent(keys);
	}

	@Override
	public boolean atomicCheckAndRemoveValue(K key, V oldValue) {
		boolean rtn = false;
		synchronized (localCache) {
			V val = localCache.getIfPresent(key);
			if(null == val) {
				rtn = false;
			}else{
				if(oldValue.equals(val)){
					localCache.invalidate(key);
					rtn = true;
				}
			}
			return rtn;
		}
	}
	
	@Override
	public V atomicGetAndPutValue(K key, V value) {
		synchronized (localCache) {
			V val = localCache.getIfPresent(key);
			localCache.put(key, value);
			return val;
		}
	}
	
	@Override
	public boolean atomicPutIfAbsent(K key, V value) {
		boolean rtn = false;
		synchronized (localCache) {
			V val = localCache.getIfPresent(key);
			if(val == null){
				localCache.put(key, value);
				rtn = true;
			}
		}
		return rtn;
	}
	
	@Override
	public boolean atomicReplaceIfExist(K key, V value) {
		boolean rtn = false;
		synchronized (localCache) {
			V val = localCache.getIfPresent(key);
			if(val != null){
				localCache.put(key, value);
				rtn = true;
			}
		}
		return rtn;
	}
	
	@Override
	public boolean containsKey(K key) throws CacheException {
		return localCache.getIfPresent(key) != null;
	}

	@Override
	public void removeAll() throws CacheException {
		synchronized (localCache) {
			localCache.invalidateAll();
		}
	}
	
	@Override
	public boolean removeKey(K key) throws CacheException {
		boolean rtn = false;
		synchronized (localCache) {
			V val = localCache.getIfPresent(key);
			if(val != null){
				localCache.invalidate(key);
				rtn = true;
			}
		}
		return rtn;
	}

	@Override
	public Set<V> keySet(String pattern) {
//		List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");  
//		Collection<String> result = Collections2.filter(names,   
//			      Predicates.contains(new Pattern()));  
		Set<K> keys = localCache.asMap().keySet();
		Iterator<K> iterator = keys.iterator();
		Set<V> valuesSet = new HashSet<V>(); 
		while(iterator.hasNext()){
			K key = iterator.next();
			if(!(key instanceof String)){
				throw new CacheException("["+getName()+"] key is not string.");
			}
			Pattern pa = Pattern.compile(pattern);
			Matcher matcher = pa.matcher(String.valueOf(key));
			if(matcher.matches()){
				valuesSet.add(localCache.getIfPresent(key));
			}
		}
		return valuesSet;
	}
	
}
