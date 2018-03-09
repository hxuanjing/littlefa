package com.yw.bean;

import java.util.Date;
/**
 * 用户基本信息
 * @author VanGogh
 *
 */
public class UserInfo {
	private long id;
	//openid 28位随机唯一字符串
	private String userName;
	//别名
	private String anotherName;
	//微信号
	private String wechatName;
	//性别
	private String sex;
	//年龄
	private int age;
	//身份
	private String province;
	//被警告次数(举报) 三次以上触发警告 六次黑名单
	private int warn;
	//注册时间
	private Date registertime;
	//取消关注时间
	private Date deletetime;
	//是否警告
	private int iswarn;
	//是否取消关注(取消关注十天后 跑批删除相关账户记录)
	private int isdelete;
	
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
	public int getWarn() {
		return warn;
	}
	public void setWarn(int warn) {
		this.warn = warn;
	}
	public Date getRegistertime() {
		return registertime;
	}
	public void setRegistertime(Date registertime) {
		this.registertime = registertime;
	}
	public Date getDeletetime() {
		return deletetime;
	}
	public void setDeletetime(Date deletetime) {
		this.deletetime = deletetime;
	}
	public int getIswarn() {
		return iswarn;
	}
	public void setIswarn(int iswarn) {
		this.iswarn = iswarn;
	}
	public int getIsdelete() {
		return isdelete;
	}
	public void setIsdelete(int isdelete) {
		this.isdelete = isdelete;
	}
	
}
