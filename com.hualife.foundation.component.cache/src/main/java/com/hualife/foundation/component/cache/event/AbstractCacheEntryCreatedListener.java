package com.hualife.foundation.component.cache.event;

import java.util.Map;

import com.hualife.foundation.component.cache.Cache.Entry;

public abstract class AbstractCacheEntryCreatedListener<K,V> implements
		CacheEntryCreatedListener<K, V> {
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
	public void beforeCreate(final Entry<K, V> entry) {
		if(isSyn) {
			doBeforeCreate(entry);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doBeforeCreate(entry);
				}
			});
			thread.setName("execute EntryCreatedListener beforeCreate thread");
			thread.start();
		}
	}

	public abstract void doBeforeCreate(Entry<K, V> entry);

	@Override
	public void afterCreate(final Entry<K, V> entry) {
		if(isSyn) {
			doAfterCreate(entry);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doAfterCreate(entry);
				}
			});
			thread.setName("execute EntryCreatedListener afterCreate thread");
			thread.start();
		}
	}

	public abstract void doAfterCreate(Entry<K, V> entry);

	@Override
	public void beforeCreateAll(final Map<K, V> datas) {
		if(isSyn) {
			doBeforeCreateAll(datas);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doBeforeCreateAll(datas);
				}
			});
			thread.setName("execute EntryCreatedListener beforeCreateAll thread");
			thread.start();
		}
	}

	public abstract void doBeforeCreateAll(Map<K, V> datas);

	@Override
	public void afterCreateAll(final Map<K, V> datas) {
		if(isSyn) {
			doAfterCreateAll(datas);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doAfterCreateAll(datas);
				}
			});
			thread.setName("execute EntryCreatedListener afterCreateAll thread");
			thread.start();
		}
	}

	public abstract void doAfterCreateAll(Map<K, V> datas);

}
