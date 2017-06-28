package com.hualife.foundation.component.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.eos.system.utility.ClassUtil;
import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.config.CacheConfiguration;
import com.hualife.foundation.component.cache.config.CacheConfiguration.CacheLoadType;
import com.hualife.foundation.component.cache.config.CacheConfiguration.CacheShareOperation;
import com.hualife.foundation.component.cache.config.CacheConfiguration.CacheShareScope;
import com.hualife.foundation.component.cache.config.CacheConfiguration.CacheWriteType;
import com.hualife.foundation.component.cache.config.CacheConfiguration.Duration;
import com.hualife.foundation.component.cache.config.CacheConfiguration.ExpiryType;
import com.hualife.foundation.component.cache.config.model.Asyn;
import com.hualife.foundation.component.cache.config.model.CacheListener;
import com.hualife.foundation.component.cache.config.model.SerializeType;
import com.hualife.foundation.component.cache.event.CacheEntryCreatedListener;
import com.hualife.foundation.component.cache.event.CacheEntryExpiredListener;
import com.hualife.foundation.component.cache.event.CacheEntryReadListener;
import com.hualife.foundation.component.cache.event.CacheEntryRemovedListener;
import com.hualife.foundation.component.cache.event.CacheEntryUpdatedListener;
import com.hualife.foundation.component.cache.provider.CachingProvider;
import com.hualife.foundation.component.cache.task.CacheLoaderTask;
import com.hualife.foundation.component.cache.task.CacheWriterTask;
import com.hualife.foundation.component.cache.task.ChangeMessage;
import com.hualife.foundation.component.cache.task.ChangeMessage.CacheMessageType;
import com.hualife.foundation.component.cache.task.CheckMasterStatusTask;
import com.hualife.foundation.component.cache.task.CheckMasterTask;
import com.hualife.foundation.component.cache.task.SynchrodataControler;
import com.hualife.foundation.component.cache.task.SynchrodataTask;
//import com.primeton.btp.api.core.logger.ILogger;
//import com.primeton.btp.api.core.logger.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCache<K, V> implements Cache<K, V> {
	private String name;
	private CacheLoader<K, V> cacheLoader;
	private CacheWriter<K, V> cacheWriter;
	// 不同Listener的接口不同，不适合放到Map里
