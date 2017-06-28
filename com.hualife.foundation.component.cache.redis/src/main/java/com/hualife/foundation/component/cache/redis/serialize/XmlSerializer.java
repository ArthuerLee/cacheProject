package com.hualife.foundation.component.cache.redis.serialize;

import com.eos.data.serialize.XMLSerializer;

public class XmlSerializer implements IDataSerialize {

	@Override
	public Object deserialize(String data) {
		if(data == null){
			return null;
		}
		if(!data.startsWith("<__root")){
			return data;
		}
		XMLSerializer serializer = new XMLSerializer();
		return serializer.unmarshall(data);
	}

	@Override
	public String serialize(Object obj) {
		if(obj == null){
			return null;
		}
		if(obj instanceof String){
			return String.valueOf(obj);
		}
		XMLSerializer serializer = new XMLSerializer();
		return serializer.marshallToString(obj);
	}

}
