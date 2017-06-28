package com.hualife.foundation.component.cache.monitor;

public class Track  {
	
	
    //采集时间
	public long collectTime;
	//应用名称
	public String appName;
	//缓存名称
	public String cacheName;
	//操作类型
	public String oprType;
	//缓存地址
	public String cacheAddress;
    //操作时间
	public String oprTime;
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getCacheName() {
		return cacheName;
	}
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}
	public String getOprType() {
		return oprType;
	}
	public void setOprType(String oprType) {
		this.oprType = oprType;
	}
	public String getOprTime() {
		return oprTime;
	}
	public void setOprTime(String oprTime) {
		this.oprTime = oprTime;
	}
	public long getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}
	public String getCacheAddress() {
		return cacheAddress;
	}
	public void setCacheAddress(String cacheAddress) {
		this.cacheAddress = cacheAddress;
	}
 
    
	
}
