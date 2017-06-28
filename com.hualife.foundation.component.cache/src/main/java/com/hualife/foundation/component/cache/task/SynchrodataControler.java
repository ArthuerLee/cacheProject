package com.hualife.foundation.component.cache.task;

import com.hualife.foundation.component.common.Lifecycle;
//import com.primeton.btp.api.core.logger.ILogger;
//import com.primeton.btp.api.core.logger.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchrodataControler implements Lifecycle{
	private SynchrodataTask task;
	private Status status = Status.UNINITIALISED;
	//private ILogger logger = LoggerFactory.getLogger(SynchrodataControler.class);
	private Logger logger = LoggerFactory.getLogger(SynchrodataControler.class);
	
	public SynchrodataControler(SynchrodataTask task) {
		this.task = task;
	}
	
	public void stop() {
		status = Status.STOPPED;
	}
	
	public void start(){
		status = Status.STARTED;
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(status == Status.STARTED){
					try {
						if(task.ready())
							task.doSth();	
					} catch (Throwable e) {
						if(logger.isErrorEnabled()){
							logger.error("Execute task ["+task.getClass().getSimpleName()+"] error.",e);
						}
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public Status getStatus() {
		return status;
	}
}
