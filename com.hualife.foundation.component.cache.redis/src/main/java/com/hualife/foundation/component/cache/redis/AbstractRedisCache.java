package com.hualife.foundation.component.cache.redis;

import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.AbstractCache;
import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheKeySetSupportable;
import com.hualife.foundation.component.cache.Caching;
import com.hualife.foundation.component.cache.RemoteCache;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.config.SystemCacheInfo;
import com.hualife.foundation.component.cache.config.model.SerializeType;
import com.hualife.foundation.component.cache.provider.CachingProvider;
import com.hualife.foundation.component.cache.redis.cluster.RedisClusterCacheProvider;
import com.hualife.foundation.component.cache.redis.serialize.DataSerializeSupport;
import com.hualife.foundation.component.cache.redis.single.RedisSingleCacheProvider;

public abstract class AbstractRedisCache<K, V> extends AbstractCache<K, V> implements RemoteCache<K, V>, CacheKeySetSupportable<K>{
	String lastStatus;
	long count;
	long seq;
	private boolean isMaster;
	public static final String CHANGE_MESSAGE= "__CHANGE_MESSAGE__";
	
	@Override
	public boolean checkSynPrevilige() {
		if(ConfigurationFactory.SYSTEM_CACHE_NAME.equals(getName())){
			return false;
		}
		boolean rtn = false;
		Cache<String, String> systemCache = getSystemCache();
		String buildFieldName = buildFieldName();
		String status = systemCache.get(buildFieldName);
		if(status == null){
			rtn = systemCache.putIfAbsent(buildFieldName, SystemCacheInfo.INSTANCE.getNodeId() + ":" + ++seq);
			count = 0;
		}else{
			String[] strs = status.split(":");
			if(strs[0].equals(SystemCacheInfo.INSTANCE.getNodeId())){
				rtn = true;
			}else{
				if(lastStatus != null && lastStatus.equals(status))
					++count;
				else {
					count = 0;
				}
			}
		}
		lastStatus = status;
		return rtn;
	}
	
	
	
	public String getCacheHostOrClusterName(){
		CachingProvider provider = getCacheProvider();
		if(provider instanceof RedisClusterCacheProvider) {
			RedisClusterCacheProvider clusterCacheProvider = (RedisClusterCacheProvider)provider;
			return clusterCacheProvider.getCluster();
		}else if(provider instanceof RedisSingleCacheProvider){
			RedisSingleCacheProvider redisSingleCacheProvider = (RedisSingleCacheProvider)provider;
			return redisSingleCacheProvider.getCacheHost();
		}
		return null;
	}

	@Override
	public boolean grabSynPrevilige() {
		seq = 0;
		Cache<String, String> systemCache = getSystemCache();
		boolean rtn = systemCache.replace(buildFieldName(), lastStatus, SystemCacheInfo.INSTANCE.getNodeId() + ":" + ++seq);
		if(rtn){
			count = 0;
		}
		lastStatus = (String) systemCache.get(buildFieldName());
		return rtn;
	}
	
	@Override
	public boolean refreshSynStatus() {
		Cache<String, String> systemCache = getSystemCache();
		boolean rtn = systemCache.replace(buildFieldName(), lastStatus, SystemCacheInfo.INSTANCE.getNodeId() + ":" + ++seq);
		count = 0;
		lastStatus = (String) systemCache.get(buildFieldName());
		return rtn;
		
	}
	
	@Override
	public boolean checkSwitchSynCondition() {
		String timeStr = System.getProperty("SwitchMasterTime");
		int time = 300;//默认是5分钟
		if(StringUtil.isNotNullAndBlank(timeStr)){
			time = Integer.valueOf(timeStr);
		}
		if(count < time){
			return false;
		}
		return true;
	}
	
	private String buildFieldName() {
		return SystemCacheInfo.INSTANCE.getAppName()+"_"+getName();
	}
	
	public K getTrueKey(String cacheNameAndSeriKey) {
		String trueKeyString = cacheNameAndSeriKey.substring((getName()+"__app__").length());
		K k = (K)DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).deserialize(trueKeyString);
		return k;
	}
	
	public String buildCacheKey(K key) {
//		if(key instanceof String){
//			return getName() + key;
//		}
		String serializeKey = DataSerializeSupport.getInstance().getDataSerialize(getSerializeType()).serialize(key);
		return getName() + "__app__" + serializeKey;
	}
	
	@Override
	public RemoteCache<String, String> getSystemCache() {
		String cacheHostOrClusterName = getCacheHostOrClusterName();
		RemoteCache<String, String> systemCache = Caching.getSystemCache(cacheHostOrClusterName);
		return systemCache;
	}
	
	@Override
	public boolean isMaster() {
		return isMaster;
	}
	
	@Override
	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}
	
	@Override
	public SerializeType getSerializeType() {
		return serializeType;
	}

}
