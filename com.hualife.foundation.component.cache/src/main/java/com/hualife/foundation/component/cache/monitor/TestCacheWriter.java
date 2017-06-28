package com.hualife.foundation.component.cache.monitor;

import com.hualife.foundation.component.cache.AbstractCacheWriter;
import com.hualife.foundation.component.cache.Cache.Entry;
import com.hualife.foundation.component.cache.CacheException;

public class TestCacheWriter extends AbstractCacheWriter{

	@Override
	public void write(Entry entry) throws CacheException {
		// TODO Auto-generated method stub
		System.out.println("key================================="+entry.getKey());
	    System.out.println("value==============================="+entry.getValue());
		entry.getValue();
		System.out.println("TestCacheWriter===========================================write");
	}

	@Override
	public void update(Entry entry) throws CacheException {
		
		
		
		// TODO Auto-generated method stub
		System.out.println("TestCacheWriter===========================================update");
	}

	@Override
	public void delete(Object key) throws CacheException {
		// TODO Auto-generated method stub
		System.out.println("TestCacheWriter==========================================delete");
	}

}