//	private Map<CacheEntryListenerType, CacheEntryListener<K, V>> listeners = new ConcurrentHashMap<CacheConfiguration.CacheEntryListenerType, CacheEntryListener<K,V>>();
	private CacheEntryCreatedListener<K, V> createdListener;
	private CacheEntryUpdatedListener<K, V> updatedListener;
	private CacheEntryRemovedListener<K, V> removedListener;
	private CacheEntryReadListener<K, V> readListener;
	private CacheEntryExpiredListener<K, V> expiryListener;
	private ExpiryType expiryType = ExpiryType.MODIFIED;
	private Duration expiryTime = Duration.ETERNAL;
	private boolean transactionEnabled = false;
	private boolean statisticsEnabled = false;
	protected SerializeType serializeType = SerializeType.XML;
	private CacheShareScope cacheShareScope = CacheShareScope.PUBLIC;
	private Set<CacheShareOperation> cacheShareOperations;
	private Status status = Status.UNINITIALISED;
	private CacheConfiguration configuration;
	private CachingProvider provider;
	
	private CacheLoadType cacheLoadType;
	private boolean needLoad = false;
	private CacheWriteType cacheWriteType;
	private boolean needWrite = false;
	private SynchrodataControler loaderControler;
	private SynchrodataControler writerControler;
	private SynchrodataControler masterSelector;
	
	/*private static ILogger logger = LoggerFactory.getLogger(AbstractCache.class);*/
	private static Logger logger = LoggerFactory.getLogger(AbstractCache.class);

	@Override
	public void start() throws CacheException {
		setStatus(Status.UNINITIALISED);
		init();
		
		doStart();
		
		doPreLoad();
		
		if(needLoad){
			if(null != cacheLoadType){
				if(cacheLoadType == CacheLoadType.ASYN){
					executeCacheLoaderTask();
				}
			}
		}
		
		if(needWrite){
			if(null != cacheWriteType && cacheWriteType == CacheWriteType.ASYN) {
				executeCacheWriteTask();
			}
		}
		
		setStatus(Status.STARTED);
	}

	private void doPreLoad() {
		if(null == cacheLoader){
			return;
		}
		Map<K,V> data = new HashMap<K, V>();
		if(this instanceof RemoteCache){
			RemoteCache<K, V> remoteCache = (RemoteCache<K, V>) this;
			new CheckMasterTask<K, V>(remoteCache, 1).doSth();
			if(remoteCache.isMaster()){
//				if(remoteCache.checkSynPrevilige()){
				data = preLoad();
				if(null != data){
					putAllValue(data);
				}
			}
		}else{
			data = preLoad();
			if(null != data){
				putAllValue(data);
			}
		}
		if(logger.isDebugEnabled()) {
			int size = 0;
			if(null != data){
				size = data.size();
			}
			logger.debug("["+getName()+"] preload ["+size+"] row.");
		}
	}

	private void executeCacheWriteTask() {
		if(null == cacheWriter) {
			return;
		}
		Integer bufferSize = cacheWriter.getAsyn().getBufferSize();
		Integer period = cacheWriter.getAsyn().getPeriod();
		boolean isBuffer = isMoreThanZero(bufferSize);
		boolean isPeriod = isMoreThanZero(period);
		if(!isPeriod && !isBuffer) {
			if(logger.isDebugEnabled()) {
				logger.debug("cache["+getName()+"] writer task not execute.");
			}
			return;
		}
		
		SynchrodataTask task = new CacheWriterTask(this, cacheWriter, period, bufferSize);
		if(this instanceof RemoteCache) {
//				task = new CheckPreviligeTask<K, V>((RemoteCache<K, V>)this, task);
			masterSelector = new SynchrodataControler(new CheckMasterTask<K, V>((RemoteCache<K, V>)this, period));
			masterSelector.start();
			task = new CheckMasterStatusTask<K, V>((RemoteCache<K, V>)this, task);
		}
		writerControler = new SynchrodataControler(task);
		writerControler.start();
		
		if(logger.isDebugEnabled()) {
			logger.debug("Execute cache["+getName()+"] writer task success.");
		}
	}

	private boolean isMoreThanZero(Integer i) {
		boolean flag = false;
		if(null != i && i > 0) {
			flag = true;
		}
		return flag;
	}


	private void executeCacheLoaderTask() {
		// 暂定每个缓存实例，启动一个线程去做定时刷新
		int period = 0;
		if(null == cacheLoader){
			return;
		}
		Integer per = cacheLoader.getAsyn().getPeriod();
		boolean isPeriod = isMoreThanZero(per);
		if(!isPeriod) {
			if(logger.isDebugEnabled()) {
				logger.debug("cache["+getName()+"] loader task not execute.");
			}
			return;
		}
		period = per;
		
		SynchrodataTask task = new CacheLoaderTask(this, cacheLoader, period);
		if(this instanceof RemoteCache) {
//			task = new CheckPreviligeTask<K, V>((RemoteCache<K, V>)this, task);
			masterSelector = new SynchrodataControler(new CheckMasterTask<K, V>((RemoteCache<K, V>)this, period));
			masterSelector.start();
			task = new CheckMasterStatusTask<K, V>((RemoteCache<K, V>)this, task);
		}
		loaderControler = new SynchrodataControler(task);
		loaderControler.start();
		
		if(logger.isDebugEnabled()) {
			logger.debug("Execute cache["+getName()+"] loader task success.");
		}
	}


	private Map<K, V> preLoad() {
		if(null != cacheLoader){
			return cacheLoader.preLoad();
		}
		return new HashMap<K, V>();
	}
	

	@Override
	public void stop() throws CacheException {
		if(logger.isDebugEnabled()) {
			logger.debug("["+getName()+"] is stopping." );
		}
		setStatus(Status.STOPPED);
		//本地缓存，清理缓存数据，远程不需要清理数据
		Caching.getCacheManager(getName()).removeCache(getName());
		if(null != loaderControler)
			loaderControler.stop();
		if(null != writerControler)
			writerControler.stop();
		doStop();
		
		if(logger.isDebugEnabled()) {
			logger.debug("["+getName()+"] is stoped." );
		}
	}


	@Override
	public CacheStatistics getStatistics() {
		// TODO
		return null;
	}
	
	@Override
	public Set<V> keys(String pattern) {
		checkStatus();
		if (StringUtil.isNullOrBlank(pattern)) {
			return null;
		}
		return keySet(pattern);
	}

	public abstract Set<V> keySet(String pattern);

	@Override
	public V get(K key) throws CacheException {
		checkStatus();
		if (key == null) {
			return null;
		}
		if(null != readListener){
			readListener.beforeRead(key);
		}
		V value = getValue(key);
		if (null == value && 
				null != cacheLoadType && 
				cacheLoadType == CacheLoadType.READ_THROUGH) {
			if (getCacheLoader() != null) {
				if(getCacheLoader().canLoad(key)){
					value = getCacheLoader().load(key).getValue();
					if (value != null) {
						put(key, value);
					}
				}
			}
		}
//		if(null != expiryType && 
//				expiryType == ExpiryType.ACCESSED &&
//				expiryTime != Duration.ETERNAL) {
//			//  更新超时时间，具体实现控制
//		}
		if(null != readListener){
			readListener.afterRead(new CacheEntry<K, V>(key,value));
		}
		return value;
	}
	

	@Override
	public Map<K, V> getAll(Collection<? extends K> keys) throws CacheException {
		checkStatus();
		if(null == keys || keys.isEmpty()) {
			return null;
		}
		if(null != readListener) {
			readListener.beforeReadAll(keys);
		}
		Map<K, V> data = getAllValue(keys);
		if (null != cacheLoadType && cacheLoadType != CacheLoadType.NONE) {
			if(data.size() != keys.size()) {
				if(cacheLoadType == CacheLoadType.READ_THROUGH){
					data = getCacheLoader().loadAll(keys);
					putAllValue(data);
					return data;
				}else if(cacheLoadType == CacheLoadType.ASYN) {
					// 等待定时刷新线程更新缓存
				}
			}
		}
		
		if(null != readListener) {
			readListener.afterReadAll(keys);
		}
		return data;
	}

