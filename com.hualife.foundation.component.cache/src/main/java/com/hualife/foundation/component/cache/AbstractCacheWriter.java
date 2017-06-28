package com.hualife.foundation.component.cache;

import java.util.Collection;
import java.util.Map;

import com.hualife.foundation.component.cache.config.CacheConfiguration.CacheWriteType;
import com.hualife.foundation.component.cache.config.model.Asyn;
import com.hualife.foundation.component.cache.config.model.ExtProperties;

public abstract class AbstractCacheWriter<K, V> implements CacheWriter<K, V> {
	private ExtProperties extProperties;
	private CacheWriteType cacheWriteType;
	private Asyn asyn;
	
	@Override
	public void writeAll(Map<K, V> map) throws CacheException {
	}

	@Override
	public void deleteAll(Collection<? extends K> keys) throws CacheException {
	}

	@Override
	public ExtProperties getExtProperties() {
		return extProperties;
	}

	@Override
	public void setExtProperties(ExtProperties propreties) {
		this.extProperties = propreties;
	}

	@Override
	public CacheWriteType getCacheWriteType() {
		return cacheWriteType;
	}

	@Override
	public void setCacheWriteType(CacheWriteType type) {
		this.cacheWriteType = type;
	}

	@Override
	public Asyn getAsyn() {
		return asyn;
	}

	@Override
	public void setAsyn(Asyn asyn) {
		this.asyn = asyn;
	}

}
