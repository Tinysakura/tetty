package com.tetty.channelhandler;

import org.slf4j.LoggerFactory;

import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;

import ch.qos.logback.classic.Logger;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 客户端处理用户发送的心跳消息的Handler
 * @author Administrator
 *
 */
public class HeartRespHandler extends ChannelHandlerAdapter{
	Logger log = (Logger)LoggerFactory.getLogger(HeartRespHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		TettyMessage rec = (TettyMessage)msg;
		
		if(rec.getHeader().getType() == Header.Type.HEART_REQ){
			log.info("rec {} heratbeat",ctx.channel().remoteAddress());
			ctx.writeAndFlush(buildHeartResp());
		}else{
			ctx.fireChannelRead(msg);
		}
	}
	
	private TettyMessage buildHeartResp(){
		Header header = new Header();
		header.setType(Header.Type.HEART_RESP);
		
		TettyMessage heartRespMessage = new TettyMessage();
		heartRespMessage.setHeader(header);
		
		return heartRespMessage;
	}
	
}