//	@Override
//	public boolean containsKey(K key) throws CacheException {
//		checkStatus();
//		return getValue(key) != null;
//	}


	@Override
	public void put(K key, V value) throws CacheException {
		checkStatus();
		if (key == null || value == null) {
			return;
		}
		// TODO 数据不一致问题
		if(null != createdListener)
			createdListener.beforeCreate(new CacheEntry<K, V>(key, value));
		putValue(key, value);
		if (null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().write(new CacheEntry<K, V>(key,value));
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, value), CacheMessageType.ADDED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		if(null != createdListener)
			createdListener.afterCreate(new CacheEntry<K, V>(key, value));
	}

	@Override
	public boolean putIfAbsent(K key, V value) throws CacheException {
		checkStatus();
		if (key == null) {
			// TODO throw nullpointexception
			return false;
		}
		// TODO 数据不一致问题
		if(null != createdListener)
			createdListener.beforeCreate(new CacheEntry<K, V>(key, value));
		boolean rtn = atomicPutIfAbsent(key, value);
		if (rtn && null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().write(new CacheEntry<K, V>(key,value));
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, value), CacheMessageType.ADDED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		if(null != createdListener)
			createdListener.afterCreate(new CacheEntry<K, V>(key, value));
		return rtn;
	}

	protected void checkStatus() {
		if(status == Status.STARTED) {
			return;
		}else if(status == Status.STOPPED) {
			throw new CacheException(getName()+": was stopped.");
		}else if(status == Status.UNINITIALISED) {
			throw new CacheException(getName()+": is initializing.");
		}
	}

	@Override
	public void putAll(Map<K, V> map) throws CacheException {
		checkStatus();
		if(null == map || map.isEmpty()) {
			return ;
		}
		if(null != createdListener) {
			createdListener.beforeCreateAll(map);
		}
		putAllValue(map);
		if (null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().writeAll(map);;
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				Iterator<K> i = map.keySet().iterator();
				while(i.hasNext()) {
					K key = i.next();
					V value = map.get(key);
					ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, value), CacheMessageType.ADDED);
					CacheWriterTask.addChangeMessage(message);
				}
			}
		}
		if(null != createdListener) {
			createdListener.afterCreateAll(map);
		}
	}

	@Override
	public boolean remove(K key) throws CacheException {
		checkStatus();
		if(null == key){
			return false;
		}
		if(null != removedListener)
			removedListener.beforeRemove(key);
		boolean rtn = removeKey(key);
		if (rtn && null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().delete(key);
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, null), CacheMessageType.REMOVED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		
		if(null != removedListener)
			removedListener.afterRemoved(new CacheEntry<K, V>(key,null));
		return rtn;
	}

	@Override
	public V getAndRemove(K key) throws CacheException {
		checkStatus();
		if(null == key){
			return null;
		}
		if(null != removedListener)
			removedListener.beforeRemove(key);
		V value = atomicGetAndRemoveValue(key);
		if (null != value && null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().delete(key);
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, null), CacheMessageType.REMOVED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		if(null != removedListener)
			removedListener.afterRemoved(new CacheEntry<K, V>(key,value));
		return value;
	}
	
	@Override
	public boolean remove(K key, V oldValue) throws CacheException {
		checkStatus();
		if(null == key){
			return false;
		}
		if(null != removedListener)
			removedListener.beforeRemove(key);
		boolean rtn = atomicCheckAndRemoveValue(key, oldValue);
		if (rtn && null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().delete(key);
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, null), CacheMessageType.REMOVED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		if(null != removedListener)
			removedListener.afterRemoved(new CacheEntry<K, V>(key,oldValue));
		return rtn;
	}

	@Override
	public boolean replace(K key, V value) throws CacheException {
		checkStatus();
		if(null == key || null == value){
			return false;
		}
		if(null != updatedListener) {
			updatedListener.beforeUpdate(new CacheEntry<K, V>(key, value));
		}
		
		boolean rtn = atomicReplaceIfExist(key, value);
		
		if (rtn && null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().update(new CacheEntry<K, V>(key,value));
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, value), CacheMessageType.UPDATED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		if(null != updatedListener) {
			updatedListener.afterUpdate(new CacheEntry<K, V>(key, value));
		}
		return rtn;
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) throws CacheException {
		checkStatus();
		if(null == key || null == newValue){
			return false;
		}
		if(null != updatedListener) {
			updatedListener.beforeUpdate(new CacheEntry<K, V>(key, newValue));
		}
		
		boolean rtn = atomicCheckAndReplaceValue(key, oldValue, newValue);
		
		if (rtn && null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().update(new CacheEntry<K, V>(key,newValue));
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, newValue), CacheMessageType.UPDATED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		
		if(null != updatedListener) {
			updatedListener.afterUpdate(new CacheEntry<K, V>(key, newValue));
		}
		return rtn;
	}
	
	@Override
	public V getAndPut(K key, V value) throws CacheException {
		checkStatus();
		if(null == key || null == value){
			return null;
		}
		if(null != updatedListener) {
			updatedListener.beforeUpdate(new CacheEntry<K, V>(key, value));
		}
		V val = atomicGetAndPutValue(key, value);
		if (null != val && null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().write(new CacheEntry<K, V>(key,value));
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, value), CacheMessageType.ADDED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		if(null != updatedListener) {
			updatedListener.afterUpdate(new CacheEntry<K, V>(key, value));
		}
		return val;
	}


	@Override
	public V getAndReplace(K key, V value) throws CacheException {
		checkStatus();
		if(null == key || null == value){
			return null;
		}
		if(null != updatedListener) {
			updatedListener.beforeUpdate(new CacheEntry<K, V>(key, value));
		}
		V val = atomicGetAndReplaceValue(key, value);
		if (null != val && null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().write(new CacheEntry<K, V>(key,value));
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, value), CacheMessageType.UPDATED);
				CacheWriterTask.addChangeMessage(message);
			}
		}
		if(null != updatedListener) {
			updatedListener.afterUpdate(new CacheEntry<K, V>(key, value));
		}
		return val;
	}

	@Override
	public void removeAll(Collection<? extends K> keys) throws CacheException {
		checkStatus();
		if(null == keys || keys.isEmpty()) {
			return ;
		}
		if(null != removedListener) {
			removedListener.beforeRemoveAll(keys);
		}
		removeAllValue(keys);
		if (null != cacheWriteType && cacheWriteType != CacheWriteType.NONE) {
			if(cacheWriteType == CacheWriteType.SYN){
				getCacheWriter().deleteAll(keys);
			}else if(cacheWriteType == CacheWriteType.ASYN) {
				Iterator<? extends K> i = keys.iterator();
				while(i.hasNext()) {
					K key = i.next();
					ChangeMessage<K, V> message = new ChangeMessage<K, V>(this.getName(), new CacheEntry<K, V>(key, null), CacheMessageType.REMOVED);
					CacheWriterTask.addChangeMessage(message);
				}
			}
		}
		if(null != removedListener) {
			removedListener.afterRemoveAll(keys);
		}
	
	}

