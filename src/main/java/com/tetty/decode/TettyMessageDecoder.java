package com.tetty.decode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.tetty.pojo.TettyMessage;
import com.tetty.util.JsonUtil;

/**
 * TettyMessage的解码类
 * 借助LengthFieldBasedFrameDecoder处理半包消息
 * @author Administrator
 *
 */
public class TettyMessageDecoder extends LengthFieldBasedFrameDecoder{
	Logger log = (Logger) LoggerFactory.getLogger(TettyMessageDecoder.class);

	public TettyMessageDecoder(int maxFrameLength, int lengthFieldOffset,
			int lengthFieldLength) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {
		log.info("decode");
		ByteBuf frame = (ByteBuf)super.decode(ctx, in);
		
		if(frame == null){//说明是半包，交由io线程继续读
			log.warn("半包");
			return null;
		}
		
		try {
			//对frame进行解码
			byte[] bytes = new byte[frame.readableBytes()-4];
			log.info("frame length:"+frame.readInt());
			frame.readBytes(bytes);
			
			String json = new String(bytes,"UTF-8");
			log.info("parse json:"+json);
			
			TettyMessage message = JsonUtil.string2Obj(json, TettyMessage.class);
			
			return message;
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("decode failed");
			return new TettyMessage();//返回一个空的message;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
