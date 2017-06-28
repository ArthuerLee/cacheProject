package com.hualife.foundation.component.cache.redis.serialize;

/**
 * 序列化接口
 */
public interface IDataSerialize {
	
	Object deserialize(String data);
	
	String serialize(Object obj);

}
