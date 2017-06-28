package com.hualife.foundation.component.cache.event;

import java.util.Collection;

import com.hualife.foundation.component.cache.Cache.Entry;

public abstract class AbstractCacheEntryRemovedListener<K, V> implements
		CacheEntryRemovedListener<K, V> {
	public boolean isSyn;
	
	@Override
	public boolean isSynchronous() {
		return isSyn;
	}

	@Override
	public void setSynchronous(boolean isSyn) {
		this.isSyn = isSyn;
	}

	@Override
	public void beforeRemove(final K key) {
		if(isSyn) {
			doBeforeRemove(key);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doBeforeRemove(key);
				}
			});
			thread.setName("execute EntryRemovedListener beforeRemove thread");
			thread.start();
		}
	}

	public abstract void doBeforeRemove(K key);

	@Override
	public void afterRemoved(final Entry<K, V> entry) {
		if(isSyn) {
			doAfterRemoved(entry);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doAfterRemoved(entry);
				}
			});
			thread.setName("execute EntryRemovedListener afterRemoved thread");
			thread.start();
		}
	}

	public abstract void doAfterRemoved(Entry<K, V> entry);

	@Override
	public void beforeRemoveAll(final Collection<? extends K> keys) {
		if(isSyn) {
			doBeforeRemoveAll(keys);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doBeforeRemoveAll(keys);
				}
			});
			thread.setName("execute EntryRemovedListener beforeRemoveAll thread");
			thread.start();
		}
	}

	public abstract void doBeforeRemoveAll(Collection<? extends K> keys);

	@Override
	public void afterRemoveAll(final Collection<? extends K> keys) {
		if(isSyn) {
			doAfterRemoveAll(keys);
		}else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					doAfterRemoveAll(keys);
				}
			});
			thread.setName("execute EntryRemovedListener afterRemoveAll thread");
			thread.start();
		}
	}

	public abstract void doAfterRemoveAll(Collection<? extends K> keys);

}
