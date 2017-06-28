package com.hualife.foundation.component.cache.event;

import java.util.Collection;

import com.hualife.foundation.component.cache.Cache.Entry;

public abstract class AbstractCacheEntryReadListener<K, V> implements
		CacheEntryReadListener<K, V> {
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
	public void beforeRead(final K key) {
		if(isSyn) {
			doBeforeRead(key);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doBeforeRead(key);
				}
			});
			thread.setName("execute EntryReadListener beforeRead thread");
			thread.start();
		}
	}

	public abstract void doBeforeRead(K key);

	@Override
	public void afterRead(final Entry<K, V> entry) {
		if(isSyn) {
			doAfterRead(entry);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doAfterRead(entry);
				}
			});
			thread.setName("execute EntryReadListener afterRead thread");
			thread.start();
		}
	}

	public abstract void doAfterRead(Entry<K, V> entry);

	@Override
	public void beforeReadAll(final Collection<? extends K> keys) {
		if(isSyn) {
			doBeforeReadAll(keys);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doBeforeReadAll(keys);
				}
			});
			thread.setName("execute EntryReadListener beforeReadAll thread");
			thread.start();
		}
	}

	public abstract void doBeforeReadAll(Collection<? extends K> keys);

	@Override
	public void afterReadAll(final Collection<? extends K> keys) {
		if(isSyn) {
			doAfterReadAll(keys);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doAfterReadAll(keys);
				}
			});
			thread.setName("execute EntryReadListener afterReadAll thread");
			thread.start();
		}
	}

	public abstract void doAfterReadAll(Collection<? extends K> keys);

}
