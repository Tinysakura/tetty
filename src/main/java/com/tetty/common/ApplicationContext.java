package com.tetty.common;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来存储与服务端建立连接的ChannelHandlerContext
 * @author Administrator
 *
 */
public class ApplicationContext {
	private static ConcurrentHashMap<Long, ChannelHandlerContext> onlineChannels =
			new ConcurrentHashMap<Long, ChannelHandlerContext>();
	
	public static void add(Long id,ChannelHandlerContext ctx){
		onlineChannels.put(id, ctx);
	}
	
	public static void remove(Long id){
		onlineChannels.remove(id);
	}
	
	public static ChannelHandlerContext get(Long id){
		return onlineChannels.get(id);
	}
	
	
	
	
	
	
	
	
}
