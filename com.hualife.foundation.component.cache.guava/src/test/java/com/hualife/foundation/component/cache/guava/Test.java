package com.hualife.foundation.component.cache.guava;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class Test {
	public static Set keySet(String pattern) {
		List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");  
		Collection<String> result = Collections2.filter(names,   
			      Predicates.containsPattern(pattern));  
		
		
		return null;
	}
	
	public static void main(String[] args) {
		
//		String str="ceponline@yahoo.com.cn";
//		Pattern pattern = Pattern.compile("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+",Pattern.CASE_INSENSITIVE);
//		Matcher matcher = pattern.matcher(str);
//		System.out.println(matcher.matches());
		
		Pattern pattern = Pattern.compile("^J.*");
		Matcher matcher = pattern.matcher("John");
		boolean b= matcher.matches();
		System.out.println(b);
		
//		keySet("J");
	}
}
