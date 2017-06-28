package com.hualife.foundation.component.cache.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class MonitorUtil {

	private final static Properties prop =  new  Properties();
	
	public static Properties getProperties(String filePath){

        InputStream in = MonitorUtil.class.getClassLoader().getResourceAsStream(filePath); 
       
        try  {    
           prop.load(in);    
       }  catch  (IOException e) {    
           e.printStackTrace();    
       } 
		return prop;
	}
	
	public static String getLocalAddress(){
		
		String address = "127.0.0.1";
		
		try {
			address = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   return address;   
	}
	public static String getPropertiesAbsolutePath(){
		
		return  System.getProperty("properties.path");
	}
}
