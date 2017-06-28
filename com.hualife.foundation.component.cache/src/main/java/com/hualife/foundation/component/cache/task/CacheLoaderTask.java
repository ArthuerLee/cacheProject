package com.hualife.foundation.component.cache.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import com.eos.data.serialize.XMLSerializer;
import com.hualife.foundation.component.cache.AbstractCache.CacheEntry;
import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheKeySetSupportable;
import com.hualife.foundation.component.cache.CacheLoader;
import com.hualife.foundation.component.cache.monitor.Monitor;
import com.hualife.foundation.component.cache.monitor.OperationType;
import com.hualife.foundation.component.cache.task.ChangeMessage.CacheMessageType;
//import com.primeton.btp.api.core.logger.ILogger;
//import com.primeton.btp.api.core.logger.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import commonj.sdo.DataObject;

public class CacheLoaderTask extends AbstractSynchrodataTask {
	private CacheLoader cacheLoader;
	private Cache cache;
	private boolean flag = true;
	//private ILogger logger = LoggerFactory.getLogger(CacheLoaderTask.class);
	private Logger logger = LoggerFactory.getLogger(CacheLoaderTask.class);
	
	public CacheLoaderTask(Cache cache, CacheLoader loader ,int period) {
		super(period);
		this.cache = cache;
		this.cacheLoader = loader;
	}
	
	@Override
	public void doSth() {
		if(logger.isDebugEnabled()){
			logger.debug("["+cache.getName()+"] start cache loader task.");
		}
		Map srcData = cacheLoader.loadAll();
		if(null == srcData) {
			cache.removeAll();
			if(logger.isDebugEnabled()){
				logger.debug("["+cache.getName()+"] end cache loader task , clear all data.");
			}
			return;
		}
		List<ChangeMessage> updatedData = compare(srcData);
		Iterator<ChangeMessage> messages = updatedData.iterator();
		int added = 0;
		int removed = 0;
		int updated = 0;
		boolean isException =false;
		if(updatedData.size()>0){
			Monitor.recordCacheStatistics(cache.getName(), OperationType.LOAD, null);
		}
		while(messages.hasNext()) {
			ChangeMessage message = messages.next();
			CacheMessageType type = message.getType();
			Object key = message.getEntry().getKey();
			Object value = message.getEntry().getValue();
			try {
				switch (type) {
				case ADDED:
					this.cache.put(key, value);
					added ++;
					break;
				case REMOVED:
					this.cache.remove(key);
					removed ++;
					break;
				case UPDATED:
					this.cache.replace(key, value);
					updated ++;
					break;
				}
			} catch (Exception e) {
				isException = true;
				if(logger.isErrorEnabled()){
					logger.error("["+cache.getName()+"] load (" + message.getType().name().toLowerCase() +") key [" +key+"] error.",e);
				}
			}
		}
	       if(isException){
	    	   Monitor.recordCacheStatistics(cache.getName(), OperationType.LOAD_EXCEPTION, null);
	       }
		if(logger.isInfoEnabled()){
			logger.info("["+cache.getName()+"] end cache loader task. added: ["+added+"] ,removed: ["+removed+"] ,updated: ["+updated+"].");
		}
	}

	private List<ChangeMessage> compare(Map srcData) {
		List<ChangeMessage> messages = new ArrayList<ChangeMessage>();
		Iterator i = srcData.keySet().iterator();
		Set keys = new HashSet();
		if(cache instanceof CacheKeySetSupportable){
			CacheKeySetSupportable c = (CacheKeySetSupportable) cache;
			keys = new HashSet(c.keySet());
		}
		while(i.hasNext()) {
			Object key = i.next();
			Object oldValue = cache.get(key);
			Object newValue = srcData.get(key);
			if(null == oldValue) {
				// ADD
				ChangeMessage message = new ChangeMessage(cache.getName(),new CacheEntry(key,newValue),CacheMessageType.ADDED);
				messages.add(message);
			}else{
				keys.remove(key);
				//注： 缓存对象需要重载equals方法
				if(oldValue instanceof DataObject && newValue instanceof DataObject){
					XMLSerializer serializer = new XMLSerializer();
					String oldStr = serializer.marshallToString(oldValue);
					String newStr = serializer.marshallToString(newValue);
					if(oldStr.equals(newStr)){
						continue;
					}
				}else if(oldValue.equals(newValue)){
					continue;
				}
				// Update
				ChangeMessage message = new ChangeMessage(cache.getName(),new CacheEntry(key,newValue),CacheMessageType.UPDATED);
				messages.add(message);
			}
		}
		// Delete
		Iterator delKeys = keys.iterator();
		while(delKeys.hasNext()) {
			Object key = delKeys.next();
			Object value = cache.get(key);
			ChangeMessage message = new ChangeMessage(cache.getName(),new CacheEntry(key,value),CacheMessageType.REMOVED);
			messages.add(message);
		}
		return messages;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
}
