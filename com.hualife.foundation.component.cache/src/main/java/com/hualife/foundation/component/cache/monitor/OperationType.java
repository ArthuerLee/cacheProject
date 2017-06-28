package com.hualife.foundation.component.cache.monitor;

public enum OperationType {
      //查询
	  QUERY,
	  //查询未命中
	  QUERY_MISS,
	  //加载
	  LOAD,
	  //加载异常
	  LOAD_EXCEPTION,
	  //持久化
	  WRITE,
	  //持久化异常
	  WRITE_EXCEPTION,
	  //新增
	  ADD,
	  //删除
	  DELETE,
	  //替换
	  REPLACE
	  
}
