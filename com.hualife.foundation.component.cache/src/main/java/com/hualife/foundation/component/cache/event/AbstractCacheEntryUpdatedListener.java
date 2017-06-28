package com.hualife.foundation.component.cache.event;

import com.hualife.foundation.component.cache.Cache.Entry;

public abstract class AbstractCacheEntryUpdatedListener<K, V> implements
		CacheEntryUpdatedListener<K, V> {
	private boolean isSyn;
	
	@Override
	public boolean isSynchronous() {
		return isSyn;
	}

	@Override
	public void setSynchronous(boolean isSyn) {
		this.isSyn = isSyn;
	}

	@Override
	public void beforeUpdate(final Entry<K, V> entry) {
		if(isSyn) {
			doBeforeUpdate(entry);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doBeforeUpdate(entry);
				}
			});
			thread.setName("execute EntryUpdatedListener beforeUpdate thread");
			thread.start();
		}
	}

	public abstract void doBeforeUpdate(Entry<K, V> entry);

	@Override
	public void afterUpdate(final Entry<K, V> entry) {
		if(isSyn) {
			doAfterUpdate(entry);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doAfterUpdate(entry);
				}
			});
			thread.setName("execute EntryUpdatedListener afterUpdate thread");
			thread.start();
		}
	}

	public abstract void doAfterUpdate(Entry<K, V> entry);

}
