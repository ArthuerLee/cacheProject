package com.hualife.foundation.component.cache.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hualife.foundation.component.cache.Caching;
import com.hualife.foundation.component.cache.config.SystemCacheInfo;
import com.hualife.foundation.component.cache.config.SystemCacheInfo.StartingMode;
import com.hualife.foundation.component.cache.monitor.Monitor;

public class WebCacheStartUpListener implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		SystemCacheInfo.INSTANCE.setStartingMode(StartingMode.WEB);
//		EmbeddedConfig emConfig = new EmbeddedConfig();
//		emConfig.setServerHome(System.getProperty("EXTERNAL_CONFIG_DIR"));
//		EmbeddedSystemCache.getInstance().initOnlyOnce(false, emConfig);
		String configPath = WebCacheStartUpListener.class.getResource("/").getPath().split("WEB-INF")[0].concat("WEB-INF");
		Caching.start(configPath);
		Monitor.startMonitor("WebProject");
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		Caching.stop();
	}
}
	