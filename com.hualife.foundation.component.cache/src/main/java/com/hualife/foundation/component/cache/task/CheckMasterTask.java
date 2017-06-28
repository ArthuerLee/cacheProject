package com.hualife.foundation.component.cache.task;

import com.eos.system.utility.StringUtil;
import com.hualife.foundation.component.cache.RemoteCache;
import com.hualife.foundation.component.cache.config.ConfigurationFactory;
import com.hualife.foundation.component.cache.config.SystemCacheInfo;
//import com.primeton.btp.api.core.logger.ILogger;
//import com.primeton.btp.api.core.logger.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CheckMasterTask<K, V> extends AbstractSynchrodataTask {
	private RemoteCache<K, V> cache;
	/*private ILogger logger = LoggerFactory.getLogger(CheckMasterTask.class);*/
	private Logger logger = LoggerFactory.getLogger(CheckMasterTask.class);
	
		
	public CheckMasterTask(RemoteCache<K, V> cache,int period) {
		super(period);
		String per = System.getProperty("SwitchMasterPeriod");
		int p = 2*60;
		if(StringUtil.isNotNullAndBlank(per)){
			p = Integer.valueOf(per);
		}
		setPeriod(p);
		this.cache = cache;
	}
	
	@Override
	public void doSth() {
		if(ConfigurationFactory.SYSTEM_CACHE_NAME.equals(cache.getName())){
			return;
		}
		logger.debug(cache.getName() + " :start check master.");
		if(cache.isMaster()){
			if(refreshSynStatus()){
				logger.debug(cache.getName() + " : is master and refresh status success.");
			}else{
				cache.setMaster(false);
				logger.error(cache.getName() + " : is master and refresh status failed.");
			}
			return;
		}else{
			if(checkSwitchSynCondition()){
				logger.debug(cache.getName() + " : try to change master.");
				if(grabSynPrevilige()){
					cache.setMaster(true);
					logger.debug(cache.getName() + " : change to master success.");
				}else{
					logger.debug(cache.getName() + " : change to master failed.");
				}
			}else{
				logger.debug(cache.getName() + " :is not master.");
			}
		}
		logger.debug(cache.getName() + " :end check master.");
	}
	
	private int getFewCycle(){
		String timeStr = System.getProperty("SwitchMasterFewCycle");
		int time = 2;//默认是2个周期
		if(StringUtil.isNotNullAndBlank(timeStr)){
			time = Integer.valueOf(timeStr);
		}
		return time;
	}
	
	private boolean refreshSynStatus() {
		String key = SystemCacheInfo.INSTANCE.getAppName()+"_"+cache.getName();
		String value = SystemCacheInfo.INSTANCE.getNodeId();
		RemoteCache<String, String> systemCache = cache.getSystemCache();
		boolean expire = systemCache.expiry(key, getPeriod()*getFewCycle());
		if(!expire){
			String string = systemCache.get(key);
			if(StringUtil.isNullOrBlank(string)){ 
				return false;
			}
			if(string.equals(value)){ 
				return true;
			}else{
				return false;
			}
		}
		return true;
	}

	private boolean grabSynPrevilige() {
		String key = SystemCacheInfo.INSTANCE.getAppName()+"_"+cache.getName();
		String value = SystemCacheInfo.INSTANCE.getNodeId();
		RemoteCache<String, String> systemCache = cache.getSystemCache();
		if(systemCache.putIfAbsent(key, value)){
			systemCache.expiry(key, getPeriod()*getFewCycle());
//			cache.setMaster(true);
			return true;
		}else{
//			cache.setMaster(false);
			String cacheValue = systemCache.get(key);
			if((null != cacheValue) && (cacheValue.equals(value))){
				systemCache.expiry(key, getPeriod()*getFewCycle());
				return true;
			}
			return false;
		}
	}

	private boolean checkSwitchSynCondition() {
		String key = SystemCacheInfo.INSTANCE.getAppName()+"_"+cache.getName();
		RemoteCache<String, String> systemCache = cache.getSystemCache();
		String value = systemCache.get(key);		
		if((null == value) || (SystemCacheInfo.INSTANCE.getNodeId().equals(value))){
			return true;
		}
		return false;
	}
}
