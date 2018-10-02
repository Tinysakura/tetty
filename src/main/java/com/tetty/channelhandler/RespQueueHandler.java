package com.tetty.channelhandler;

import ch.qos.logback.classic.Logger;
import com.tetty.pojo.TettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.LoggerFactory;

/**
 * 服务端处理服务端发送的消息的Handler
 * @author Administrator
 */
public abstract class RespQueueHandler extends ChannelHandlerAdapter{
	private Logger log = (Logger)LoggerFactory.getLogger(RespQueueHandler.class);
	
	public RespQueueHandler(){
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
			readReq(ctx, (TettyMessage)msg);
		}else{
			log.info("未认证的用户");
		}
	}

	public abstract void readReq(ChannelHandlerContext ctx, TettyMessage rec);
}
