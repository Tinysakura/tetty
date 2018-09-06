package com.tetty.pojo;

/**
 * 自定义的消息
 * @author Administrator
 *
 */
public class TettyMessage{
	private Header header;
	private Object body;
	
	public Header getHeader() {
		return header;
	}
	public void setHeader(Header header) {
		this.header = header;
	}
	public Object getBody() {
		return body;
	}
	public void setBody(Object body) {
		this.body = body;
	}
}
