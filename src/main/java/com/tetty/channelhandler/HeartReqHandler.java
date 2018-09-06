package com.tetty.channelhandler;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * 客户端定时向服务端发送消息的心跳Handler
 * @author Administrator
 *
 */
public class HeartReqHandler extends ChannelHandlerAdapter{
	Logger log = (Logger)LoggerFactory.getLogger(HeartReqHandler.class);
	private volatile ScheduledFuture<?> heartFuture;

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if(heartFuture != null){
			heartFuture.cancel(true);//放弃等待的异步执行的任务的结果
			heartFuture = null;//help gc
		}
		
		ctx.fireExceptionCaught(cause);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		TettyMessage rec = (TettyMessage)msg;
		
		if(rec.getHeader().getType() == Header.Type.LOGIN_RESP){//说明握手成功，客户顿主动发送心跳消息
			//每隔5000毫秒发送一个心跳消息
			heartFuture = 
					ctx.executor().scheduleAtFixedRate(new HeartSendTask(ctx), 0, 5000, TimeUnit.MILLISECONDS);
			
			log.info("HeartSendTask begin");
		}else if(rec.getHeader().getType() == Header.Type.HEART_RESP){
			//如果收到的是服务端发送的心跳响应消息则直接打印日志即可
			log.info("recive heart beat from sever");
		}
		
		ctx.fireChannelRead(msg);
	}

	/**
	 * 发送心跳数据的task
	 */
	private class HeartSendTask implements Runnable{
		private final ChannelHandlerContext ctx;//final修饰符可以保证线程安全
		private TettyMessage heartReqMessage;
		
		public HeartSendTask(final ChannelHandlerContext ctx){
			this.ctx = ctx;
			this.heartReqMessage = buildHeartMessage();
		}

		public void run() {
			log.info("client send heart beat");
			ctx.writeAndFlush(heartReqMessage);
		}
		
		private TettyMessage buildHeartMessage(){
			Header header = new Header();
			header.setType(Header.Type.HEART_REQ);
			
			TettyMessage heartReqMessage = new TettyMessage();
			heartReqMessage.setHeader(header);
			
			return heartReqMessage;
		}
		
	}

}
