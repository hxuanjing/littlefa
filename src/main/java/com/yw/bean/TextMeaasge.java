package com.yw.bean;

public class TextMeaasge extends BaseMessage{
	private String Content;
	private String MsgId;
	
	public TextMeaasge() {
		
	}
	
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	public String getMsgId() {
		return MsgId;
	}
	public void setMsgId(String msgId) {
		MsgId = msgId;
	}
}
