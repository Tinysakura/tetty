package com.tetty.encode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.tetty.pojo.TettyMessage;
import com.tetty.util.JsonUtil;

/**
 * TettyMessage的编码类
 * @author Administrator
 *
 */
public class TettyMessageEncode extends MessageToMessageEncoder<TettyMessage>{
	Logger log = (Logger)LoggerFactory.getLogger(TettyMessageEncode.class);
	
	@Override
	protected void encode(ChannelHandlerContext ctx, TettyMessage msg,
			List<Object> out) throws Exception {
		if(msg == null || msg.getHeader() == null){
			throw new Exception("the encode message is null");//先检查消息的合法性
		}
		
		log.info("body:"+msg.getBody());
		
		byte[] bytes = JsonUtil.obj2String(msg).getBytes("UTF-8");
		ByteBuf sendBuf = Unpooled.copiedBuffer(bytes);
		
		log.info("bytes length:"+bytes.length);
		//在消息开头写入消息的长度（配合LengthFieldBasedFrameDecoder解决粘包粘包问题）
		ByteBuf finalBuf = Unpooled.buffer();
		finalBuf.writeInt(sendBuf.readableBytes());
		finalBuf.writeBytes(sendBuf);
		out.add(finalBuf);
	}

}
