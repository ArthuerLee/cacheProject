package com.hualife.foundation.component.cache.redis.serialize;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hualife.foundation.component.cache.config.model.SerializeType;

public class DataSerializeSupport {
	
	public static final String SERIALIZE_TYPE = "SerializeType";

	private static DataSerializeSupport INSTANCE = null;
	
	private IDataSerialize dataSerialize = null;
	
	private Map<SerializeType, IDataSerialize> cachedDataSerialize = new ConcurrentHashMap<SerializeType, IDataSerialize>();
	
	private DataSerializeSupport(){
		registerDataSerialize(SerializeType.XML, new XmlSerializer());
		registerDataSerialize(SerializeType.JSON, new JsonDataSerialize());
	}
	
	public static DataSerializeSupport getInstance(){
		if(INSTANCE != null){
			return INSTANCE;
		}
		
		synchronized (DataSerializeSupport.class) {
			if(INSTANCE != null){
				return INSTANCE;
			}
			INSTANCE = new DataSerializeSupport();
		}
		return INSTANCE;
	}
	
	public void registerDataSerialize(SerializeType type, IDataSerialize dataSerialize){
		if(type == null || dataSerialize == null){
			return;
		}
		cachedDataSerialize.put(type, dataSerialize);
	}
	
	/**
	 * 根据cacheProperty 决定序列化类型；
	 * @param config
	 * @return
	 */
	public IDataSerialize getDataSerialize(SerializeType type){
		if(dataSerialize != null){
			return dataSerialize;
		}
//		if(config == null){
		IDataSerialize serialize = cachedDataSerialize.get(type);
		if(null == serialize){
			dataSerialize = getDefaultDataSerialize();
			return dataSerialize;
		}
		return serialize;
//		}
//		String serializeType = config.getReadThroughConfig().getSerializeType().name();
//		IDataSerialize serialize = cachedDataSerialize.get(serializeType);
//		if(serialize == null){
//			return getDefaultDataSerialize();
//		}
//		dataSerialize = serialize;
//		return dataSerialize;
	}

	private IDataSerialize getDefaultDataSerialize() {
		return new XmlSerializer();
	}
	
}
