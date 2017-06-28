package com.hualife.foundation.component.cache.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eos.infra.config.Configuration.Group;
import com.eos.infra.config.Configuration.Module;
import com.eos.infra.config.Configuration.Value;
import com.eos.runtime.core.ApplicationContext;
import com.eos.system.utility.StringUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hualife.foundation.component.cache.config.CacheConfiguration;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.config.SystemCacheInfo;
import com.hualife.foundation.component.cache.listener.WebCacheStartUpListener;
 

public class Monitor {


	//private final static Properties prop =  MonitorUtil.getProperties("/cache_monitor.properties");
	
    private final static String PACKAGE_NAME = "com.hualife.foundation.component.cache.monitor.task";
	   
    private final static String INTERFACE = "java.lang.Runnable";
    
    private final static String GUAVA_CACHE_PROVIDER="GuavaCacheProvider";
    
    private final static String REDIS_CLUSTER_CACHE_PRROVIDER="RedisClusterCacheProvider";
    
    private final static String REDIS_SINGLE_CACHE_PROVIDER="RedisSingleCacheProvider";
    
    private final static String CONTACTOR ="@";
    
    public static final String MONITOR_PROPERTIES_FILE = "/cache_monitor.properties";
    public static final String LOGBACK = "/cache_logback.xml";
    
    private final static Properties prop =  new  Properties();
    
    private final static Cache<String,Object> cache = CacheBuilder.newBuilder().build();
    
	private Monitor(){
		 
	}
	//创建本地缓存guava
	private static class MonitorCache{
		
		//static CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
	  static LoadingCache<String,List<Indices>> cacheBuilder = CacheBuilder.newBuilder().concurrencyLevel(100).build(
				
				 new CacheLoader<String, List<Indices>>() {
					 
					 public List<Indices> load(String key) throws Exception{
						  List<Indices> indicesLst = new ArrayList<Indices>();
						
						  return indicesLst;
					   }

				    }
				
				) ;
	   }
	
	
	 //获取存储指标的本地缓存guava实例
	 public static final LoadingCache<String,List<Indices>> getMonitorCache(){
		 
		 return MonitorCache.cacheBuilder;
	 }
	 
