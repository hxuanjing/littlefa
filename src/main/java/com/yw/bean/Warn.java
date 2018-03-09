package com.yw.bean;

import java.util.Date;

public class Warn {
	private long id;
	private String userName;
	private String warnUserName;
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
	public String getWarnUserName() {
		return warnUserName;
	}
	public void setWarnUserName(String warnUserName) {
		this.warnUserName = warnUserName;
	}
	public Date getTimeOccur() {
		return timeOccur;
	}
	public void setTimeOccur(Date timeOccur) {
		this.timeOccur = timeOccur;
	}
}
