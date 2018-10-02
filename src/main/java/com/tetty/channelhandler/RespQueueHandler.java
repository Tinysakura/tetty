package com.tetty.channelhandler;

import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import ch.qos.logback.classic.Logger;

import com.tetty.listener.ReqHandlerListener;
import com.tetty.pojo.TettyMessage;

/**
 * 服务端处理服务端发送的消息的Handler
 * @author Administrator
 */
public class RespQueueHandler extends ChannelHandlerAdapter{
	private ReqHandlerListener reqHandler;
	private Logger log = (Logger)LoggerFactory.getLogger(RespQueueHandler.class);
	
	public RespQueueHandler(ReqHandlerListener reqHandler){
		this.reqHandler = reqHandler;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		//可以对未处理的消息持久化等待异常状态恢复后再做处理
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		//只接受已登录用户的请求
		if(LoginAuthRespHandler.loginedNode.containsKey(ctx.channel().remoteAddress().toString())){
			reqHandler.readReq(ctx, (TettyMessage)msg);
		}else{
			log.info("未认证的用户");
		}
	}
}
