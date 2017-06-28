package com.hualife.foundation.component.cache.redis.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.eos.infra.config.Configuration.Group;
import com.eos.infra.config.Configuration.Module;
import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;

public class CacheHostManager {
//	private static Map<String/*redis cluster name*/,JedisCluster> jedisClusters;
	private static Map<String/*redis cluster name*/,ShardedJedisPool> jedisClusters;
	private static Map<String/*connection pool name*/,JedisPoolConfig> jedisPools;
	private static Map<String/*cache host name*/, JedisPool> jedises;
	
	private CacheHostManager(){}
	
	public static void initJedisConfig() {
		initPools();
		initJedises();
		initClusters();
	}

	private static void initJedises() {
		if(null == jedises) {
			jedises = new ConcurrentHashMap<String, JedisPool>();
			Module cacheHost = ConfigurationFactory.getUserConfiguration().getModule("CacheHost");
			if (cacheHost == null){
				return;
			}
			Map<String, Group> groups = cacheHost.getGroups();
			if (groups == null){
				return;
			} 
			Iterator<String> groupNames = groups.keySet().iterator();
			while(groupNames.hasNext()) {
				String groupName = groupNames.next();
				String address = cacheHost.getConfigValue(groupName, "Address");
				String password = cacheHost.getConfigValue(groupName, "Password");
				String timeOut = cacheHost.getConfigValue(groupName, "TimeOut");
				int timeout = 10000;
				if(StringUtil.isNotNullAndBlank(timeOut)){
					timeout = Integer.valueOf(timeOut)*1000;
				}
				String connectionPool = cacheHost.getConfigValue(groupName, "ConnectionPool");
				if(StringUtil.isNullOrBlank(address)){
					throw new CacheException("The configuration of CacheHost-"+groupName+"-Address should not be null");
				}
				HostAndPort hostAndPort = getHostAndSets(address).iterator().next();
				JedisPool pool = null;
				if(StringUtil.isNullOrBlank(password) || "NONE".equalsIgnoreCase(password)){
					pool = new JedisPool(jedisPools.get(connectionPool), hostAndPort.getHost(),hostAndPort.getPort(),timeout);
				}else {
					pool = new JedisPool(jedisPools.get(connectionPool), hostAndPort.getHost(),hostAndPort.getPort(),timeout,password);
				}
				jedises.put(groupName, pool);
			}
		}
	}

	private static void initPools() {
		if(null == jedisPools) {
			jedisPools = new ConcurrentHashMap<String, JedisPoolConfig>();
			Module cacheConnectionPoolModule = ConfigurationFactory.getUserConfiguration().getModule("CacheConnectionPool");
			if (cacheConnectionPoolModule == null){
				return;
			}
			Map<String, Group> groups = cacheConnectionPoolModule.getGroups();
			if(groups == null){
				return;
			}
			Iterator<String> groupNames = groups.keySet().iterator();
			while(groupNames.hasNext()) {
				JedisPoolConfig config = new JedisPoolConfig();
				String groupName = groupNames.next();
				String maxActive = cacheConnectionPoolModule.getConfigValue(groupName, "MaxActive");
				String maxIdle = cacheConnectionPoolModule.getConfigValue(groupName, "MaxIdle");
				String maxWait = cacheConnectionPoolModule.getConfigValue(groupName, "MaxWait");
				String testOnBorrow = cacheConnectionPoolModule.getConfigValue(groupName, "TestOnBorrow");
				
				if(StringUtil.isNullOrBlank(maxActive)){
					config.setMaxTotal(50);
				}else{
					config.setMaxTotal(Integer.valueOf(maxActive));
				}
				if(StringUtil.isNullOrBlank(maxIdle)){
					config.setMaxIdle(5);
				}else{
					config.setMaxIdle(Integer.valueOf(maxIdle));
				}
				if(StringUtil.isNullOrBlank(maxWait)){
					config.setMaxWaitMillis(1000*10);
				}else{
					config.setMaxWaitMillis(Integer.valueOf(maxWait)*1000);
				}
				if(StringUtil.isNullOrBlank(testOnBorrow)){
					config.setTestOnBorrow(true);
				}else{
					config.setTestOnBorrow(Boolean.valueOf(testOnBorrow));
				}
				jedisPools.put(groupName, config);
			}
		}
	}

