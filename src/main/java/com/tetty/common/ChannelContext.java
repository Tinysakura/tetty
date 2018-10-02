package com.tetty.common;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存与tetty客户端的通信信道
 */
public class ChannelContext {
	private static ConcurrentHashMap<String, Channel> channelsMap;

	static {
		channelsMap = new ConcurrentHashMap<String, Channel>();
	}

	public static void addChannel(String channelKey, Channel channel) {
		channelsMap.put(channelKey, channel);
	}

	public static Channel getChannel(String channelkey) {
		return channelsMap.get(channelkey);
	}
}
