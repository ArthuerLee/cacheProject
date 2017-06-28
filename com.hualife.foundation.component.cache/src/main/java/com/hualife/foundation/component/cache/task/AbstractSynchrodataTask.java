package com.hualife.foundation.component.cache.task;

public abstract class AbstractSynchrodataTask implements SynchrodataTask{
	long nextTime;
	public int period;

	public AbstractSynchrodataTask(int period) {
		this.period = period;
	}

	@Override
	public boolean ready() {
		long currentTime = System.currentTimeMillis();
		
		boolean rtn = false;
		if(currentTime > nextTime){
			rtn = true;
			nextTime = currentTime + period * 1000;
		}
		return rtn;
		// 影响检测主节点是否宕机的时间
		// 每秒检测一次
//		try {
//			Thread.sleep(period * 1000);
//		} catch (InterruptedException e) {
//			// ignore
//		}
//		return true;
	}

	@Override
	public int getPeriod() {
		return this.period;
	}
	
	public void setPeriod(int period) {
		this.period = period;
	}	
	
	@Override
	public void setNextTime(long time) {
		this.nextTime = time;
	}
}
