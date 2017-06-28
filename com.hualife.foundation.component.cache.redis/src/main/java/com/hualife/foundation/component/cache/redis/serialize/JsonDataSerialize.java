package com.hualife.foundation.component.cache.redis.serialize;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.hualife.foundation.component.cache.CacheException;


/**
 * 实现需要优化，基本数据类型不能带类型走，否则数据量太大，影响网络传输速度
 */
public class JsonDataSerialize implements IDataSerialize {

	private JsonGenerator jsonGenerator = null;
	private ObjectMapper objectMapper = null;
	
	private StringWriter writer = new StringWriter();

	public JsonDataSerialize(){
		objectMapper = new ObjectMapper();
		try {
			jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(writer);
		} catch (IOException e) {
			throw new CacheException(e);
		}
	}
	
	@Override
	public Object deserialize(String data) {
		if(data == null){
			return null;
		}
		if(!data.startsWith("{\"clazz\":")){
			return data;
		}
		try {
			JsonNode node = objectMapper.readTree(data);
			String clazzStr = node.get(JsonModel.CLAZZ_KEY).asText();
			Class<?> clazz = Class.forName(clazzStr);//TOTO need try multi ways 
			return objectMapper.readValue(node.get(JsonModel.DATA_KEY), clazz);
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String serialize(Object obj) {
		if(obj == null){
			return null;
		}
		if(obj instanceof String){
			return String.valueOf(obj);
		}
		clearWriter();
		try {
			jsonGenerator.writeObject(new JsonModel(obj.getClass(), obj));
		} catch (Throwable e) {
			throw new CacheException(e);
		}
		writer.flush();
		return writer.toString();
	}
	
	private void clearWriter() {
		StringBuffer sb = writer.getBuffer();
		sb.delete(0, sb.length());
	}

}
