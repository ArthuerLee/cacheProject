package com.hualife.foundation.component.cache.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.eos.infra.config.Configuration;
import com.eos.infra.config.Configuration.Group;
import com.eos.infra.config.Configuration.Module;
import com.eos.system.utility.ClassUtil;
import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.CacheBuilder;
import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.CacheManager;
import com.hualife.foundation.component.cache.Caching;
import com.hualife.foundation.component.cache.config.SystemCacheInfo.StartingMode;
import com.hualife.foundation.component.cache.config.model.Cache;
import com.hualife.foundation.component.cache.config.model.Caches;
import com.hualife.foundation.component.cache.provider.CachingProvider;
//import com.primeton.btp.api.core.logger.ILogger;
//import com.primeton.btp.api.core.logger.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache Configuration Factory
 */
public class ConfigurationFactory {

//	private static ILogger logger = LoggerFactory.getLogger(ConfigurationFactory.class);

    public static final String DEFAULT_CLASSPATH_CONFIGURATION_FILE = "/cache.xml";
    public static final String DEFAULT_CLASSPATH_USER_CONFIGURATION_FILE = "/user-config.xml";
    private static Configuration configuration;
    //private static ILogger logger = LoggerFactory.getLogger(ConfigurationFactory.class);
    private static Logger logger = LoggerFactory.getLogger(Caching.class);
    private static Map<String,CacheConfiguration> configs = new ConcurrentHashMap<String,CacheConfiguration>();

    /**
     * Constructor.
     */
    private ConfigurationFactory() {

    }

    /**
     * Configures a bean from an XML file.
     */
    public static List<CacheConfiguration> parseConfiguration(File file) throws CacheException {
        if (file == null) {
            throw new CacheException("Attempt to configure cache from null file.");
        }
        
        String absolutePath = file.getAbsolutePath();
        if(logger.isDebugEnabled()){
			logger.debug("find config file: " + absolutePath);
		}
		JAXBContext jContext;
		try {
			jContext = JAXBContext.newInstance(Caches.class);
			
			Unmarshaller unmarshaller = jContext.createUnmarshaller();
			Caches model = (Caches) unmarshaller.unmarshal(file);
			SystemCacheInfo.INSTANCE.setAppName(model.getAppName());
			
			for(Cache cache : model.getCache()){
				DefaultCacheConfiguration config = new DefaultCacheConfiguration();
				config.setModel(cache);
				configs.put(cache.getName(), config);
			}
			if(logger.isDebugEnabled()){
				logger.debug("load config file[" + file.getAbsolutePath() + "] success.");
			}
		} catch (Throwable e) {
			if(logger.isErrorEnabled()){
				logger.error("load config file[" + file.getAbsolutePath() + "] exception.", e);
			}
			throw new CacheException("load config file[" + file.getAbsolutePath() + "] exception.", e);
		}       
		ArrayList<CacheConfiguration> cacheConfs = new ArrayList<CacheConfiguration>();
		cacheConfs.addAll(configs.values());
		return cacheConfs;
    }
    
    /**
     * Configures a bean from an XML file in the classpath.
     */
    public static List<CacheConfiguration> parseConfiguration(ClassLoader classLoader) throws CacheException {
    	List<CacheConfiguration> allConfigs = new ArrayList<CacheConfiguration>();
        Enumeration<URL> cacheConfigFiles;
        Enumeration<URL> userCacheConfigFiles;
		try {
			cacheConfigFiles = classLoader.getResources(DEFAULT_CLASSPATH_CONFIGURATION_FILE);
			userCacheConfigFiles = classLoader.getResources(DEFAULT_CLASSPATH_USER_CONFIGURATION_FILE);
		} catch (IOException e) {
			throw new CacheException("load cache config exception: ", e);
		}
		while(userCacheConfigFiles.hasMoreElements()) {
			URL url = cacheConfigFiles.nextElement();
			Configuration config = parseUserConfiguration(new File(url.getPath() + File.separator + url.getFile()));
			// 加载Provider
			Module provider = config.getModule("CacheProvider");
			Iterator<String> keys = provider.getGroups().keySet().iterator();
			while(keys.hasNext()){
				String key = keys.next();
				Group group = provider.getGroup(key);
				String clazz = group.getConfigValue("Clazz");
				if(null != clazz && !"".equals(clazz)){
//					CachingProvider pro = (CachingProvider) ClassUtils.newInstance(clazz, classLoader);
//					pro.setCluster(group.getConfigValue("Cluster"));
					CachingProvider pro = null;
					try {
						pro = (CachingProvider) ClassUtil.newInstance(clazz, new String[]{key});
					} catch (Throwable e) {
						throw new CacheException(e);
					}
					Caching.addProvider(key, pro);
				}
			}
			// 加载CacheHost，交给具体缓存实现，因为不同缓存的主机配置不一样。
			// 但是配置文件只加载一次，具体实现可以通过ConfigurationFactory获取配置信息。
		}
		while(cacheConfigFiles.hasMoreElements()){
        	URL url = cacheConfigFiles.nextElement();
        	List<CacheConfiguration> config = parseConfiguration(new File(url.getPath() + File.separator + url.getFile()));
        	allConfigs.addAll(config);
        }
        return allConfigs;
    }
    
