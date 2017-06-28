/*******************************************************************************
 * $Header$
 * $Revision$
 * $Date$
 *
 *==============================================================================
 *
 * Copyright (c) 2005-2015 Primeton Technologies, Ltd.
 * All rights reserved.
 * 
 * Created on 2016年3月10日
 *******************************************************************************/


package com.hualife.foundation.component.cache.config;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.hualife.foundation.component.cache.TimeUtil;
import com.hualife.foundation.component.cache.config.model.Cache;
import com.hualife.foundation.component.cache.config.model.CacheListener;
import com.hualife.foundation.component.cache.config.model.CacheListeners;
import com.hualife.foundation.component.cache.config.model.CacheLoader;
import com.hualife.foundation.component.cache.config.model.CacheWriter;
import com.hualife.foundation.component.cache.config.model.Expiry;
import com.hualife.foundation.component.cache.config.model.ExpiryTimeType;
import com.hualife.foundation.component.cache.config.model.ListenerType;
import com.hualife.foundation.component.cache.config.model.SerializeType;
import com.hualife.foundation.component.cache.transaction.IsolationLevel;
import com.hualife.foundation.component.cache.transaction.Mode;

public class DefaultCacheConfiguration implements CacheConfiguration{
	private Cache model;

	@Override
	public String getName() {
		return model.getName();
	}

//	@Override
//	public CacheShareScope getCacheShareScope() {
//		Share share = model.getShare();
//		if(null == share){
//			return CacheShareScope.PUBLIC;
//		}
//		String scope = share.getScope();
//		if(null == scope || "".equals(scope)){
//			return CacheShareScope.PUBLIC;
//		}
//		return CacheShareScope.valueOf(scope);
//	}

//	@Override
//	public void setCacheShareScope(CacheShareScope scope) {
//		if(null != scope) {
//			if(null == model.getShare()){
//				Share share = new Share();
//				share.setScope(scope.name());
//				model.setShare(share);
//			}else{
//				model.getShare().setScope(scope.name());
//			}
//		}
//	}

	@Override
	public boolean isStoreByValue() {
		return false;
	}

	@Override
	public boolean isStatisticsEnabled() {
		return model.isStatisticsEnabled();
	}

	@Override
	public void setStatisticsEnabled(boolean enableStatistics) {
		model.setStatisticsEnabled(enableStatistics);
	}

	@Override
	public boolean isTransactionEnabled() {
		return model.isTransactionEnabled();
	}

	@Override
	public IsolationLevel getTransactionIsolationLevel() {
		return null;
	}

	@Override
	public Mode getTransactionMode() {
		return null;
	}

	@Override
	public void setExpiry(ExpiryType type, Duration duration) {
		model.getExpiry().setExpiryType(com.hualife.foundation.component.cache.config.model.ExpiryType.valueOf(type.name()));
		model.getExpiry().setExpiryTime(String.valueOf(duration.getTimeToLive()));
	}

	@Override
	public ExpiryType getExpiryType() {
		Expiry expiry = model.getExpiry();
		if(null == expiry){
			return ExpiryType.MODIFIED;
		}
		com.hualife.foundation.component.cache.config.model.ExpiryType expiryType2 = expiry.getExpiryType();
		if(null == expiryType2){
			return ExpiryType.MODIFIED;
		}
		String expiryType = expiryType2.name();
		if(null == expiryType || "".equals(expiryType)){
			return ExpiryType.MODIFIED;
		}
		return ExpiryType.valueOf(expiryType);
	}

	@Override
	public Duration getExpiry() {
		Expiry expiry = model.getExpiry();
		long expiryTime = 0;
		if(null != expiry) {
			ExpiryTimeType expiryTimeType = expiry.getExpiryTimeType();
			if(null != expiryTimeType){
				if(ExpiryTimeType.POINT_IN_TIME == expiryTimeType){
					Date date = TimeUtil.getDate(expiry.getExpiryTime());
					Date newDate = new Date();
					Long l = date.getTime()-newDate.getTime();
					expiryTime = l/1000;
				}else{
					expiryTime = Long.valueOf(expiry.getExpiryTime()).longValue();
				}
			}
		}
		return new Duration(TimeUnit.SECONDS, expiryTime);
	}

	public Cache getModel() {
		return model;
	}

