package com.hualife.foundation.component.cache.monitor.task;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hualife.foundation.component.cache.monitor.ClassUtil;
import com.hualife.foundation.component.cache.monitor.Monitor;
import com.hualife.foundation.component.cache.monitor.Track;

public class CacheFrameTrackRunnable implements Runnable{
	Logger monitor3Logger = LoggerFactory.getLogger("monitor3");
 
	public void run(){
	 
/*	 @SuppressWarnings("unchecked")
	  List<Track> indcLst =(List<Track>) Monitor.getCache().getIfPresent("trackLst");
	  long time = Calendar.getInstance().getTimeInMillis();
	  if(indcLst!=null && indcLst.size() > 0){
			for(Track t :indcLst){
				t.setCollectTime(time);
				monitor3Logger.info(ClassUtil.getLogContentByBean(t));
		   	}
		
	    }*/
	}
	
}
