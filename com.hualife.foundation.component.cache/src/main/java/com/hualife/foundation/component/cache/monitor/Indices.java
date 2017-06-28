package com.hualife.foundation.component.cache.monitor;

import java.util.Map;

public class Indices  {
	
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
	public String cacheSize;
	//缓存失效时长
	public String cacheDuration;
	//缓存失效策略
	public String invalidPolicy;
	//主节点
	public String mainNode;
	//是否开启统计
	public String isStatisticsEnabled;
	//是否开启事物
	public String isTransactionEnabled;
	//监听类型和是否同步
	public Map<String,Boolean> listenerType;
	//加载频率
	public String loadFrequency;
	//持久化频率
	public String writeFrequency;
	//更新多少次后进行持久化
	public String bufferSize;
	//查询缓存的次数
	public String queryCount;
	//缓存命中次数
	public String hitCount;
	//缓存加载次数
	public String loadCount;
	//缓存加载日常次数
	public String loadExceptionCount;
	//缓存持久化次数
	public String writeCount;
	//缓存持久化异常次数
	public String writeExceptionCount;
	//操作类型
	public String oprType; 
    //操作时间
	public String oprTime;
	//操作节点IP
	public String oprNodeIp;
	
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

	public String getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(String cacheSize) {
		this.cacheSize = cacheSize;
	}


	public String getCacheDuration() {
		return cacheDuration;
	}

	public void setCacheDuration(String cacheDuration) {
		this.cacheDuration = cacheDuration;
	}

	public String getInvalidPolicy() {
		return invalidPolicy;
	}

	public void setInvalidPolicy(String invalidPolicy) {
		this.invalidPolicy = invalidPolicy;
	}

	public String getMainNode() {
		return mainNode;
	}

	public void setMainNode(String mainNode) {
		this.mainNode = mainNode;
	}

	
 
	public String getIsStatisticsEnabled() {
		return isStatisticsEnabled;
	}

	public void setIsStatisticsEnabled(String isStatisticsEnabled) {
		this.isStatisticsEnabled = isStatisticsEnabled;
	}

	public String getIsTransactionEnabled() {
		return isTransactionEnabled;
	}

	public void setIsTransactionEnabled(String isTransactionEnabled) {
		this.isTransactionEnabled = isTransactionEnabled;
	}

	public Map<String, Boolean> getListenerType() {
		return listenerType;
	}

	public void setListenerType(Map<String, Boolean> listenerType) {
		this.listenerType = listenerType;
	}

	public String getLoadFrequency() {
		return loadFrequency;
	}

	public void setLoadFrequency(String loadFrequency) {
		this.loadFrequency = loadFrequency;
	}

	public String getWriteFrequency() {
		return writeFrequency;
	}

	public void setWriteFrequency(String writeFrequency) {
		this.writeFrequency = writeFrequency;
	}

	public String getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(String bufferSize) {
		this.bufferSize = bufferSize;
	}

	public String getQueryCount() {
		return queryCount;
	}

	public void setQueryCount(String queryCount) {
		this.queryCount = queryCount;
	}

	public String getHitCount() {
		return hitCount;
	}

	public void setHitCount(String hitCount) {
		this.hitCount = hitCount;
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

	public String getOprNodeIp() {
		return oprNodeIp;
	}

	public void setOprNodeIp(String oprNodeIp) {
		this.oprNodeIp = oprNodeIp;
	}
	
	
}
