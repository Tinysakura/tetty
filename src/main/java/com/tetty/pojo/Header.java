package com.tetty.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息头信息
 * @author Administrator
 *
 */
public class Header {
	private int crcCode = 0xabef0101;
	private int length;//长度
	private long sessionId;//会话id
	private byte priority;//优先级
	private byte type;//会话类型
	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	public int getCrcCode() {
		return crcCode;
	}
	public void setCrcCode(int crcCode) {
		this.crcCode = crcCode;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public long getSessionId() {
		return sessionId;
	}
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	public byte getPriority() {
		return priority;
	}
	public void setPriority(byte priority) {
		this.priority = priority;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public interface Type{
		public static final byte LOGIN_REQ = 1;
		public static final byte LOGIN_RESP = 2;
		public static final byte HEART_REQ = 3;
		public static final byte HEART_RESP	= 4;
		public static final byte ECHO_REQ = 5;
		public static final byte ECHO_RESP = 6;
	}
	
}
