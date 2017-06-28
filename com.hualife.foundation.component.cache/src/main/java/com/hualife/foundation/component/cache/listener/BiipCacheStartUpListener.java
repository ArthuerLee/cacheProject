package com.hualife.foundation.component.cache.listener;

import com.eos.runtime.core.ApplicationContext;
import com.eos.runtime.core.IRuntimeListener;
import com.eos.runtime.core.RuntimeEvent;
import com.hualife.foundation.component.cache.Caching;
import com.hualife.foundation.component.cache.config.SystemCacheInfo;
import com.hualife.foundation.component.cache.config.SystemCacheInfo.StartingMode;
import com.hualife.foundation.component.cache.monitor.Monitor;

//public class BiipCacheStartUpListener implements IContributionListener{
public class BiipCacheStartUpListener implements IRuntimeListener{

	@Override
	public void start(RuntimeEvent arg0) {
		SystemCacheInfo.INSTANCE.setStartingMode(StartingMode.BIIP);
		String configPath = ApplicationContext.getInstance().getApplicationConfigPath();
		Caching.start(configPath);
		Monitor.saveConfigInfoInLocalGuava();
		Monitor.startMonitor();
	}

	@Override 
	public void stop(RuntimeEvent arg0) {
		Caching.stop();
	}

//	@Override
//	public void load(IContributionEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	} 
//
//	@Override
//	public void loadFinished(IContributionEvent arg0) {
//		// TODO Auto-generated method stub
//		SystemCacheInfo.INSTANCE.setStartingMode(StartingMode.BIIP);
//		String configPath = ApplicationContext.getInstance().getApplicationConfigPath();
//		Caching.start(configPath);
//	}
//
//	@Override
//	public void unLoad(IContributionEvent arg0) {
//		// TODO Auto-generated method stub
//		Caching.stop();
//	}

}
