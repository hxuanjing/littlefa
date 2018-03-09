package com.yw.bean;

import java.util.Date;

/**
 * 月老的手账本
 * @author Administrator
 *
 */
public class Matchmaker {
	private long id;
	//openid 标识用户
	private String userName;
	//openid 标识用户
	private String toUserName;
	//交互次数
	private int interaction;
	//最新交互时间
	private Date timeOccur;
	//是否同意暴露微信
	private int agree;
	
	private String anotherName;
	private String wechatName;
	private String sex;
	private int age;
	private String province;
	
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
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public int getInteraction() {
		return interaction;
	}
	public void setInteraction(int interaction) {
		this.interaction = interaction;
	}
	public Date getTimeOccur() {
		return timeOccur;
	}
	public void setTimeOccur(Date timeOccur) {
		this.timeOccur = timeOccur;
	}
	public int getAgree() {
		return agree;
	}
	public void setAgree(int agree) {
		this.agree = agree;
	}
	public String getAnotherName() {
		return anotherName;
	}
	public void setAnotherName(String anotherName) {
		this.anotherName = anotherName;
	}
	public String getWechatName() {
		return wechatName;
	}
	public void setWechatName(String wechatName) {
		this.wechatName = wechatName;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	
}
