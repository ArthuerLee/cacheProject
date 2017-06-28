package com.hualife.foundation.component.cache.redis.single;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.config.CacheConfiguration.ExpiryType;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.redis.AbstractRedisCache;
import com.hualife.foundation.component.cache.redis.config.CacheHostManager;
import com.hualife.foundation.component.cache.redis.serialize.DataSerializeSupport;
import com.hualife.foundation.component.cache.task.ChangeMessage;

public class RedisSingleCache<K, V> extends AbstractRedisCache<K, V> {
	@Override
	public void doStart() {
		// 初始化连接池配置信息
		CacheHostManager.initJedisConfig();
		if(com.hualife.foundation.component.cache.config.ConfigurationFactory.SYSTEM_CACHE_NAME.equals(getName())){
			return;
		}
		if(ConfigurationFactory.getCacheConfiguration(getName()).isRestartWithClearData()){
			removeAll();
		}
	}

	private void setExpiry(Jedis jedis, String serializeKey) {
		int time = 0;//默认不失效
		if(null != getExpiryTime()) {
			time = (int) getExpiryTime().getTimeToLive();
			if(time > 0)
				jedis.expire(serializeKey, time);
		}
	}
	
	private void setExpiry(Transaction transaction, String serializeKey) {
		int time = 0;//默认不失效
		if(null != getExpiryTime()) {
			time = (int) getExpiryTime().getTimeToLive();
			if(time > 0)
				transaction.expire(serializeKey, time);
		}
	}

	@Override
	public void doStop() {
//		if(ConfigurationFactory.SYSTEM_CACHE_NAME.equals(getName())){
//			Caching.removeSystemCache(getCacheClusterName());
//		}
//		CacheHostManager.shutdown(getCacheClusterName());
	}

