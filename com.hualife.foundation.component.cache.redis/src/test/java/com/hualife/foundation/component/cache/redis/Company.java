package com.hualife.foundation.component.cache.redis;

import java.io.Serializable;
import java.util.List;

public class Company implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 205462468663474279L;
	private String name;
	private List<Department> departments;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Department> getDepartments() {
		return departments;
	}
	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}
}