	private static void initClusters() {
		if(null == jedisClusters){
			jedisClusters = new ConcurrentHashMap<String, ShardedJedisPool>();
			Module module = ConfigurationFactory.getUserConfiguration().getModule("CacheCluster");
			if(module == null){
				return;
			}
			Map<String, Group> groups = module.getGroups();
			if(groups == null){
				return;
			}
			Iterator<String> keys = groups.keySet().iterator();
			while(keys.hasNext()) {
				String key = keys.next();
				Group group = module.getGroup(key);
				
				String clusterName = group.getName();
				String timeout = group.getConfigValue("TimeOut");
				int timeOut = 10000;
				if(StringUtil.isNotNullAndBlank(timeout)){
					timeOut = Integer.valueOf(timeout)*1000;
				}
//				int maxRedirections = Integer.valueOf(group.getConfigValue("MaxRedirections"));
				String connectionPool = group.getConfigValue("ConnectionPool");
				String srcAddrs = group.getConfigValue("Address");
//				Set<HostAndPort> hostAndPortSet = getHostAndSets(srcAddrs);
				List<JedisShardInfo> shardInfos = getShardInfos(srcAddrs);
				JedisPoolConfig config = getJedisPoolConfig(connectionPool);
				ShardedJedisPool shardedJedisPool = new ShardedJedisPool(config, shardInfos);	
//			    JedisCluster cluster = new JedisCluster(hostAndPortSet,timeOut, maxRedirections, config);
			    jedisClusters.put(clusterName, shardedJedisPool);
			}
		}
	}

	private static Set<HostAndPort> getHostAndSets(String srcAddrs) {
		Set<HostAndPort> hostAndPortSet = new HashSet<HostAndPort>();
		if(StringUtil.isNotNullAndBlank(srcAddrs)) {
			String[] ipAndPorts = srcAddrs.split(";");
			for (String ipAndPort : ipAndPorts) {
				String[] hostInfos = ipAndPort.split(":");
				HostAndPort e = new HostAndPort(hostInfos[0], Integer.valueOf(hostInfos[1]));
				hostAndPortSet.add(e);
			}
		}
		return hostAndPortSet;
	}
	
	private static List<JedisShardInfo> getShardInfos(String srcAddrs) {
		List<JedisShardInfo> shardInfos = new ArrayList<JedisShardInfo>();
		if(StringUtil.isNotNullAndBlank(srcAddrs)) {
			String[] ipAndPorts = srcAddrs.split(";");
			for (String ipAndPort : ipAndPorts) {
				String[] hostInfos = ipAndPort.split(":");
				JedisShardInfo shardInfo = new JedisShardInfo(hostInfos[0], Integer.valueOf(hostInfos[1]));
				if(hostInfos.length > 2){
					String auth = hostInfos[2];
					shardInfo.setPassword(auth);
				}
				shardInfos.add(shardInfo);
			}
		}
		return shardInfos;
	}

	private static JedisPoolConfig getJedisPoolConfig(String connectionPool) {
		return jedisPools.get(connectionPool);
	}
	
	public static void shutdown(String cacheHostOrCluserName) {
		JedisPool jedisPool = jedises.get(cacheHostOrCluserName);
		if(jedisPool != null) {
			jedisPool.destroy();
		}
		ShardedJedisPool cluster = jedisClusters.get(cacheHostOrCluserName);
		if(null != cluster) {
			try {
				cluster.destroy();
			} catch (Throwable e) {
				throw new CacheException("close jedis cluster error.",e);
			}
		}
	}
	
	public static ShardedJedis getJedisCluster(String cacheCluster) {
		ShardedJedisPool jedisCluster = getShardedJedisPool(cacheCluster);
		ShardedJedis jedis = jedisCluster.getResource();
		return jedis;
	}
	
	public static ShardedJedisPool getShardedJedisPool(String cacheCluster) {
		return jedisClusters.get(cacheCluster);
	}
	
	public static Jedis getJedis(String cacheHost) {
		JedisPool jedisPool = getJedisPool(cacheHost);
		return jedisPool.getResource();
	}
	
	public static JedisPool getJedisPool(String cacheHost){
		JedisPool jedisPool = jedises.get(cacheHost);
		return jedisPool;
	}
}