	@Override
	public Set<K> keySet() {
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
			Set<String> keys = jedis.keys(getName()+"__app__*");
			Set<K> keyResult = new HashSet<K>();
			for(String key : keys){
				K k = getTrueKey(key);
				keyResult.add((K)k);
			}
			return keyResult;
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public boolean atomicPutIfAbsent(K key, V value) throws CacheException {
		if(null == key || null == value){
			return false;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
//			String serializeKey = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(key);
			String serializeKey = buildCacheKey(key);
			String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
//			long ret = jedis.hsetnx(getName(), serializeKey, serializeValue);
			long ret = jedis.setnx(serializeKey, serializeValue);
			boolean flag = ret == 1;
			if(flag){// 不管超时策略是什么都需要设置超时
				setExpiry(jedis, serializeKey);
			}
			return  flag;
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public void putValue(K key, V value) {
		if(key == null || value == null){
			return ;
		}
		if(StringUtil.isNullOrBlank(String.valueOf(key))|| StringUtil.isNullOrBlank(String.valueOf(value)))
			return;
		// 考虑到连接池，所以没有把Jedis作为RedisCache的属性
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String strKey = null;
		try{
//			strKey = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(key);
			strKey = buildCacheKey(key);
			String strValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
//			boolean flag = false;
//			flag = lock(jedis, strKey);
//			if(!flag){
//				return ;
//			}
			jedis.set(strKey, strValue);
			setExpiry(jedis, strKey);// 不管超时策略是什么都需要设置超时
		}finally{
//			unlock(jedis, strKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public V getValue(K key) {
		if(key == null){
			return null;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
//			String serializeKey = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(key);
//			String value = jedis.hget(getName(), serializeKey);
			String serializeKey = buildCacheKey(key);
			String value = jedis.get(serializeKey);
			if(StringUtil.isNotNullAndBlank(value) && getExpiryType() == ExpiryType.ACCESSED) {// 当超时策略是ACCESSED的时候需要设置超时
				setExpiry(jedis, serializeKey);
			}
			return (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(value);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public V atomicGetAndRemoveValue(K key) {
		if(key == null){
			return null;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		Transaction transaction = null;
		boolean executeTransaction = false;
		try{
			serializeKey = buildCacheKey(key);
			jedis.watch(serializeKey);
			String strData = jedis.get(serializeKey);
			if(StringUtil.isNullOrBlank(strData)){
				jedis.resetState();
				return null;
			}
			transaction = jedis.multi();
			V objValue = (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(strData);
			transaction.del(serializeKey);
			List<Object> objects = transaction.exec();
			// 没有数据和事务失败，objects都为null，无法判断是哪种原因引起的null值
			if(null == objects || ((Long)objects.get(0)) == 0){
				executeTransaction = true;
				throw new CacheException("the key was modified , please try again.");
			}
			return objValue;
		}catch(Throwable e){
			if(!executeTransaction && null != transaction){
				transaction.discard();
			}
			throw new CacheException("execute getAndRemove(K) error." ,e);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		/*
		if(key == null){
			return null;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		try{
			serializeKey = buildPutToCacheKey(key);
			boolean flag = false;
			flag = lock(jedis, serializeKey);
			if(!flag){
				return null;
			}
			String strData = jedis.get(serializeKey);
			V objValue = (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(strData);
			jedis.del(serializeKey);
			return objValue;
		}finally{
			unlock(jedis, serializeKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		*/
	}

	@Override
	public V atomicGetAndReplaceValue(K key, V value) {
		if(key == null){
			return null;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		Transaction transaction = null;
		boolean executeTransaction = false;
		try{
			serializeKey = buildCacheKey(key);
			jedis.watch(serializeKey);
			String strData = jedis.get(serializeKey);
			if(StringUtil.isNullOrBlank(strData)){
				jedis.resetState();
				return null;
			}
			transaction = jedis.multi();
			String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
			transaction.set(serializeKey, serializeValue);
			List<Object> objects = transaction.exec();
			if(null == objects || !String.valueOf(objects.get(0)).equalsIgnoreCase("OK")){
				executeTransaction = true;
				throw new CacheException("the key was modified , please try again.");
			}
			V objValue = (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(strData);
			setExpiry(jedis, serializeKey);// 不管超时策略是什么都需要设置超时
			return objValue;
		}catch(Throwable e){
			if(!executeTransaction && null != transaction){
				transaction.discard();
			}
			throw new CacheException("execute getAndReplace(K, V) error." ,e);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		/*
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		try{
			boolean flag = false;
			serializeKey = buildPutToCacheKey(key);
//			serializeKey = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(key);
			flag = lock(jedis, serializeKey);
			if(!flag){
				return null;
			}
			String oldCacheValue = jedis.get(serializeKey);
			if (StringUtil.isNullOrBlank(oldCacheValue)) {
				return null;
			}
			String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
			jedis.set(serializeKey, serializeValue);
			setExpiry(jedis, serializeKey);// 不管超时策略是什么都需要设置超时
			return (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(oldCacheValue);
		}finally{
			unlock(jedis, serializeKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		*/
	}
	

	@Override
	public boolean atomicCheckAndReplaceValue(K key, V oldValue, V newValue) throws CacheException {
		if(key == null){
			return false;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		Transaction transaction = null;
		boolean executeTransaction = false;
		try{
			serializeKey = buildCacheKey(key);
			jedis.watch(serializeKey);
			String oldCacheValue = jedis.get(serializeKey);
			if(StringUtil.isNullOrBlank(oldCacheValue)){
				jedis.resetState();
				return false;
			}
			transaction = jedis.multi();
			String serializeOldValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(oldValue);
			String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(newValue);
			if(!oldCacheValue.equals(serializeOldValue)){
				jedis.resetState();
				return false;
			}
			transaction.set(serializeKey, serializeValue);
			List<Object> objects = transaction.exec();
			if(null == objects || !String.valueOf(objects.get(0)).equalsIgnoreCase("OK")){
				executeTransaction = true;
				throw new CacheException("the key was modified , please try again.");
			}
			boolean flag = String.valueOf(objects.get(0)).equalsIgnoreCase("OK");
			if(flag){
				setExpiry(jedis, serializeKey);// 不管超时策略是什么都需要设置超时
			}
			return flag;
		}catch(Throwable e){
			if(!executeTransaction && null != transaction){
				transaction.discard();
			}
			throw new CacheException("execute replace(K, V, V) error." ,e);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		/*
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey =null;
		try{
			boolean flag = false;
			serializeKey = buildPutToCacheKey(key);
			flag = lock(jedis, serializeKey);
			if(!flag)
				return flag;
			String oldCacheValue = jedis.get(serializeKey);
			if(StringUtil.isNotNullAndBlank(oldCacheValue)){
				String serializeOldValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(oldValue);
				String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(newValue);
				if(!oldCacheValue.equals(serializeOldValue)){
					return false;
				}
				jedis.set(serializeKey, serializeValue);
				setExpiry(jedis, serializeKey);// 不管超时策略是什么都需要设置超时
				return true;
			}else{
				return false;
			}
		}finally{
			unlock(jedis, serializeKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		*/
	}
	
	@Override
	public boolean containsKey(K key) throws CacheException {
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
			String serializeKey = buildCacheKey(key);
			boolean rtn = jedis.exists(serializeKey);
			if(rtn && getExpiryType() == ExpiryType.ACCESSED){
				setExpiry(jedis, serializeKey);// 当超时策略是ACCESSED的时候需要设置超时
			}
			return rtn;
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public void removeAll() throws CacheException {
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
			Set<String> keys = jedis.keys(getName()+"__app__*");	
			long lenth = keys.size();
			Pipeline pipeline = jedis.pipelined();
			int count = 1;
			for(String key : keys){
				if(lenth > 10000) {
					pipeline.del(key);
					keys.remove(key);
					count++;
					if(count == 10000){
						pipeline.sync();
						lenth = keys.size();
						count = 1;
					}
				}else{
					pipeline.del(key);
					keys.remove(key);
				}
			}
			if(lenth <= 10000){
				pipeline.sync();
			}
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public boolean removeKey(K key) {
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		try{
			serializeKey = buildCacheKey(key);
//			boolean flag = false;
//			flag = lock(jedis, serializeKey);
//			if(!flag)
//				throw new CacheException("cache item busy, retry later");
			return jedis.del(serializeKey) == 1;
		}finally{
//			unlock(jedis, serializeKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public boolean atomicCheckAndRemoveValue(K key, V oldValue) {
		if(key == null){
			return false;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		Transaction transaction = null;
		boolean executeTransaction = false;
		try{
			serializeKey = buildCacheKey(key);
			jedis.watch(serializeKey);
			String oldCacheValue = jedis.get(serializeKey);
			if(StringUtil.isNullOrBlank(oldCacheValue)){
				jedis.resetState();
				return false;
			}
			transaction = jedis.multi();
			String serializeOldValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(oldValue);
			if(!oldCacheValue.equals(serializeOldValue)){
				jedis.resetState();
				return false;
			}
			transaction.del(serializeKey);
			List<Object> objects = transaction.exec();
			if(null == objects || ((Long)objects.get(0)) == 0){
				executeTransaction = true;
				throw new CacheException("the key was modified , please try again.");
			}
			boolean flag = (Long)objects.get(0) == 1;
			return flag;
		}catch(Throwable e){
			if(!executeTransaction && null != transaction){
				transaction.discard();
			}
			throw new CacheException("execute remove(K, V) error." ,e);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		/*
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		try{
			boolean flag = false;
			serializeKey = buildPutToCacheKey(key);
			flag = lock(jedis, serializeKey);
			if(!flag)
				return flag;
			String serializeOldValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(oldValue);
			String oldCacheValue = jedis.get(serializeKey);
			if(StringUtil.isNotNullAndBlank(oldCacheValue) && oldCacheValue.equals(serializeOldValue)){
				return jedis.del(serializeKey) == 1;
			}else{
				return false;
			}
		}finally{
			unlock(jedis, serializeKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		*/
	}

	private String getCacheHostName() {
		RedisSingleCacheProvider provider = (RedisSingleCacheProvider) getCacheProvider();
		return provider.getCacheHost();
	}

	@Override
	public V atomicGetAndPutValue(K key, V value) {
		if(key == null){
			return null;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		Transaction transaction = null;
		boolean executeTransaction = false;
		try{
			serializeKey = buildCacheKey(key);
			jedis.watch(serializeKey);
			String oldCacheValue = jedis.get(serializeKey);
			transaction = jedis.multi();
			String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
			transaction.set(serializeKey, serializeValue);
			List<Object> objects = transaction.exec();
			if(null == objects || !String.valueOf(objects.get(0)).equalsIgnoreCase("OK")){
				executeTransaction = true;
				throw new CacheException("the key was modified , please try again.");
			}
			setExpiry(jedis, serializeKey);// 不管超时策略是什么都需要设置超时
			if(StringUtil.isNullOrBlank(oldCacheValue)){
				return null;
			}
			V deserialize = (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(oldCacheValue);
			return deserialize;
		}catch(Throwable e){
			if(!executeTransaction && null != transaction){
				transaction.discard();
			}
			throw new CacheException("execute getAndPut(K, V) error." ,e);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		/*
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey =null;
		try{
			boolean flag = false;
			serializeKey = buildPutToCacheKey(key);
			flag = lock(jedis, serializeKey);
			if(!flag)
				throw new CacheException("cache item busy, retry later");
			String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
			String oldCacheValue = jedis.get(serializeKey);
			jedis.set(serializeKey, serializeValue);
			setExpiry(jedis, serializeKey);// 不管超时策略是什么都需要设置超时
			if(StringUtil.isNullOrBlank(oldCacheValue)){
				return null;
			}
			return (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(oldCacheValue);
		}finally{
			unlock(jedis, serializeKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		*/
	}

	@Override
	public boolean atomicReplaceIfExist(K key, V value) {
		if(key == null){
			return false;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey = null;
		Transaction transaction = null;
		boolean executeTransaction = false;
		try{
			serializeKey = buildCacheKey(key);
			jedis.watch(serializeKey);
			transaction = jedis.multi();
			transaction.exists(serializeKey);
			List<Object> objects = transaction.exec();
			if(null != objects && objects.get(0) instanceof Boolean){
				if(((Boolean)objects.get(0))){
					String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
					jedis.set(serializeKey, serializeValue);
					setExpiry(jedis, serializeKey);// 不管超时策略是什么都需要设置超时
				}
			}
			if(null == objects){
				executeTransaction = true;
				throw new CacheException("the key was modified , please try again.");
			}
			return (Boolean)objects.get(0);
		}catch(Throwable e){
			if(!executeTransaction && null != transaction){
				transaction.discard();
			}
			throw new CacheException("execute replace(K, V) error." ,e);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		/*
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String serializeKey =  null;
		try{
			serializeKey = buildPutToCacheKey(key);
			boolean flag = false;
			flag = lock(jedis, serializeKey);
			if(!flag)
				throw new CacheException("cache item busy, retry later");
			boolean rtn = jedis.exists(serializeKey);
			if(rtn){
				String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
				jedis.set(serializeKey, serializeValue);
				setExpiry(jedis, serializeKey);// 不管超时策略是什么都需要设置超时
			}
			return rtn;
		}finally{
			unlock(jedis, serializeKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
		*/
	}

	@Override
	public void putAllValue(Map<K, V> map) {
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		Iterator<K> keys = map.keySet().iterator();
		Transaction transaction = jedis.multi();
		try{
			while(keys.hasNext()) {
				K key = keys.next();
				V value = map.get(key);
				if(StringUtil.isNullOrBlank(String.valueOf(key))|| StringUtil.isNullOrBlank(String.valueOf(value)))
					continue;
				String serializeKey = buildCacheKey(key);
				String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
				transaction.set(serializeKey, serializeValue);
				setExpiry(transaction, serializeKey);
			}
			transaction.exec();
		}catch(Throwable e){
			transaction.discard();
			throw new CacheException("execute putAll error.",e);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public void removeAllValue(Collection<? extends K> keys) {
		if(null == keys || keys.isEmpty()) {
			return;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		Transaction transaction = jedis.multi();
		Iterator<? extends K> ks = keys.iterator();
		try{
			while(ks.hasNext()) {
				K key = ks.next();
				String serializeKey = buildCacheKey(key);
				transaction.del(serializeKey);
			}
			transaction.exec();
		}catch(Throwable e){
			transaction.discard();
			throw new CacheException("execute removeAll error.",e);
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}
	
	@Override
	public Map<K, V> getAllValue(Collection<? extends K> keys) {
		if(null == keys || keys.isEmpty()) {
			return null;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
			Iterator<? extends K> k = keys.iterator();
			Map<K, V> data = new HashMap<K, V>();
			Pipeline pipeline = jedis.pipelined();
			while(k.hasNext()) {
				K key = k.next();
				String serializeKey = buildCacheKey(key);
				pipeline.get(serializeKey);
			}
			List<Object> rtn = pipeline.syncAndReturnAll();
			if(keys.size() != rtn.size()) {
				throw new CacheException("Query results not equal with keys.");
			}
			
			int i=0;
			k = keys.iterator();
			while(k.hasNext()) {
				K key = k.next();
				Object obj = rtn.get(i);
				if(null == obj){
					data.put(key, null);
					continue;
				}
				V value = (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(String.valueOf(obj));
				data.put(key, value);
				if(getExpiryType() == ExpiryType.ACCESSED){
					setExpiry(jedis, buildCacheKey(key));
				}
				i++;
			}
			return data;
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}
	
	public boolean lock(Jedis jedis, String key){
		String lockKey = "__4lock__" + key;
		long ret = jedis.setnx(lockKey, "1");
		jedis.expire(lockKey, 1);
		return  ret == 1;
	}
	
	public boolean unlock(Jedis jedis, String key){
		String lockKey = "__4lock__" + key;
		long ret = jedis.del(lockKey);
		return  ret == 1;		
	}
	
	@Override
	public void addChangedMessage(ChangeMessage<K, V> message) {
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try {
			String serializeMessage = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(message);
			jedis.rpush(CHANGE_MESSAGE, serializeMessage);//添加到队列尾
		} finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public List<ChangeMessage<K, V>> getChangedMessages() {
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
			long len = jedis.llen(CHANGE_MESSAGE);
			List<ChangeMessage<K, V>> changeMessages = new ArrayList<ChangeMessage<K, V>>();
			if(len > 0){
				Pipeline pipeline = jedis.pipelined();
				if(len>1000){
					len = 1000;
				}
				for(int i=0;i < len;i++){
					pipeline.lpop(CHANGE_MESSAGE);
				}
				List<Object> reps = pipeline.syncAndReturnAll();
				for (Object message : reps) {
					ChangeMessage value = (ChangeMessage) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(String.valueOf(message));
					changeMessages.add(value);
				}
			}
			return changeMessages;
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public boolean expiry(K key, int seconds) throws CacheException {
		if(null == key) {
			return false;
		}
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
			String buildPutToCacheKey = buildCacheKey(key);
			return jedis.expire(buildPutToCacheKey, seconds) == 1;
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}

	@Override
	public Set<V> keySet(String pattern) {
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		try{
			String truePattern = buildCacheKey((K)pattern);
			Set<String> keys = jedis.keys(truePattern);
			Iterator<String> k = keys.iterator();
			Pipeline pipeline = jedis.pipelined();
			while(k.hasNext()) {
				String key = k.next();
				pipeline.get(key);
			}
			List<Object> rtn = pipeline.syncAndReturnAll();
			Set<V> data = new HashSet<V>();
			
			int i=0;
			k = keys.iterator();
			while(k.hasNext()) {
				String key = k.next();
				Object obj = rtn.get(i);
				if(null == obj){
					data.add(null);
					continue;
				}
				V value = (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(String.valueOf(obj));
				data.add(value);
				if(getExpiryType() == ExpiryType.ACCESSED){
					setExpiry(jedis, key);
				}
				i++;
			}
			return data;
		}finally{
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}
	
	@Override
	public String incr(K key) {
		if(key == null){
			throw new CacheException("the incr key is not null");
		}
		// 考虑到连接池，所以没有把Jedis作为RedisCache的属性
		Jedis jedis = CacheHostManager.getJedis(getCacheHostName());
		String strKey = null;
		String value = null;
		try{
//			strKey = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(key);
			strKey = buildCacheKey(key);
//			boolean flag = false;
//			flag = lock(jedis, strKey);
//			if(!flag){
//				return ;
//			}
			value = jedis.incr(strKey).toString();
			setExpiry(jedis, strKey);// 不管超时策略是什么都需要设置超时
			return value;
		}finally{
//			unlock(jedis, strKey);
			CacheHostManager.getJedisPool(getCacheHostName()).returnResourceObject(jedis);
		}
	}	
	
}
