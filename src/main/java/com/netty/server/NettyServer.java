package com.netty.server;

import com.tetty.channelhandler.ReqQueueHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import com.tetty.channelhandler.HeartRespHandler;
import com.tetty.channelhandler.LoginAuthRespHandler;
import com.tetty.channelhandler.RespQueueHandler;
import com.tetty.decode.TettyMessageDecoder;
import com.tetty.encode.TettyMessageEncode;
import com.tetty.listener.ReqHandlerListener;
import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class NettyServer {
	private static Logger log = LoggerFactory.getLogger(NettyServer.class);
	private List<RespQueueHandler> respQueueHandlers;

	public void start(int port){	
		new Thread(new Bind(port)).start();
	}

	public void setRespQueueHandlers(List<RespQueueHandler> respQueueHandlers) {
		this.respQueueHandlers = respQueueHandlers;
	}
	
	private class Bind implements Runnable{
		private int port;
		
		public Bind(int port){
			this.port = port;
		}

		public void run() {
			bind(port);
		}
	}
	
	public void bind(int port){
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup,workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.childHandler(new ChildChannelHandler());
		
		try {
			ChannelFuture f = bootstrap.bind(port).sync();
			log.info("tettyServer start");
			
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new TettyMessageDecoder(1024*1024,0,4));
			ch.pipeline().addLast(new TettyMessageEncode());
			ch.pipeline().addLast(new ReadTimeoutHandler(50));
			ch.pipeline().addLast(new LoginAuthRespHandler());
			ch.pipeline().addLast(new HeartRespHandler());
			//测试用的RespQueueHandler,实际使用中应该通过respQueueHandlers变量传入
			ch.pipeline().addLast(new RespQueueHandler(new ReqHandlerListener() {
				//echo
				public void readReq(ChannelHandlerContext ctx, TettyMessage req) {
					if(req.getHeader().getType() == Header.Type.ECHO_REQ){
						log.info("server rec:"+req.getBody());
						
						TettyMessage resp = new TettyMessage();
						Header header = new Header();
						header.setType(Header.Type.ECHO_RESP);
						
						resp.setHeader(header);
						resp.setBody(req.getBody());
						
						ctx.writeAndFlush(resp);
					}else{
						ctx.fireChannelRead(req);
					}
				}
			}));
			//传入自定义的respQueueHandler
			if (respQueueHandlers != null) {
				for (RespQueueHandler respQueueHandler : respQueueHandlers) {
					ch.pipeline().addLast(respQueueHandler);
				}
			}
		}
		
	}

	/**
	 * 测试tetty的服务端
	 * @param args
	 */
	public static void main(String[] args){
		int port = 8888;
		
		new NettyServer().start(port);
	}
}
