package com.hualife.foundation.component.cache.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.IpUtil;

public class SystemCacheInfo {
	public static SystemCacheInfo INSTANCE = new SystemCacheInfo();
	private SystemCacheInfo(){
	
	}
	private String nodeId;
	private String appName;
	private StartingMode startingMode;
	
	
	public StartingMode getStartingMode() {
		return startingMode;
	}

	public void setStartingMode(StartingMode startingMode) {
		this.startingMode = startingMode;
	}

	public String getAppName(){
		return INSTANCE.appName;
	}
	
	public void setAppName(String appName) {
		INSTANCE.appName = appName;
	}

	public String getNodeId() {
		try {
			if(nodeId == null){
				nodeId = IpUtil.getLocalHost().getHostAddress().concat("_").concat(appName);
			}
		} catch (Exception e) {
			throw new CacheException("Can not get local address.",e);
		}
		return nodeId;
	}
	
	public enum StartingMode {
		BIIP,
		WEB
	}
}
