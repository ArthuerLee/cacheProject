package com.hualife.foundation.component.cache;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.config.CacheConfiguration;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.provider.CachingProvider;
//import com.primeton.btp.api.core.logger.ILogger;
//import com.primeton.btp.api.core.logger.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Caching {
	private static ConcurrentHashMap<String/*providerName*/, CachingProvider> providers = new ConcurrentHashMap<String, CachingProvider>();
	private static ConcurrentHashMap<String/*cacheName*/, String/*providerName*/> map = new ConcurrentHashMap<String, String>();
	private static ConcurrentHashMap<String/*cacheHost/cacheCluster*/, RemoteCache<String,String>/*systemCache*/> systemCacheMapping = new ConcurrentHashMap<String, RemoteCache<String,String>>();
	//private static ILogger logger = LoggerFactory.getLogger(Caching.class);
	private static Logger logger = LoggerFactory.getLogger(Caching.class);

    private Caching() {}
    
	public static CacheManager getCacheManager(String cacheName){
		String providerName = map.get(cacheName);
		if(StringUtil.isNotNullAndBlank(providerName)){
			CachingProvider provider = providers.get(providerName);
			if(null != provider) {
				return provider.getCacheManager();
			}
		}
		return null;
	}
	
	public static Cache getCache(String cacheName){
		return getCacheManager(cacheName).getCache(cacheName);
	}
	
	public static void addProvider(String name ,CachingProvider provider) {
		providers.putIfAbsent(name, provider);
	}
	
	public static void addCacheNameAndProviderMapping(String cacheName, String providerName){
		if(map.containsKey(cacheName)) {
			throw new CacheException("Cache name can not repeate.");
		}
		map.putIfAbsent(cacheName, providerName);
	}
	
	public static void addSystemCacheMapping(String cacheHostOrClusterName, Cache<String,String> systemCache){
		if(systemCache instanceof RemoteCache){
			if(systemCacheMapping.containsKey(cacheHostOrClusterName)) {
				throw new CacheException("Cache name can not repeate.");
			}
			systemCacheMapping.putIfAbsent(cacheHostOrClusterName, (RemoteCache<String, String>) systemCache);
		}
	}
	
	/**
     * 每个CacheHost/CacheCluster都有一个系统缓存。
     * 目前保存一些状态信息，用于检查当前进程内的刷新、持久化线程是否会可执行
     * @return
     */
	public static RemoteCache<String,String> getSystemCache(String cacheHostOrClusterName){
		return systemCacheMapping.get(cacheHostOrClusterName);
	}
	
	public static void removeSystemCache(String cacheHostOrClusterName){
		systemCacheMapping.remove(cacheHostOrClusterName);
	}
	
	public static CachingProvider removeProvider(String name) {
		return providers.remove(name);
	}
	
	public static CachingProvider getProviderByProviderName(String name) {
		return providers.get(name);
	}
	
	public static CachingProvider getProviderByCacheName(String cacheName){
		String providerName = map.get(cacheName);
		if(StringUtil.isNotNullAndBlank(providerName)){
			return providers.get(providerName);
		}
		return null;
	}
	
	public static void start() {
		if(logger.isDebugEnabled()){
			logger.debug("Cache start beginning.");
		}
		
		startBySearchingFile();
		
		if(logger.isDebugEnabled()){
			logger.debug("Cache start ended.");
		}
	}

	private static void startBySearchingFile() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		List<CacheConfiguration> configs = ConfigurationFactory.parseConfiguration(classLoader);
		for (CacheConfiguration configuration : configs) {
			doStartCache(configuration);
		}
	}
	
	public static void start(String configPath) {
		if(logger.isDebugEnabled()){
			logger.debug("Caching start beginning.");
		}
		if(StringUtil.isNullOrBlank(configPath)) {
			if(logger.isErrorEnabled()){
				logger.error("Cache configuration path is null.");
			}
			throw new CacheException("Configuration path can't find.");
		}
		try {
			File userConfig = new File(StringUtil.concat(configPath,ConfigurationFactory.DEFAULT_CLASSPATH_USER_CONFIGURATION_FILE));
			File cacheConfig = new File(StringUtil.concat(configPath,ConfigurationFactory.DEFAULT_CLASSPATH_CONFIGURATION_FILE));

			ConfigurationFactory.initUserConfigConfiguration(userConfig);
			List<CacheConfiguration> configs = ConfigurationFactory.initCacheConfiguration(cacheConfig);;
			for (CacheConfiguration configuration : configs) {
				doStartCache(configuration);
			}
			if(logger.isDebugEnabled()){
				logger.debug("Caching start ended.");
			}
		} catch (Throwable e) {
			if(logger.isErrorEnabled()){
				logger.error("Caching start exception.",e);
			}
			throw new CacheException(e);
		}
		
		
	}

	public static void doStartCache(CacheConfiguration configuration) {
		String cacheProvider = configuration.getCacheProvider();
		String cacheName = configuration.getName();
		CachingProvider provider = providers.get(cacheProvider);
		if(provider == null){
			if(logger.isErrorEnabled()){
				logger.error("can't find cache provider: " + cacheProvider);
			}
			throw new CacheException("can't find cache provider: " + cacheProvider);
		}
		CacheManager manager = provider.getCacheManager();
		CacheBuilder builder = manager.createCacheBuilder(cacheName);
		// 缓存启动时统一初始化配置
		Cache cache = builder.build(configuration);
		cache.setCacheProvider(provider);
		try{
			cache.start();
			Caching.addCacheNameAndProviderMapping(cacheName, provider.getName());
			manager.addCache(cache);
		}catch(Throwable e){
			if(logger.isErrorEnabled()){
				logger.error("Cache ["+cache.getName()+"] start exception" ,e);
			}
			throw new CacheException("Cache ["+cache.getName()+"] start exception" ,e);
		}
		
	}
	
	public static void stop() {
		Iterator<CachingProvider> provider = providers.values().iterator();
		while(provider.hasNext()) {
			CachingProvider pro = provider.next();
			pro.getCacheManager().shutdown();
		}
		map.clear();
		Iterator<String> keys = systemCacheMapping.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			Cache systemCache = systemCacheMapping.get(key);
			systemCache.stop();
		}
	}
}
