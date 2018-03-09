package com.yw.bean;
/**
 * 消息会话流
 * @author Administrator
 *
 */
public class LastString {
	//openid标识用户
	private String userName;
	//关键节点留言
	private String lastString;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getLastString() {
		return lastString;
	}
	public void setLastString(String lastString) {
		this.lastString = lastString;
	}
	
}
