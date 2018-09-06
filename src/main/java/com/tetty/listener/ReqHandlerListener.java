package com.tetty.listener;

import com.tetty.pojo.TettyMessage;

import io.netty.channel.ChannelHandlerContext;

public interface ReqHandlerListener {
	public void readReq(ChannelHandlerContext ctx,TettyMessage req);
}