//	@Override
//	public void removeAll() throws CacheException {
//		checkStatus();
//		Set<K> sets = keySet();
//		removeAll(sets);
//	}
	
	public abstract void doStart();
	
	public abstract void doStop();
	
	public abstract void putAllValue(Map<K, V> map);
	
	public abstract void removeAllValue(Collection<? extends K> keys);
	
	public abstract Map<K, V> getAllValue(Collection<? extends K> keys);
	
	public abstract void putValue(K key, V value);

	public abstract V getValue(K key);
	
	public abstract boolean removeKey(K key);
	public abstract V atomicGetAndRemoveValue(K key);
	public abstract boolean atomicPutIfAbsent(K key, V value);
	public abstract boolean atomicCheckAndRemoveValue(K key, V oldValue);
	public abstract boolean atomicCheckAndReplaceValue(K key, V oldValue, V newValue);
	public abstract V atomicGetAndReplaceValue(K key, V value);
	public abstract V atomicGetAndPutValue(K key, V value);
	public abstract boolean atomicReplaceIfExist(K key, V value);
	
	public static class CacheEntry<K, V> implements Entry<K, V> {
		private K key;
		private V value;
		public CacheEntry(){
			// 序列化/反序列化用
		}
		public CacheEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		public void setKey(K key) {
			this.key = key;
		}
		public void setValue(V value) {
			this.value = value;
		}
		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}
		
	}
	
	private void init() {
		initCacheLoader();
		initCacheLoadType();
		initCacheWriter();
		initCacheWriteType();
		initListener();
		initExpiry();
		if(null != configuration){
			serializeType = configuration.getSerializeType();
		}
//		initCacheShare();
	}


