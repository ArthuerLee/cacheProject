package com.hualife.foundation.component.cache.redis;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hualife.foundation.component.cache.Cache;
import com.hualife.foundation.component.cache.CacheException;
import com.hualife.foundation.component.cache.CacheHelper;
import com.hualife.foundation.component.cache.Caching;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    static{
    	String pathString = "src\\test\\java\\com\\hualife\\foundation\\component\\cache\\redis";
    	String target = AppTest.class.getResource("/").getPath().split("target")[0];
		String configPath = target+pathString;
//		String cacheUrl = configPath + "\\cache.xml";
//    	String userConfigUrl = configPath + "\\user-config.xml";
//    	System.out.println(userConfigUrl);
//    	System.out.println(cacheUrl);
    	try {
			Caching.start(configPath);
		} catch (Throwable e) {
			e.printStackTrace();
		}
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	Company company = new Company();
    	company.setName("hualife");
    	Department department = new Department();
    	department.setName("tech");
    	department.setNumber(100);
    	Department department2 = new Department();
    	department2.setName("hr");
    	department2.setNumber(30);
    	List<Department> departments = new ArrayList<Department>();
    	departments.add(department);
    	departments.add(department2);
    	company.setDepartments(departments);
    	CacheHelper.put("test", "company", company);
    	try{
    		Company company2 = CacheHelper.get("test", "company");
    		Assert.assertTrue(company2.getName().equals("hualife"));
    		CacheHelper.remove("test", "company");
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
    	
//    	testKeySet();
    	
//    	testKeysSingleRedis();
//    	testGetAndPut();
//    	testRemoveKV();
//    	testReplaceKVV();
//    	testReplaceKV();
		testGetAndReplace();
//    	testGetAndRemove();
    	
    	testGetLocalIp();
    	
    	testAdd();
    	testGet();
    	testRemvoe();
    	testPutAll();
    	testGetAll();
    	testContainsKey();
    }

	private void testKeySet() {
		CacheHelper.put("test2", "861101", "a");
    	CacheHelper.put("test2", "861102", "b");
    	CacheHelper.put("test2", "861103", "c");
    	CacheHelper.put("test2", "861201", "d");
    	CacheHelper.put("test2", "861202", "e");
    	try{
    		Set<Object> set = CacheHelper.keys("test2", "8611*");
    		Assert.assertTrue(set.size() == 3);
    		CacheHelper.remove("test2", "861101");
    		CacheHelper.remove("test2", "861102");
    		CacheHelper.remove("test2", "861103");
    		CacheHelper.remove("test2", "861201");
    		CacheHelper.remove("test2", "861202");
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
	}

	private void testKeysSingleRedis() {
		CacheHelper.put("test", "861101", "a");
    	CacheHelper.put("test", "861102", "b");
    	CacheHelper.put("test", "861103", "c");
    	CacheHelper.put("test", "861201", "d");
    	CacheHelper.put("test", "861202", "e");
    	try{
    		Set<Object> set = CacheHelper.keys("test", "8611*");
    		Assert.assertTrue(set.size() == 3);
    		CacheHelper.remove("test", "861101");
    		CacheHelper.remove("test", "861102");
    		CacheHelper.remove("test", "861103");
    		CacheHelper.remove("test", "861201");
    		CacheHelper.remove("test", "861202");
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
	}

	private void testGetAndPut() {
		CacheHelper.put("test", "11", "aa");
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				CacheHelper.getAndPut("test", "11","aa");
			}
		}).start();
    	try{
    		CacheHelper.getAndPut("test", "11","aa");
    		CacheHelper.remove("test", "11");
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
	}

	private void testRemoveKV() {
		CacheHelper.put("test", "11", "aa");
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				CacheHelper.remove("test", "11","aa");
			}
		}).start();
    	try{
    		boolean flag = CacheHelper.remove("test", "11","aa");
    		CacheHelper.remove("test", "11");
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
	}

	private void testReplaceKVV() {
		CacheHelper.put("test", "11", "aa");
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				CacheHelper.replace("test", "11","aa", "bb");
			}
		}).start();
    	try{
    		boolean flag = CacheHelper.replace("test", "11","aa","cc");
    		CacheHelper.remove("test", "11");
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
	}

	private void testReplaceKV() {
		CacheHelper.put("test", "11", "aa");
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				CacheHelper.replace("test", "11", "bb");
			}
		}).start();
    	try{
    		boolean flag = CacheHelper.replace("test", "11","cc");
    		CacheHelper.remove("test", "11");
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
	}

	private void testGetAndReplace() {
		CacheHelper.put("test", "11", "aa");
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				CacheHelper.replace("test", "11", "bb");
			}
		}).start();
    	try{
    		String oldString = CacheHelper.getAndReplace("test", "11","cc");
    		CacheHelper.remove("test", "11");
    		System.out.println(oldString);
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
	}

	private void testGetAndRemove() {
		CacheHelper.put("test", "11", "aa");
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				CacheHelper.replace("test", "11", "bb");
			}
		}).start();
    	try{
    		
    		String oldString = CacheHelper.getAndRemove("test", "11");
    		CacheHelper.remove("test", "11");
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
	}
    
	private void testGetLocalIp() {
		try {
			String nodeId = InetAddress.getLocalHost().getHostAddress();
			System.out.println(nodeId);
		} catch (UnknownHostException e) {
			throw new CacheException("Can not get local address.",e);
		}
	}

	public void testContainsKey() {
		Cache<String,String> cache = Caching.getCacheManager("test").getCache("test");
    	cache.put("test_key", "test_value");
        assertTrue(cache.containsKey("test_key"));
        cache.remove("test_key");
	}

	public void testPutAll() {
		Cache<String, String> cache = Caching.getCacheManager("test").getCache("test");
		Map<String, String> map = new HashMap<String, String>();
		map.put("test_key_1", "test_value_1");
		map.put("test_key_2", "test_value_2");
		map.put("test_key_3", "test_value_3");
		cache.putAll(map);
		ArrayList<String> keys = new ArrayList<String>();
		keys.add("test_key_1");
		keys.add("test_key_2");
		keys.add("test_key_3");
		Map<String, String> result = cache.getAll(keys);
		assertEquals(result.size(), 3);
		cache.removeAll(keys);
	}

	public void testGetAll() {
		Cache<String, String> cache = Caching.getCacheManager("test").getCache("test");
		Map<String, String> map = new HashMap<String, String>();
		map.put("test_key_1", "test_value_1");
		map.put("test_key_2", "test_value_2");
		map.put("test_key_3", "test_value_3");
		cache.putAll(map);
		ArrayList<String> keys = new ArrayList<String>();
		keys.add("test_key_1");
		keys.add("test_key_2");
		keys.add("test_key_3");
		Map<String, String> result = cache.getAll(keys);
		assertEquals(result.size(), 3);
		cache.removeAll(keys);
	}

	public void testRemvoe() {
		Cache<String,String> cache = Caching.getCacheManager("test").getCache("test");
    	cache.put("test_key", "test_value");
    	Object object = cache.get("test_key");
		String string = object.toString();
		assertEquals("test_value", string);
		cache.remove("test_key");
		object = cache.get("test_key");
		assertTrue(object == null);
	}

	public void testGet() {
		Cache<String,String> cache = Caching.getCacheManager("test").getCache("test");
    	cache.put("test_key", "test_value");
    	Object object = cache.get("test_key");
		String string = object.toString();
		assertEquals("test_value", string);
		cache.remove("test_key");
	}

	public void testAdd() {
		
		Cache<String,String> cache = Caching.getCacheManager("test").getCache("test");
    	cache.put("test_key", "test_value");
    	Object object = cache.get("test_key");
		String string = object.toString();
    	assertEquals("test_value", string);
    	cache.remove("test_key");
	}
}
