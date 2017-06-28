package com.hualife.foundation.component.cache.monitor.task;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hualife.foundation.component.cache.monitor.ClassUtil;
import com.hualife.foundation.component.cache.monitor.Indices;
import com.hualife.foundation.component.cache.monitor.Monitor;

public class CacheFrameConfigRunnable implements Runnable{
	Logger monitor1Logger = LoggerFactory.getLogger("monitor1");
	public void run(){
	 
	 try {
		List<Indices> indcLst = Monitor.getMonitorCache().get("configLst");
		for(Indices indc:indcLst){
		    indc.setCollectTime(Calendar.getInstance().getTimeInMillis());
			monitor1Logger.info(ClassUtil.getLogContentByBean(indc));
		}
	} catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
	}
	
	
}
