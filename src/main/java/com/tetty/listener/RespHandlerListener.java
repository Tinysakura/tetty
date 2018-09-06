package com.tetty.listener;

import com.tetty.pojo.TettyMessage;

import io.netty.channel.ChannelHandlerContext;

public interface RespHandlerListener {
	public void readResp(ChannelHandlerContext ctx,TettyMessage resp);
}
