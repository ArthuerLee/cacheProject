package com.hualife.foundation.component.cache.event;

import com.hualife.foundation.component.cache.Cache.Entry;

public abstract class AbstractCacheEntryExpiredListener<K, V> implements
		CacheEntryExpiredListener<K, V> {
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
	public void beforeExpire(final Entry<K, V> entry) {
		if(isSyn) {
			doBeforeExpire(entry);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doBeforeExpire(entry);
				}
			});
			thread.setName("execute EntryExpiredListener beforeExpire thread");
			thread.start();
		}
	}

	public abstract void doBeforeExpire(Entry<K, V> entry);

	@Override
	public void afterExpire(final Entry<K, V> entry) {
		if(isSyn) {
			doAfterExpire(entry);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doAfterExpire(entry);
				}
			});
			thread.setName("execute EntryExpiredListener afterExpire thread");
			thread.start();
		}
	}

	public abstract void doAfterExpire(Entry<K, V> entry);

}
