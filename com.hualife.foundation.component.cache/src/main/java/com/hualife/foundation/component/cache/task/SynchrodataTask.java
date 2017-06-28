package com.hualife.foundation.component.cache.task;


public interface SynchrodataTask {
	void setNextTime(long time);
	int getPeriod();
	boolean ready();
	void doSth();
}
