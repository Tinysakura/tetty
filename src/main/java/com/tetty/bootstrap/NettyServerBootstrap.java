package com.tetty.bootstrap;

import org.slf4j.LoggerFactory;

import com.netty.server.NettyServer;

import ch.qos.logback.classic.Logger;

/**
 * netty服务端启动类
 * @author Mr.Chen
 * date: 2018年8月14日 上午11:41:17
 */
public class NettyServerBootstrap {
	Logger log = (Logger)LoggerFactory.getLogger(NettyServerBootstrap.class);
	
	private int port;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void serverStart(){
		new NettyServer().start(port);
		log.info("server start in port:{}"+port);
	}
}
