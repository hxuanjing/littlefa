package com.yw.bean;

import java.util.Date;
/**
 * 解忧
 * @author Administrator
 *
 */
public class TroubleSolve {
	private long id;
	//对应于trouble的id
	private long tid;
	//openid 标识用户
	private String userName;
	//烦恼的回复
	private String details;
	//解答时间
	private Date timeOccur;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTid() {
		return tid;
	}
	public void setTid(long tid) {
		this.tid = tid;
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
	public Date getTimeOccur() {
		return timeOccur;
	}
	public void setTimeOccur(Date timeOccur) {
		this.timeOccur = timeOccur;
	}
}