    public static final String SYSTEM_CACHE_NAME = "__SYSTEM_CACHE_NAME__"; 
    
    public static void initUserConfigConfiguration(File file) throws CacheException {
		Configuration config = parseUserConfiguration(file);
		// 加载Provider
		Module provider = config.getModule("CacheProvider");
		if(null == provider) {
			return;
		}
		Iterator<String> keys = provider.getGroups().keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Group group = provider.getGroup(key);
			String clazz = group.getConfigValue("Clazz");
			String cacheHost = group.getConfigValue("CacheHost");
			String cacheCluster = group.getConfigValue("CacheCluster");
			String cacheHostOrClusterName = getNotNullValue(cacheHost,cacheCluster);
			if(null != clazz && !"".equals(clazz)){
				CachingProvider pro = null;
				try {
					pro = (CachingProvider) ClassUtil.newInstance(clazz, new String[]{key});
				} catch (Throwable e) {
					throw new CacheException(e);
				}
//				CachingProvider pro = (CachingProvider) ClassUtils.newInstance(clazz, ConfigurationFactory.class.getClassLoader());
//				pro.setCluster(group.getConfigValue("Cluster"));
				CacheManager cacheManager = pro.getCacheManager();
				CacheBuilder<String,String> cacheBuilder = cacheManager.createCacheBuilder(SYSTEM_CACHE_NAME);
				com.hualife.foundation.component.cache.Cache<String,String> sysCache = cacheBuilder.build();
				sysCache.setCacheProvider(pro);
				sysCache.start();
				cacheManager.addCache(sysCache);
				Caching.addSystemCacheMapping(cacheHostOrClusterName, sysCache);
//				Caching.addCacheNameAndProviderMapping(SYSTEM_CACHE_NAME, group.getName());
				Caching.addProvider(key, pro);
			}
		}
    }
    
    private static String getNotNullValue(String cacheHost, String cacheCluster) {
    	if(StringUtil.isNotNullAndBlank(cacheHost)){
    		return cacheHost;
    	}
    	if(StringUtil.isNotNullAndBlank(cacheCluster)){
    		return cacheCluster;
    	}
		return null;
	}
    
    public static CacheConfiguration getCacheConfiguration(String cacheName){
    	return configs.get(cacheName);
    }

	public static List<CacheConfiguration> initCacheConfiguration(File file) throws CacheException {
    	return parseConfiguration(file);
    }

	private static Configuration parseUserConfiguration(File file) {
		if(SystemCacheInfo.INSTANCE.getStartingMode() == StartingMode.BIIP) {
			return com.eos.common.config.ConfigurationFactory.getUserConfiguration();
		}else {
			configuration = Configuration.initConfiguration(file);
			return configuration;
		}
	}
	
	public static Configuration getUserConfiguration() {
		if(SystemCacheInfo.INSTANCE.getStartingMode() == StartingMode.BIIP) {
			return com.eos.common.config.ConfigurationFactory.getUserConfiguration();
		}
		return configuration;
	}
	
	public static Map<String,CacheConfiguration> getCachesConfigs(){
		return configs;
	}
}
