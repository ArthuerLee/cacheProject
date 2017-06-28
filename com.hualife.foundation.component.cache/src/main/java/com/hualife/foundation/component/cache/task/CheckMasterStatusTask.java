package com.hualife.foundation.component.cache.task;

import com.hualife.foundation.component.cache.RemoteCache;

public class CheckMasterStatusTask<K, V> implements SynchrodataTask{
	private SynchrodataTask component;
	private RemoteCache<K, V> cache;
	
	public CheckMasterStatusTask(RemoteCache<K, V>remoteCache, SynchrodataTask task) {
		this.component = task;
		this.cache = remoteCache;
	}
	
	@Override
	public void doSth() {
		if(cache.isMaster()){
			this.component.doSth();
		}
	}

	@Override
	public boolean ready() {
		return component.ready();
	}

	@Override
	public void setNextTime(long time) {
		component.setNextTime(time);
	}

	@Override
	public int getPeriod() {
		return component.getPeriod();
	}

}
