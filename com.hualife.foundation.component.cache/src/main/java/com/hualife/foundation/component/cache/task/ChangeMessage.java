package com.hualife.foundation.component.cache.task;

import com.hualife.foundation.component.cache.Cache;

public class ChangeMessage<K, V> {
	private CacheMessageType type;
	private Cache.Entry<K, V> entry;
	private String cacheName;
	
	public ChangeMessage(){
		// 序列化/反序列化用
	}
	
	public ChangeMessage(String cacheName, Cache.Entry<K, V> entry, CacheMessageType type) {
		this.cacheName = cacheName;
		this.entry = entry;
		this.type = type;
	}
	
	public CacheMessageType getType() {
		return type;
	}

	public void setType(CacheMessageType type) {
		this.type = type;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public Cache.Entry<K, V> getEntry() {
		return entry;
	}

	public void setEntry(Cache.Entry<K, V> entry) {
		this.entry = entry;
	}

	public enum CacheMessageType {
		ADDED,
		REMOVED,
		UPDATED,
	}
}
