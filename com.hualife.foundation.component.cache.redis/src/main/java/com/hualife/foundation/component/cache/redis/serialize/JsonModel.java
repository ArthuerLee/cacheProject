package com.hualife.foundation.component.cache.redis.serialize;

public class JsonModel<V> {
	public final static String CLAZZ_KEY = "clazz";
	public final static String DATA_KEY = "data";
	
	private Class<V> clazz = null;
	private V data = null;
	
	public JsonModel() {
		super();
	}
	public JsonModel(Class<V> clazz, V data) {
		super();
		this.clazz = clazz;
		this.data = data;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<V> clazz) {
		this.clazz = clazz;
	}
	public Object getData() {
		return data;
	}
	public void setData(V data) {
		this.data = data;
	}
}