//	private void initCacheShare() {
//		if(null != configuration){
//			cacheShareScope = configuration.getCacheShareScope();
//			cacheShareOperations = configuration.getCacheShareOperations();
//			if(null == cacheShareScope){
//				cacheShareScope = CacheShareScope.PUBLIC;
//			}
//		}
//		if(cacheShareScope == CacheShareScope.PUBLIC){
//			if(null == cacheShareOperations || cacheShareOperations.isEmpty()) {
//				cacheShareOperations = new HashSet<CacheShareOperation>();
//				cacheShareOperations.add(CacheShareOperation.WRITE);
//				cacheShareOperations.add(CacheShareOperation.REMOVE);
//				cacheShareOperations.add(CacheShareOperation.UPDATE);
//				cacheShareOperations.add(CacheShareOperation.READ);
//			}
//		}
//	}


	private void initExpiry() {
		if(null != configuration){
			expiryType = configuration.getExpiryType();
			expiryTime = configuration.getExpiry();
		}
		if(null == expiryType){
			expiryType = ExpiryType.MODIFIED;
		}
		if(null == expiryTime)
			expiryTime = Duration.ETERNAL;
	}


	private void initListener() {
		if(null != configuration) {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if(null == createdListener) {
				CacheListener listener = configuration.getCacheEntryCreatedListener();
				if(null != listener){
					String cacheEntryCreatedListener = listener.getImpl();
					if(null != cacheEntryCreatedListener && !"".equals(cacheEntryCreatedListener)){
						try {
							createdListener = (CacheEntryCreatedListener<K, V>) ClassUtil.newInstance(cacheEntryCreatedListener, null);
							createdListener.setSynchronous(listener.isSyn());
						} catch (Throwable e) {
							throw new CacheException(e);
						}
					}
				}
			}
			if(null == removedListener) {
				CacheListener listener = configuration.getCacheEntryRemovedListener();
				if(null != listener) {
					String cacheEntryRemovedListener = listener.getImpl();
					if(null != cacheEntryRemovedListener && !"".equals(cacheEntryRemovedListener)){
						try {
							removedListener = (CacheEntryRemovedListener<K, V>) ClassUtil.newInstance(cacheEntryRemovedListener, null);
							removedListener.setSynchronous(listener.isSyn());
						} catch (Throwable e) {
							throw new CacheException(e);
						}
					}
				}
			}
			if(null == updatedListener) {
				CacheListener listener = configuration.getCacheEntryUpdatedListener();
				if(null != listener) {
					String cacheEntryUpdatedListener = listener.getImpl();
					if(null != cacheEntryUpdatedListener && !"".equals(cacheEntryUpdatedListener)){
						try {
							updatedListener = (CacheEntryUpdatedListener<K, V>) ClassUtil.newInstance(cacheEntryUpdatedListener, null);
							updatedListener.setSynchronous(listener.isSyn());
						} catch (Throwable e) {
							throw new CacheException(e);
						}
					}
				}
			}
			if(null == readListener) {
				CacheListener listener = configuration.getCacheEntryReadListener();
				if(null != listener) {
					String cacheEntryReadListener = listener.getImpl();
					if(null != cacheEntryReadListener && !"".equals(cacheEntryReadListener)){
						try {
							readListener = (CacheEntryReadListener<K, V>) ClassUtil.newInstance(cacheEntryReadListener, null);
							readListener.setSynchronous(listener.isSyn());
						} catch (Throwable e) {
							throw new CacheException(e);
						}
					}
				}
			}
			if(null == expiryListener) {
				CacheListener listener = configuration.getCacheEntryExpiredListener();
				if(null != listener) {
					String cacheEntryExpiredListener = listener.getImpl();
					if(null != cacheEntryExpiredListener && !"".equals(cacheEntryExpiredListener)){
						try {
							expiryListener = (CacheEntryExpiredListener<K, V>) ClassUtil.newInstance(cacheEntryExpiredListener, null);
							expiryListener.setSynchronous(listener.isSyn());
						} catch (Throwable e) {
							throw new CacheException(e);
						}
					}
				}
			}
		}
	}


	private void initCacheLoadType() {
		if(null != this.cacheLoader){
			this.cacheLoadType = this.cacheLoader.getCacheLoadType();
			if(null != cacheLoadType && cacheLoadType != CacheLoadType.NONE)
				this.needLoad = true;
		}
	}


	private void initCacheLoader() {
		if(null == this.cacheLoader){
			if(null != configuration) {
				com.hualife.foundation.component.cache.config.model.CacheLoader cacheLoaderModel = configuration.getCacheLoader();
				if(cacheLoaderModel != null){
					String impl = cacheLoaderModel.getImpl();
					if(null != impl && !"".equals(impl)){
						try {
							this.cacheLoader = (CacheLoader<K, V>) ClassUtil.newInstance(impl, null);
						} catch (Throwable e) {
							throw new CacheException(e);
						}
						Asyn asyn = cacheLoaderModel.getAsyn();
						if(null != asyn){
							this.cacheLoader.setCacheLoadType(CacheLoadType.ASYN);
							this.cacheLoader.setAsyn(asyn);
						}else{
							this.cacheLoader.setCacheLoadType(CacheLoadType.NOTIFY);
						}
						this.cacheLoader.setExtProperties(cacheLoaderModel.getExtProperties());
					}
				}
			}
		}
	}
	
	private void initCacheWriteType() {
		if(null != this.cacheWriter){
			this.cacheWriteType = this.cacheWriter.getCacheWriteType();
			if(null != cacheWriteType && cacheWriteType != CacheWriteType.NONE)
				this.needWrite = true;
		}
	}


	private void initCacheWriter() {
		if(null == this.cacheWriter){
			if(null != configuration) {
				com.hualife.foundation.component.cache.config.model.CacheWriter cacheWriterModel = configuration.getCacheWriter();
				if(null != cacheWriterModel) {
					String impl = cacheWriterModel.getImpl();
					if(null != impl && !"".equals(impl)){
						try {
							this.cacheWriter = (CacheWriter<K, V>) ClassUtil.newInstance(impl, null);
						} catch (Throwable e) {
							throw new CacheException(e);
						}
						Asyn asyn = cacheWriterModel.getAsyn();
						if(null != asyn) {
							this.cacheWriter.setCacheWriteType(CacheWriteType.ASYN);
							this.cacheWriter.setAsyn(asyn);
						}else{
							this.cacheWriter.setCacheWriteType(CacheWriteType.SYN);
						}
						this.cacheWriter.setExtProperties(cacheWriterModel.getExtProperties());
					}
				}
			}
		}
	}

	@Override
	public String incr(K key) throws CacheException {
		checkStatus();
		if(null == key){
			throw new CacheException("the incr key is not null");
		}
		return incr(key);
	}	
	
	public CacheLoader<K, V> getCacheLoader() {
		return cacheLoader;
	}

	public void setCacheLoader(CacheLoader<K, V> cacheLoader) {
		this.cacheLoader = cacheLoader;
	}

	public CacheWriter<K, V> getCacheWriter() {
		return cacheWriter;
	}

	public void setCacheWriter(CacheWriter<K, V> cacheWriter) {
		this.cacheWriter = cacheWriter;
	}

	public ExpiryType getExpiryType() {
		return expiryType;
	}

	public void setExpiryType(ExpiryType expiryType) {
		this.expiryType = expiryType;
	}

	public Duration getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(Duration expiryTime) {
		this.expiryTime = expiryTime;
	}

	public boolean isTransactionEnabled() {
		return transactionEnabled;
	}

	public void setTransactionEnabled(boolean transactionEnabled) {
		this.transactionEnabled = transactionEnabled;
	}

	public boolean isStatisticsEnabled() {
		return statisticsEnabled;
	}

	public void setStatisticsEnabled(boolean statisticsEnabled) {
		this.statisticsEnabled = statisticsEnabled;
	}

	public CacheShareScope getCacheShareScope() {
		return cacheShareScope;
	}

	public void setCacheShareScope(CacheShareScope cacheShareScope) {
		this.cacheShareScope = cacheShareScope;
	}

	public Set<CacheShareOperation> getCacheShareOperations() {
		return cacheShareOperations;
	}

	public void setCacheShareOperations(
			Set<CacheShareOperation> cacheShareOperations) {
		this.cacheShareOperations = cacheShareOperations;
	}
	
	public CacheEntryCreatedListener<K, V> getCreatedListener() {
		return createdListener;
	}


	public void setCreatedListener(CacheEntryCreatedListener<K, V> createdListener) {
		this.createdListener = createdListener;
	}


	public CacheEntryUpdatedListener<K, V> getUpdatedListener() {
		return updatedListener;
	}


	public void setUpdatedListener(CacheEntryUpdatedListener<K, V> updatedListener) {
		this.updatedListener = updatedListener;
	}


	public CacheEntryRemovedListener<K, V> getRemovedListener() {
		return removedListener;
	}


	public void setRemovedListener(CacheEntryRemovedListener<K, V> removedListener) {
		this.removedListener = removedListener;
	}


	public CacheEntryReadListener<K, V> getReadListener() {
		return readListener;
	}


	public void setReadListener(CacheEntryReadListener<K, V> readListener) {
		this.readListener = readListener;
	}


	public CacheEntryExpiredListener<K, V> getExpiryListener() {
		return expiryListener;
	}


	public void setExpiryListener(CacheEntryExpiredListener<K, V> expiryListener) {
		this.expiryListener = expiryListener;
	}


	public CacheLoadType getCacheLoadType() {
		return cacheLoadType;
	}


	public void setCacheLoadType(CacheLoadType cacheLoadType) {
		this.cacheLoadType = cacheLoadType;
	}


	public boolean isNeedLoad() {
		return needLoad;
	}


	public void setNeedLoad(boolean needLoad) {
		this.needLoad = needLoad;
	}


	public CacheWriteType getCacheWriteType() {
		return cacheWriteType;
	}


	public void setCacheWriteType(CacheWriteType cacheWriteType) {
		this.cacheWriteType = cacheWriteType;
	}


	public boolean isNeedWrite() {
		return needWrite;
	}


	public void setNeedWrite(boolean needWrite) {
		this.needWrite = needWrite;
	}


	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public CacheConfiguration getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(CacheConfiguration configuration) {
		this.configuration = configuration;
	}
	public CachingProvider getCacheProvider() {
		return provider;
	}

	public void setCacheProvider(CachingProvider provider) {
		this.provider = provider;
	}
}
