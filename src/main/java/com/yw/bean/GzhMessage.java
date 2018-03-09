package com.yw.bean;

import java.util.Date;

public class GzhMessage {
	private long id;
	private String userName;
	private String details;
	private Date timeOccur;
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
	public Date getTimeOccur() {
		return timeOccur;
	}
	public void setTimeOccur(Date timeOccur) {
		this.timeOccur = timeOccur;
	}
	
}
