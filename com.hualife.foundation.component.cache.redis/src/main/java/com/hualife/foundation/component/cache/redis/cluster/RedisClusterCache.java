package com.hualife.foundation.component.cache.redis.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;

import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.config.CacheConfiguration.ExpiryType;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.redis.AbstractRedisCache;
import com.hualife.foundation.component.cache.redis.config.CacheHostManager;
import com.hualife.foundation.component.cache.redis.serialize.DataSerializeSupport;
import com.hualife.foundation.component.cache.task.ChangeMessage;

public class RedisClusterCache<K, V> extends AbstractRedisCache<K, V> {
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

	private void setExpiry(ShardedJedis jedis, String serialiazedKey) {
		int time = 0;//默认不失效
		if(null != getExpiryTime()) {
			time = (int) getExpiryTime().getTimeToLive();
			if(time > 0)
				jedis.expire(serialiazedKey, time);
		}
	}
	
	private void setExpiry(ShardedJedisPipeline jedis, String serialiazedKey) {
		int time = 0;//默认不失效
		if(null != getExpiryTime()) {
			time = (int) getExpiryTime().getTimeToLive();
			if(time > 0)
				jedis.expire(serialiazedKey, time);
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
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		Set<K> keyResult = new HashSet<K>();
		try{
			Collection<Jedis> allShards = jedis.getAllShards();
			for (Jedis jed : allShards) {
				Set<String> keys = jed.keys(getName()+"__app__*");
				for(String key : keys){
					K k = getTrueKey(key);
					keyResult.add(k);
				}
			}
			return keyResult;
		}finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}
	
	@Override
	public boolean atomicPutIfAbsent(K key, V value) throws CacheException {
		if(null == key || null == value){
			return false;
		}
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try {
			String serializeKey = buildCacheKey(key);
			String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
			long ret = jedis.setnx(serializeKey, serializeValue);
			boolean flag = ret == 1;
			if(flag){// 不管超时策略是什么都需要设置超时
				setExpiry(jedis, serializeKey);
			}
			return  flag;
		} finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
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
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String strKey = null;
		try{
			strKey = buildCacheKey(key);
			String strValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
			boolean flag = false;
			flag = lock(jedis, strKey);
			if(!flag){
				return ;
			}
			jedis.set(strKey, strValue);
			setExpiry(jedis, strKey);// 不管超时策略是什么都需要设置超时
		}finally{
			unlock(jedis, strKey);
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public V getValue(K key) {
		if(key == null){
			return null;
		}
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			String serializeKey = buildCacheKey(key);
			String value = jedis.get(serializeKey);
			if(StringUtil.isNotNullAndBlank(value) && getExpiryType() == ExpiryType.ACCESSED) {// 当超时策略是ACCESSED的时候需要设置超时
				setExpiry(jedis, serializeKey);
			}
			return (V) DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(value);
		}finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public V atomicGetAndRemoveValue(K key) {
		if(key == null){
			return null;
		}
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String serializeKey = null;
		try{
			serializeKey = buildCacheKey(key);
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
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public V atomicGetAndReplaceValue(K key, V value) {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String serializeKey = null;
		try{
			boolean flag = false;
			serializeKey = buildCacheKey(key);
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
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}
	

	@Override
	public boolean atomicCheckAndReplaceValue(K key, V oldValue, V newValue) throws CacheException {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String serializeKey = null;
		try{
			boolean flag = false;
			serializeKey = buildCacheKey(key);
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
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}
	
	@Override
	public boolean containsKey(K key) throws CacheException {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String serializeKey = null;
		try{
			serializeKey = buildCacheKey(key);
			Boolean exists = jedis.exists(serializeKey);
			if(exists && getExpiryType() == ExpiryType.ACCESSED){
				setExpiry(jedis, serializeKey);// 当超时策略是ACCESSED的时候需要设置超时
			}
			return exists;
		}finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public void removeAll() throws CacheException {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			Collection<Jedis> allShards = jedis.getAllShards();
			ShardedJedisPipeline pipeline = jedis.pipelined();
			for (Jedis jed : allShards) {
				Set<String> keys = jed.keys(getName()+"__app__*");
				for(String key : keys){
					pipeline.del(key);
				}
			}
			pipeline.sync();
		}finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public boolean removeKey(K key) {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String serializeKey = null;
		try{
			serializeKey = buildCacheKey(key);
			boolean flag = false;
			flag = lock(jedis, serializeKey);
			if(!flag)
				throw new CacheException("cache item busy, retry later");
			return jedis.del(serializeKey) == 1;
		}finally{
			unlock(jedis, serializeKey);
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public boolean atomicCheckAndRemoveValue(K key, V oldValue) {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String serializeKey = null;
		try{
			boolean flag = false;
			serializeKey = buildCacheKey(key);
			flag = lock(jedis, serializeKey);
			if(!flag)
				return flag;
			String oldCacheValue = jedis.get(serializeKey);
			if(StringUtil.isNotNullAndBlank(oldCacheValue)){
				String serializeOldValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(oldValue);
				if(!oldCacheValue.equals(serializeOldValue)) {
					return false;
				}
				return jedis.del(serializeKey) == 1;
			}else{
				return false;
			}
		}finally{
			unlock(jedis, serializeKey);
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	private String getCacheClusterName() {
		RedisClusterCacheProvider provider = (RedisClusterCacheProvider) getCacheProvider();
		return provider.getCluster();
	}

	@Override
	public V atomicGetAndPutValue(K key, V value) {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String serializeKey = null;
		try{
			boolean flag = false;
			serializeKey = buildCacheKey(key);
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
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public boolean atomicReplaceIfExist(K key, V value) {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		String serializeKey = null;
		try{
			serializeKey = buildCacheKey(key);
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
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public void putAllValue(Map<K, V> map) {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			Iterator<K> keys = map.keySet().iterator();
			ShardedJedisPipeline pipeline = jedis.pipelined();
			while(keys.hasNext()) {
				K key = keys.next();
				V value = map.get(key);
				if(StringUtil.isNullOrBlank(String.valueOf(key))|| StringUtil.isNullOrBlank(String.valueOf(value)))
					continue;
				String serializeKey = buildCacheKey(key);
				String serializeValue = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(value);
				pipeline.set(serializeKey, serializeValue);
				setExpiry(pipeline, serializeKey);
//			boolean flag = false;
//			flag = lock(jedis, serializeKey);
//			if(!flag)
//				throw new CacheException("cache item busy, retry later");
//			try{
//				jedis.hset(getName(), serializeKey, serializeValue);
//			}finally{
//				unlock(jedis, serializeKey);
//			}
			}
			pipeline.sync();
		}finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public void removeAllValue(Collection<? extends K> keys) {
		if(null == keys || keys.isEmpty()) {
			return;
		}
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			ShardedJedisPipeline pipeline = jedis.pipelined();
			Iterator<? extends K> ks = keys.iterator();
			while(ks.hasNext()) {
				K key = ks.next();
				String serializeKey = buildCacheKey(key);
				pipeline.del(serializeKey);
			}
			pipeline.sync();
		}finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}
	
	@Override
	public Map<K, V> getAllValue(Collection<? extends K> keys) {
		if(null == keys || keys.isEmpty()) {
			return null;
		}
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			ShardedJedisPipeline pipeline = jedis.pipelined();
			Iterator<? extends K> k = keys.iterator();
			Map<K, V> data = new HashMap<K, V>();
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
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	public boolean lock(ShardedJedis jedis, String key){
		String lockKey = "__4lock__" + key;
		long ret = jedis.setnx(lockKey, "1");
		jedis.expire(lockKey, 1);
		return  ret == 1;
	}
	
	public boolean unlock(ShardedJedis jedis, String key){
		String lockKey = "__4lock__" + key;
		long ret = jedis.del(lockKey);
		return  ret == 1;		
	}
	
	@Override
	public void addChangedMessage(ChangeMessage<K, V> message) {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			String serializeMessage = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(message);
			jedis.rpush(CHANGE_MESSAGE, serializeMessage);//添加到队列尾
		}finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public List<ChangeMessage<K, V>> getChangedMessages() {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			long len = jedis.llen(CHANGE_MESSAGE);
			List<ChangeMessage<K, V>> changeMessages = new ArrayList<ChangeMessage<K, V>>();
			if(len > 0){
				ShardedJedisPipeline pipeline = jedis.pipelined();
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
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public boolean expiry(K key, int seconds) throws CacheException {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			String buildPutToCacheKey = buildCacheKey(key);
			return jedis.expire(buildPutToCacheKey, seconds) == 1;
		}finally{
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}

	@Override
	public Set<V> keySet(String pattern) {
		ShardedJedis jedis = CacheHostManager.getJedisCluster(getCacheClusterName());
		try{
			ShardedJedisPipeline pipeline = jedis.pipelined();
			Collection<Jedis> allShards = jedis.getAllShards();
			List<String> allKeys = new ArrayList<String>(); 
			String truePattern = buildCacheKey((K)pattern);
			for (Jedis jed : allShards) {
				Set<String> keys = jed.keys(truePattern);
				allKeys.addAll(keys);
				for(String key : keys){
					pipeline.get(key);
				}
			}
			
			List<Object> rtn = pipeline.syncAndReturnAll();
			if(allKeys.size() != rtn.size()) {
				throw new CacheException("Query results not equal with keys.");
			}
			
			Set<V> data = new HashSet<V>(); 
			int i=0;
			Iterator<String> k = allKeys.iterator();
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
			CacheHostManager.getShardedJedisPool(getCacheClusterName()).returnResource(jedis);
		}
	}
	
}
