package com.hualife.foundation.component.cache.task;


import com.hualife.foundation.component.cache.RemoteCache;

public class CheckPreviligeTask<K, V> implements SynchrodataTask{
	private SynchrodataTask component;
	private RemoteCache<K, V> cache;
	
	public CheckPreviligeTask(RemoteCache<K, V>remoteCache, SynchrodataTask task) {
		this.component = task;
		this.cache = remoteCache;
	}
	
	@Override
	public void doSth() {
		if(cache.checkSynPrevilige()){
			this.component.doSth();
		}
	}

	@Override
	public boolean ready() {
		if(!cache.checkSynPrevilige()){
			if(cache.checkSwitchSynCondition()){
				if(cache.grabSynPrevilige()){
					setNextTime(System.currentTimeMillis() + getPeriod() * 1000);
					return true;
				}
			}
			return false;
		}
		cache.refreshSynStatus();
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
