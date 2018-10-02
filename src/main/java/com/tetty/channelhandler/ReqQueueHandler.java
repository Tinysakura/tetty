package com.tetty.channelhandler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.tetty.listener.RespHandlerListener;
import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;

/**
 * 客户端发送请求的Handler
 * @author Administrator
 *
 */
public class ReqQueueHandler extends ChannelHandlerAdapter{
	Logger log = (Logger)LoggerFactory.getLogger(ReqQueueHandler.class);
	//第一种方式，将要发送的消息放在阻塞队列中，开启一个线程读取阻塞队列中积压的消息进行发送
	private LinkedBlockingQueue<TettyMessage> reqQueue = new LinkedBlockingQueue<TettyMessage>();
	private ArrayList<TettyMessage> reqTest = new ArrayList<TettyMessage>();
	private RespHandlerListener respHandler;
	private volatile boolean stop = false;
	
	public ReqQueueHandler(RespHandlerListener respHandler){
		this.respHandler = respHandler;
	}
	
	public void sendReq(TettyMessage req){
		try {
			reqQueue.put(req);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void sendTest(TettyMessage req){
		reqTest.add(req);
	}
	
	public void stopSend(){
		this.stop = true;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		//如果发生了异常可以将阻塞队列中未发送的请求持久化，等待连接恢复或连接重新建立成功后再将这些消息发送出去
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
        TettyMessage rec = (TettyMessage)msg;
		
		if(rec.getHeader().getType() == Header.Type.LOGIN_RESP){//说明握手成功，客户顿主动发送心跳消息
			//ctx.executor().execute(new MessageSendTask(ctx, reqQueue));
			ctx.executor().scheduleAtFixedRate(new Test(ctx, reqTest),0,20000,TimeUnit.MILLISECONDS);
		}else{//如果收到的是其他类型的消息直接交给respHandler处理
			respHandler.readResp(ctx, rec);
		}
	}
	
	private class MessageSendTask implements Runnable{
		private final ChannelHandlerContext ctx;
		private final LinkedBlockingQueue<TettyMessage> msg;

		public MessageSendTask(ChannelHandlerContext ctx,LinkedBlockingQueue<TettyMessage> msg){
			this.ctx = ctx;
			this.msg = msg;
		}
		
		public void run() {
			while(!Thread.currentThread().isInterrupted() && !stop){
				try {
					ctx.writeAndFlush(msg.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}
		
	}

	private class Test implements Runnable{
		private final ChannelHandlerContext ctx;
		private final ArrayList<TettyMessage> msg;

		public Test(ChannelHandlerContext ctx,ArrayList<TettyMessage> msg){
			this.ctx = ctx;
			this.msg = msg;
		}

		public void run() {
			log.info("test run");

			for(int i=0;i<10;i++){
				msg.add(buildEcho());
			}

			Iterator<TettyMessage> iterator = msg.iterator();

			while(iterator.hasNext()){
				ctx.writeAndFlush(iterator.next());
				iterator.remove();
			}
		}

	}

	public static TettyMessage buildEcho(){
		TettyMessage req = new TettyMessage();
		Header header = new Header();
		header.setType(Header.Type.ECHO_REQ);
		
		req.setHeader(header);
		req.setBody("掌声送给社会人");
		
		return req;
	}
}
