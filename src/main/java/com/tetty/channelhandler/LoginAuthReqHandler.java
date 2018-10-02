package com.tetty.channelhandler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;

/**
 * 客户端发起握手请求的channelhandler
 * @author Administrator
 */
public class LoginAuthReqHandler extends ChannelHandlerAdapter{
	Logger log = (Logger)LoggerFactory.getLogger(LoginAuthReqHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.fireExceptionCaught(cause);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(buildLoginMessage());//向客户端发起握手请求
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		TettyMessage loginResp = (TettyMessage)msg;
		
		if(loginResp.getHeader() != null && loginResp.getHeader().getType() == Header.Type.LOGIN_RESP){
			//log.info("body:{}",loginResp.getBody());
			if(loginResp.getBody() instanceof Integer){
				log.warn("body type is integer");
			}
			
			//注意使用jackson反序列化会将写入的byte类型反序列化为Integer类型
			byte result = (byte)((Integer)loginResp.getBody()).intValue();
			if(result != (byte)0){//协议约定服务端应答的消息体为0时代表同意连接
				ctx.close();
				log.warn("服务端拒绝连接");
			}else{
				log.info("连接建立完成");
				//this.ctx = ctx;//将客户端与服务端的连接保存在一个静态变量中
				ctx.fireChannelRead(msg);//透传给HeartHandler
			}
		}else{
			ctx.fireChannelRead(msg);//如果不是握手的应答消息直接透传给下一个handler
		}
	}
	
	private TettyMessage buildLoginMessage(){
		Header header = new Header();
		header.setType(Header.Type.LOGIN_REQ);
		header.setSessionId(1l);//为双方的通信链路建立唯一标识
		
		TettyMessage loginMessage = new TettyMessage();
		loginMessage.setHeader(header);
		
		return loginMessage;
	}
}