	public void setModel(Cache model) {
		this.model = model;
	}

	@Override
	public String getCacheProvider() {
		return model.getProvider();
	}

	CacheListeners listeners = new CacheListeners();
	
	@Override
	public void setCacheEntryCreatedListener(CacheListener listener) {
		// TODO CacheListeners 没有Set方法，无法添加
//		model.setCacheListeners(listeners.add(e));
	}

	@Override
	public void setCacheEntryRemovedListener(CacheListener listener) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void setCacheEntryUpdatedListener(CacheListener listener) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void setCacheEntryReadListner(CacheListener listener) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void setCacheEntryExpiredListener(CacheListener listener) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public CacheListener getCacheEntryCreatedListener() {
		CacheListeners listeners = model.getCacheListeners();
		if(null == listeners){
			return null;
		}
		List<CacheListener> cacheListener = listeners.getCacheListener();
		if(null == cacheListener || cacheListener.size() <= 0){
			return null;
		}
		Iterator<CacheListener> i = cacheListener.iterator();
		while(i.hasNext()) {
			CacheListener listener = i.next();
			if(ListenerType.CREATED == listener.getListenerType()){
				return listener;
			}
		}
		return null;
	}

	@Override
	public CacheListener getCacheEntryRemovedListener() {
		CacheListeners listeners = model.getCacheListeners();
		if(null == listeners){
			return null;
		}
		List<CacheListener> cacheListener = listeners.getCacheListener();
		if(null == cacheListener || cacheListener.size() <= 0){
			return null;
		}
		Iterator<CacheListener> i = cacheListener.iterator();
		while(i.hasNext()) {
			CacheListener listener = i.next();
			if(ListenerType.REMOVED == listener.getListenerType()){
				return listener;
			}
		}
		return null;
	}

	@Override
	public CacheListener getCacheEntryUpdatedListener() {
		CacheListeners listeners = model.getCacheListeners();
		if(null == listeners){
			return null;
		}
		List<CacheListener> cacheListener = listeners.getCacheListener();
		if(null == cacheListener || cacheListener.size() <= 0){
			return null;
		}
		Iterator<CacheListener> i = cacheListener.iterator();
		while(i.hasNext()) {
			CacheListener listener = i.next();
			if(ListenerType.UPDATED == listener.getListenerType()){
				return listener;
			}
		}
		return null;
	}

	@Override
	public CacheListener getCacheEntryReadListener() {
		CacheListeners listeners = model.getCacheListeners();
		if(null == listeners){
			return null;
		}
		List<CacheListener> cacheListener = listeners.getCacheListener();
		if(null == cacheListener || cacheListener.size() <= 0){
			return null;
		}
		Iterator<CacheListener> i = cacheListener.iterator();
		while(i.hasNext()) {
			CacheListener listener = i.next();
			if(ListenerType.READ == listener.getListenerType()){
				return listener;
			}
		}
		return null;
	}

	@Override
	public CacheListener getCacheEntryExpiredListener() {
		CacheListeners listeners = model.getCacheListeners();
		if(null == listeners){
			return null;
		}
		List<CacheListener> cacheListener = listeners.getCacheListener();
		if(null == cacheListener || cacheListener.size() <= 0){
			return null;
		}
		Iterator<CacheListener> i = cacheListener.iterator();
		while(i.hasNext()) {
			CacheListener listener = i.next();
			if(ListenerType.EXPIRY == listener.getListenerType()){
				return listener;
			}
		}
		return null;
	}

	@Override
	public CacheLoader getCacheLoader() {
		return model.getCacheLoader();
	}

	@Override
	public void setCacheLoader(CacheLoader loader) {
		model.setCacheLoader(loader);
	}

	@Override
	public CacheWriter getCacheWriter() {
		return model.getCacheWriter();
	}

	@Override
	public void setCacheWriter(CacheWriter writer) {
		model.setCacheWriter(writer);
	}

	@Override
	public boolean isRestartWithClearData() {
		return model.isRestartWithClearData();
	}

	@Override
	public SerializeType getSerializeType() {
		SerializeType serializeType = model.getSerializeType();
		if(null == serializeType){
			return SerializeType.XML;
		}
		return serializeType;
	}
	
}

/*
 * 修改历史
 * $Log$ 
 */