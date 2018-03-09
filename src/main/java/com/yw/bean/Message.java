package com.yw.bean;

import java.util.Date;
/**
 * 信件(隔天收到)
 * @author Administrator
 *
 */
public class Message {
	private long id;
	//openid 标识用户
	private String fromUserName;
	//openid 标识用户
	private String toUserName;
	//信件内容
	private String details;
	//时间
	private Date timeOccur;
	//被浏览次数
	private int counts;
	
	private String anotherName;
	
	private String sex;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
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
