package com.hualife.foundation.component.cache.monitor;

import java.util.Map;

import com.hualife.foundation.component.cache.AbstractCacheLoader;

public class TestCacheLoader extends AbstractCacheLoader{

	@Override
	public Map preLoad() {
		// TODO Auto-generated method stub
	  System.out.println("TestCacheLoader================================preLoad");
		return null;
	}

	@Override
	public Map loadAll() {
		// TODO Auto-generated method stub
		System.out.println("TestCacheLoader===============================loadAll");
		return null;
	}

}
