package com.hualife.foundation.component.cache;

import java.util.Collection;
import java.util.Map;

import com.hualife.foundation.component.cache.Cache.Entry;
import com.hualife.foundation.component.cache.config.CacheConfiguration.CacheLoadType;
import com.hualife.foundation.component.cache.config.model.Asyn;
import com.hualife.foundation.component.cache.config.model.ExtProperties;

public abstract class AbstractCacheLoader<K, V> implements CacheLoader<K, V> {
	private ExtProperties extProperties;
	private CacheLoadType cacheLoadType;
	private Asyn asyn;

	@Override
	public Entry<K, V> load(Object key) {
		return null;
	}

	@Override
	public Map<K, V> loadAll(Collection<? extends K> keys) {
		return null;
	}

	@Override
	public boolean canLoad(Object key) {
		return true;
	}

	@Override
	public ExtProperties getExtProperties() {
		return extProperties;
	}

	@Override
	public void setExtProperties(ExtProperties propreties) {
		extProperties = propreties;
	}

	@Override
	public CacheLoadType getCacheLoadType() {
		return cacheLoadType;
	}

	@Override
	public void setCacheLoadType(CacheLoadType type) {
		this.cacheLoadType = type;
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
