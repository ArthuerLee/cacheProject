package com.hualife.foundation.component.cache.monitor.task;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hualife.foundation.component.cache.monitor.ClassUtil;
import com.hualife.foundation.component.cache.monitor.Indices;
import com.hualife.foundation.component.cache.monitor.Monitor;
import com.hualife.foundation.component.cache.monitor.OperationType;
import com.hualife.foundation.component.cache.monitor.Statistics;

public class CacheFrameStatisticsRunnable implements Runnable{
	Logger monitor2Logger = LoggerFactory.getLogger("monitor2");
	private final static String CONTACTOR ="@";
	private final static String COLON =":";
	public void run(){
 
	 try {
		List<Indices> indcLst = Monitor.getMonitorCache().get("configLst");
		for(Indices idc:indcLst){
	    Statistics st = new Statistics();
		Object queryCount= Monitor.getCache().getIfPresent(idc.getCacheName().split(COLON)[1]+CONTACTOR+OperationType.QUERY.name());
		if(queryCount!=null){
			st.setQueryCount("queryCount:"+queryCount);
		}else{
			st.setQueryCount("queryCount:0");
		}
		Object queryMissCount = Monitor.getCache().getIfPresent(idc.getCacheName().split(COLON)[1]+CONTACTOR+OperationType.QUERY_MISS.name());
		if(queryMissCount!=null){
			st.setQueryMissCount("queryMissCount:"+queryMissCount);
		}else{
			st.setQueryMissCount("queryMissCount:0");
		}
		Object loadCount = Monitor.getCache().getIfPresent(idc.getCacheName().split(COLON)[1]+CONTACTOR+OperationType.LOAD.name());
		if(loadCount!=null){
			st.setLoadCount("loadCount:"+loadCount);
		}else{
			st.setLoadCount("loadCount:0");
		}
		Object loadExceptionCount = Monitor.getCache().getIfPresent(idc.getCacheName().split(COLON)[1]+CONTACTOR+OperationType.LOAD_EXCEPTION.name());
		if(loadExceptionCount!=null){
			st.setLoadExceptionCount("loadExceptionCount:"+loadExceptionCount);
		}else{
			st.setLoadExceptionCount("loadExceptionCount:0");
		}
		Object writeCount = Monitor.getCache().getIfPresent(idc.getCacheName().split(COLON)[1]+CONTACTOR+OperationType.WRITE.name());
		if(writeCount!=null){
			st.setWriteCount("writeCount:"+writeCount);
		}else{
			st.setWriteCount("writeCount:0");
		}
		
		Object writeExceptionCount = Monitor.getCache().getIfPresent(idc.getCacheName().split(COLON)[1]+CONTACTOR+OperationType.WRITE_EXCEPTION.name());
		if(writeExceptionCount!=null){
			st.setWriteExceptionCount("writeExceptionCount:"+writeExceptionCount);
		}else{
			st.setWriteExceptionCount("writeExceptionCount:0");
		}	
        st.setAppName(idc.getAppName());
        st.setCacheName(idc.getCacheName());
        st.setCacheType(idc.getCacheType());
        st.setCacheAddress(idc.getCacheAddress());
        st.setCollectTime(Calendar.getInstance().getTimeInMillis());
        
        monitor2Logger.info(ClassUtil.getLogContentByBean(st));
		}
		
	} catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
}
