package com.hualife.foundation.component.cache.monitor;

public class Statistics  {
	
    //采集时间
	public long collectTime;
	//应用名称
	public String appName;
	//缓存名称
	public String cacheName;
	//缓存类型
	public String cacheType;
	//缓存地址
	public String cacheAddress;
	//缓存查询次数
	public String queryCount;
	//未命中次数
	public String queryMissCount;
	//加载次数
	public String loadCount;
	//加载异常次数
	public String loadExceptionCount;
	//持久化次数
	public String writeCount;
	//持久化异常次数
	public String writeExceptionCount;
	
	public long getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}
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
	
	public String getCacheType() {
		return cacheType;
	}
	public void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}
	public String getCacheAddress() {
		return cacheAddress;
	}
	public void setCacheAddress(String cacheAddress) {
		this.cacheAddress = cacheAddress;
	}
	public String getQueryCount() {
		return queryCount;
	}
	public void setQueryCount(String queryCount) {
		this.queryCount = queryCount;
	}
	public String getQueryMissCount() {
		return queryMissCount;
	}
	public void setQueryMissCount(String queryMissCount) {
		this.queryMissCount = queryMissCount;
	}
	public String getLoadCount() {
		return loadCount;
	}
	public void setLoadCount(String loadCount) {
		this.loadCount = loadCount;
	}
	public String getLoadExceptionCount() {
		return loadExceptionCount;
	}
	public void setLoadExceptionCount(String loadExceptionCount) {
		this.loadExceptionCount = loadExceptionCount;
	}
	public String getWriteCount() {
		return writeCount;
	}
	public void setWriteCount(String writeCount) {
		this.writeCount = writeCount;
	}
	public String getWriteExceptionCount() {
		return writeExceptionCount;
	}
	public void setWriteExceptionCount(String writeExceptionCount) {
		this.writeExceptionCount = writeExceptionCount;
	}

}