	 public static Cache<String,Object> getCache(){
		return Monitor.cache;
	 }
	 //采集缓存统计保存本地缓存
	 public static void recordCacheStatistics(String cacheName,OperationType oprType,Object result){
		      //查询缓存
		      if(OperationType.QUERY.equals(oprType)){
		        Object o = getCache().getIfPresent(cacheName+CONTACTOR+oprType.name());
				if(o!=null && o instanceof Integer){
					getCache().put(cacheName+CONTACTOR+oprType.name(), ((Integer)o).intValue()+1);
				}else{
					getCache().put(cacheName+CONTACTOR+oprType.name(), 1);
				}
			 if(result==null){
				 Object om = getCache().getIfPresent(cacheName+CONTACTOR+OperationType.QUERY_MISS.name());
					if(om!=null && om instanceof Integer){
						getCache().put(cacheName+CONTACTOR+OperationType.QUERY_MISS.name(), ((Integer)om).intValue()+1);
					}else{
						getCache().put(cacheName+CONTACTOR+OperationType.QUERY_MISS.name(), 1);
					} 
			 } 
			 //缓存加载
		   }else if(OperationType.LOAD.equals(oprType)){
			 
			   Object o = getCache().getIfPresent(cacheName+CONTACTOR+OperationType.LOAD.name());
				if(o!=null && o instanceof Integer){
					getCache().put(cacheName+CONTACTOR+OperationType.LOAD.name(), ((Integer)o).intValue()+1);
				}else{
					getCache().put(cacheName+CONTACTOR+OperationType.LOAD.name(), 1);
				} 
		    //加载异常
		   }else if(OperationType.LOAD_EXCEPTION.equals(oprType)){
			  
			   Object o = getCache().getIfPresent(cacheName+CONTACTOR+OperationType.LOAD_EXCEPTION.name());
				if(o!=null && o instanceof Integer){
					getCache().put(cacheName+CONTACTOR+OperationType.LOAD_EXCEPTION.name(), ((Integer)o).intValue()+1);
				}else{
					getCache().put(cacheName+CONTACTOR+OperationType.LOAD_EXCEPTION.name(), 1);
				} 
			 //缓存持久化
		   }else if(OperationType.WRITE.equals(oprType)){
			 
			   Object o = getCache().getIfPresent(cacheName+CONTACTOR+OperationType.WRITE.name());
				if(o!=null && o instanceof Integer){
					getCache().put(cacheName+CONTACTOR+OperationType.WRITE.name(), ((Integer)o).intValue()+1);
				}else{
					getCache().put(cacheName+CONTACTOR+OperationType.WRITE.name(), 1);
				} 
			//持久化异常
		   }else if(OperationType.WRITE_EXCEPTION.equals(oprType)){
			  
			   Object o = getCache().getIfPresent(cacheName+CONTACTOR+OperationType.WRITE_EXCEPTION.name());
				if(o!=null && o instanceof Integer){
					getCache().put(cacheName+CONTACTOR+OperationType.WRITE_EXCEPTION.name(), ((Integer)o).intValue()+1);
				}else{
					getCache().put(cacheName+CONTACTOR+OperationType.WRITE_EXCEPTION.name(), 1);
				} 
		   }
		
	 }
	 //采集缓存轨迹保存本地缓存
	 public static void recordCacheTrack(String cacheName,String oprType){
		 @SuppressWarnings("unchecked")
		List<Track> trackLst = getCache().getIfPresent("trackLst")!=null? (ArrayList<Track>)getCache().getIfPresent("trackLst"):new ArrayList<Track>();
		Track t = new Track();
		t.setAppName("appName:");
		t.setCacheName("cacheName:"+cacheName);
		t.setCacheAddress("address:"+MonitorUtil.getLocalAddress());
		t.setOprType(oprType);
		t.setOprTime("oprTime:"+new Date());
		trackLst.add(t);
		getCache().put("trackLst", trackLst);
	 }
	 //获取缓存框架配置类指标信息，并存入本地缓存
	 public static void saveConfigInfoInLocalGuava(){
		 
		   List<Indices> configLst = new ArrayList<Indices>();
		   Map<String,CacheConfiguration> cc = ConfigurationFactory.getCachesConfigs();
		      
	        for (Map.Entry<String, CacheConfiguration> scc : cc.entrySet()) { 
	        	CacheConfiguration c = scc.getValue();
	        	Indices  indices = new Indices();	
	        	indices.setAppName("appName:"+SystemCacheInfo.INSTANCE.getAppName());
	            System.out.println("provider============================="+c.getCacheProvider());
	            
	            Map<String,Module> m =  ConfigurationFactory.getUserConfiguration().getModules();
	            
	            for (Map.Entry<String, Module> entry : m.entrySet()) { 
	            	
	            	Module mdl = entry.getValue();
	            	//System.out.println("module name is:"+mdl.getName());
	            	Map<String,Group> g = mdl.getGroups();
	            	for (Map.Entry<String, Group> sm : g.entrySet()) {
	            		Group grp = sm.getValue();
	            		 System.out.println("group name==========================="+grp.getName());
	            		 if(c.getCacheProvider().equals(grp.getName())){
	            			 
	            	       		Map<String,Value> v = grp.getValues();
	    	            		for (Map.Entry<String, Value> sv : v.entrySet()) {
	    	            			Value val = sv.getValue();
	    	            			if(val.getValue().endsWith(GUAVA_CACHE_PROVIDER)){
	    	            				indices.setCacheType("cacheType:"+CacheType.LOCAL.name());
	    	            			}else if(val.getValue().endsWith(REDIS_CLUSTER_CACHE_PRROVIDER)){
	    	            				indices.setCacheType("cacheType:"+CacheType.REMOTE.name());
	    	            			}else{
	    	            				indices.setCacheType("cacheType:"+CacheType.REMOTE.name());
	    	            			}
	    	            			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"+indices.getCacheType());
	    	            			break;
	    	            		}
	            		 }
	            		 
	            	}
	            	
	            }
	            indices.setCacheName("cacheName:"+c.getName());
	            indices.setCacheAddress("cacheAddress:"+MonitorUtil.getLocalAddress());
	            indices.setCacheDuration("cacheDuration:"+c.getExpiry().getTimeToLive());
	            indices.setMainNode("mainNode:"+SystemCacheInfo.INSTANCE.getNodeId());
	            indices.setIsStatisticsEnabled("isStatisticsEnabled:"+c.isStatisticsEnabled());
	            indices.setIsTransactionEnabled("isTransactionEnabled:"+c.isTransactionEnabled());
	            
	            Map<String,Boolean> listenerMap = new HashMap<String,Boolean>();
	            if(c.getCacheEntryCreatedListener()!=null){
	            	listenerMap.put(c.getCacheEntryCreatedListener().getListenerType().name(), c.getCacheEntryCreatedListener().isSyn());
	            }
	            if(c.getCacheEntryExpiredListener()!=null){
	                listenerMap.put(c.getCacheEntryExpiredListener().getListenerType().name(), c.getCacheEntryExpiredListener().isSyn());
	            }
	            if(c.getCacheEntryReadListener()!=null){
	            	listenerMap.put(c.getCacheEntryReadListener().getListenerType().name(), c.getCacheEntryReadListener().isSyn());
	            }
	            if(c.getCacheEntryRemovedListener()!=null){
	                listenerMap.put(c.getCacheEntryRemovedListener().getListenerType().name(), c.getCacheEntryRemovedListener().isSyn());
	            }
	            if(c.getCacheEntryUpdatedListener()!=null){
	            	listenerMap.put(c.getCacheEntryUpdatedListener().getListenerType().name(), c.getCacheEntryUpdatedListener().isSyn());
	            }
	            if(c.getCacheLoader()!=null){
	            	indices.setLoadFrequency("loadFrequency:"+c.getCacheLoader().getAsyn().getPeriod());
	            }
	            if(c.getCacheWriter()!=null){
	            	indices.setWriteFrequency("writeFrequency:"+c.getCacheWriter().getAsyn().getPeriod());
	            	indices.setBufferSize("bufferSize:"+c.getCacheWriter().getAsyn().getBufferSize());
	            }
/*	            System.out.println("isStatisticsEnabled============"+c.isStatisticsEnabled());
	            System.out.println("isTransactionEnabled==========="+c.isTransactionEnabled());
	            //System.out.println("listenerType==========="+c.getCacheEntryCreatedListener().getListenerType());
	           // System.out.println("isSyn==========="+c.getCacheEntryCreatedListener().isSyn());
	            System.out.println("loadFrequency==========="+c.getCacheLoader().getAsyn().getPeriod());
	            System.out.println("writeFrequency==========="+c.getCacheWriter().getAsyn().getPeriod());
	            System.out.println("bufferSize==========="+c.getCacheWriter().getAsyn().getBufferSize());
	            
	        	System.out.println("cache name==================="+c.getName());
	        	System.out.println("scc key=========================="+scc.getKey());
	        	System.out.println("cacheDuration=========================="+c.getExpiry().getTimeToLive());
	        	System.out.println("policy=========================="+c.getExpiryType().name());*/
	        	configLst.add(indices);
	        }
		 
		 getMonitorCache().put("configLst", configLst);
	 }
	 //biip启动定时采集日志功能
	 public static void startMonitor(){
		 
	     ScheduledExecutorService service=Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
         try{
        	  String monitorPath = ApplicationContext.getInstance().getApplicationConfigPath(); 
        	  System.out.println("monitor path is: "+StringUtil.concat(monitorPath,MONITOR_PROPERTIES_FILE));
        	  FileInputStream fis = new FileInputStream(new File(StringUtil.concat(monitorPath,MONITOR_PROPERTIES_FILE)));
              System.out.println("is============================"+fis);
              prop.load(fis);
        	  //String configPath = WebCacheStartUpListener.class.getResource("/").getPath().split("WEB-INF")[0].concat("WEB-INF");
        	  //LogBackConfigLoader.load(StringUtil.concat(monitorPath,LOGBACK));
	          List<Class> classes = ClassUtil.getAllClassByInterface(PACKAGE_NAME,Class.forName(INTERFACE));
           for (Class clas :classes) {  
               //获取class名称
               String clsName = ClassUtil.getClassName(clas.getName());
               
               System.out.println("clsName======================================"+clsName);
               //获取执行频率
               long interval = Long.parseLong(prop.getProperty(clsName).trim()) ;
               System.out.println("interval=============================="+interval);
               Class<?> clazz = Class.forName(clas.getName());
               //获得Runnable实例
               Runnable m = (Runnable) clazz.newInstance();
               //启动定时线程
               service.scheduleAtFixedRate(m,0,interval,TimeUnit.SECONDS);
           }
         }catch(Throwable t){
        	 t.printStackTrace();
         }
	 }
	//web启动定时采集日志功能
	 public static void startMonitor(String s){
		 
	     ScheduledExecutorService service=Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
         try{
        	  String monitorPath = WebCacheStartUpListener.class.getResource("/").getPath().split("WEB-INF")[0].concat("WEB-INF"); 
        	  System.out.println("monitor path is: "+StringUtil.concat(monitorPath,MONITOR_PROPERTIES_FILE));
        	  FileInputStream fis = new FileInputStream(new File(StringUtil.concat(monitorPath,MONITOR_PROPERTIES_FILE)));
              System.out.println("is============================"+fis);
              prop.load(fis);
        	  //String configPath = WebCacheStartUpListener.class.getResource("/").getPath().split("WEB-INF")[0].concat("WEB-INF");
        	  //LogBackConfigLoader.load(StringUtil.concat(monitorPath,LOGBACK));
	          List<Class> classes = ClassUtil.getAllClassByInterface(PACKAGE_NAME,Class.forName(INTERFACE));
           for (Class clas :classes) {  
               //获取class名称
               String clsName = ClassUtil.getClassName(clas.getName());
               
               System.out.println("clsName======================================"+clsName);
               //获取执行频率
               long interval = Long.parseLong(prop.getProperty(clsName).trim()) ;
               System.out.println("interval=============================="+interval);
               Class<?> clazz = Class.forName(clas.getName());
               //获得Runnable实例
               Runnable m = (Runnable) clazz.newInstance();
               //启动定时线程
               service.scheduleAtFixedRate(m,0,interval,TimeUnit.SECONDS);
           }
         }catch(Throwable t){
        	 t.printStackTrace();
         }
	 }
	 
	  enum CacheType {

	        LOCAL,

	        REMOTE
      }
	 
	 public static void main(String[] args) throws IOException {
		
		 FileInputStream fis = new FileInputStream(new File("D:/logs/monitor.properties"));
         Properties prop =  new  Properties();
         prop.load(fis);
         System.out.println("is============================"+prop);
         
	 }
}
