package com.hualife.foundation.component.cache.task;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.CacheWriter;
import com.hualife.foundation.component.cache.Caching;
import com.hualife.foundation.component.cache.RemoteCache;
import com.hualife.foundation.component.cache.monitor.Monitor;
import com.hualife.foundation.component.cache.monitor.OperationType;
import com.hualife.foundation.component.cache.task.ChangeMessage.CacheMessageType;
//import com.primeton.btp.api.core.logger.ILogger;
//import com.primeton.btp.api.core.logger.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheWriterTask extends AbstractSynchrodataTask {
	private int period;
	private CacheWriter cacheWriter;
	private Cache cache;
	private boolean flag = true;
	private int bufferSize;
	private static LinkedBlockingQueue<ChangeMessage> queue = new LinkedBlockingQueue<ChangeMessage>();
	//private ILogger logger = LoggerFactory.getLogger(CacheLoaderTask.class);
	private Logger logger = LoggerFactory.getLogger(CacheLoaderTask.class);
	
	public CacheWriterTask(Cache cache, CacheWriter writer,int period, int bufferSize) {
		super(period);
		this.cache = cache;
		this.cacheWriter = writer;
		this.bufferSize = bufferSize;
		this.period = period;
	}
	
	@Override
	public void doSth() {
		if(logger.isDebugEnabled()){
			logger.debug("["+cache.getName()+"] start cache writer task.");
		}
		int added = 0;
		int removed = 0;
		int updated = 0;
		boolean isException =false;
		Iterator<ChangeMessage> messages = this.queue.iterator();
		if(queue.size()>0){
			Monitor.recordCacheStatistics(cache.getName(), OperationType.WRITE, null);
		}
	
		while(messages.hasNext()) {
			ChangeMessage message = messages.next();
			String cacheName = message.getCacheName();
			Cache.Entry entry = message.getEntry();
			CacheMessageType type = message.getType();
			try {
				if(CacheMessageType.ADDED == type) {
					this.cacheWriter.write(entry);
					added++;
				}else if(CacheMessageType.REMOVED == type) {
					this.cacheWriter.delete(entry.getKey());
					removed++;
				}else if(CacheMessageType.UPDATED == type) {
					this.cacheWriter.update(entry);
					updated++;
				}
				this.queue.remove(message);
				
				if(logger.isDebugEnabled()){
					logger.debug("["+cacheName+"] " + message.getType().name().toLowerCase() +" key [" +entry.getKey()+"].");
				}
			} catch (Throwable e) {
				isException = true;
				if(logger.isErrorEnabled()) {
					logger.error("["+cacheName+"] write (" + message.getType().name().toLowerCase() +") key [" +entry.getKey()+"] error.",e);
				}
			}
		}
	       if(isException){
	    	   Monitor.recordCacheStatistics(cache.getName(), OperationType.WRITE_EXCEPTION, null);
	       }
		if(logger.isInfoEnabled()){
			logger.info("["+cache.getName()+"] end cache writer task. added: ["+added+"] ,removed: ["+removed+"] ,updated: ["+updated+"].");
		}
	}
		
	public static void addChangeMessage(ChangeMessage message) {
		if(null == message)
			throw new CacheException("Change message can not be null.");
		String cacheName = message.getCacheName();
		Cache cache = Caching.getCache(cacheName);
		if(cache instanceof RemoteCache){
			RemoteCache remoteCache = (RemoteCache)cache;
			remoteCache.addChangedMessage(message);;
			return;
		}
		queue.add(message);
	}
	
	@Override
	public boolean ready() {
		acquireChangeMessageWhenRemoteCache();
		if(bufferSize <= 0 && period > 0) {
			return super.ready();
		}
		if(period <=0 && bufferSize > 0) {
			return queue.size() > bufferSize;
		}
		boolean rtn = super.ready() || queue.size() > bufferSize ;
		if(rtn){
			setNextTime(System.currentTimeMillis()+period*1000);
		}
		return rtn;
	}

	private void acquireChangeMessageWhenRemoteCache() {
		if(cache instanceof RemoteCache){
			RemoteCache remoteCache = (RemoteCache)cache;
			List<ChangeMessage> messages = remoteCache.getChangedMessages();
			if(messages.size()>0){
				queue.addAll(messages);
			}
		}
	}

}
