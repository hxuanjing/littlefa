package com.yw.bean;

import java.util.Date;
/**
 * 烦恼
 * @author Administrator
 *
 */
public class Trouble {
	private long id;
	//openid 发布烦恼的用户标识
	private String userName;
	//烦恼
	private String details;
	//回复
	private String allayDetails;
	//发布时间
	private Date timeOccur;
	//被解答次数(同一个烦恼会被至少二个用户解答)
	private int counts;
	
	private String anotherName;
	
	private String sex;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getAllayDetails() {
		return allayDetails;
	}
	public void setAllayDetails(String allayDetails) {
		this.allayDetails = allayDetails;
	}
	public Date getTimeOccur() {
		return timeOccur;
	}
	public void setTimeOccur(Date timeOccur) {
		this.timeOccur = timeOccur;
	}
	public int getCounts() {
		return counts;
	}
	public void setCounts(int counts) {
		this.counts = counts;
	}
	public String getAnotherName() {
		return anotherName;
	}
	public void setAnotherName(String anotherName) {
		this.anotherName = anotherName;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	
}
