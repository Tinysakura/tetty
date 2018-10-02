package com.tetty.channelhandler;

import com.tetty.enums.LoginStatusEnum;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.tetty.common.ApplicationContext;
import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;

/**
 * 服务端处理握手消息的ChannelHandler
 * @author Administrator
 *
 */
public class LoginAuthRespHandler extends ChannelHandlerAdapter{
	Logger log = (Logger)LoggerFactory.getLogger(LoginAuthRespHandler.class);
	//ip白名单
	private static Set<String> whiteIp = new HashSet<String>();
	//已连接用户
	public static Map<String, Boolean> loginedNode = new ConcurrentHashMap<String, Boolean>();
	
	static{
		whiteIp.add("127.0.0.1");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		loginedNode.remove(ctx.channel().remoteAddress().toString());//如果发生异常要将对应的节点从已登录名单中剔除
		log.info("remove {} from logindeNode",ctx.channel().remoteAddress().toString());
		ctx.fireExceptionCaught(cause);
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		TettyMessage rec = (TettyMessage)msg;
		
		if(rec.getHeader().getType() == Header.Type.LOGIN_REQ){
			InetSocketAddress address = (InetSocketAddress)ctx.channel().remoteAddress();
			if(!whiteIp.contains(address.getAddress().getHostAddress())){
				//如果连接的节点的ip地址不在白名单中，拒绝连接
				log.warn(LoginStatusEnum.NOT_IN_WHITE_TABLES.getMsg());
				ctx.writeAndFlush(buildLoginResp(LoginStatusEnum.NOT_IN_WHITE_TABLES.getCode()));
			}else{//判断节点是否已连入
				if(loginedNode.containsKey(ctx.channel().remoteAddress().toString())){
					log.warn(LoginStatusEnum.REPEAT_LOGIN.getMsg());
					ctx.writeAndFlush(buildLoginResp(LoginStatusEnum.REPEAT_LOGIN.getCode()));
				}else{
				    log.warn(LoginStatusEnum.NORMAL_LOGIN.getMsg());
				    loginedNode.put(ctx.channel().remoteAddress().toString(), true);
				    ApplicationContext.add(rec.getHeader().getSessionId(), ctx);
				    ctx.writeAndFlush(buildLoginResp(LoginStatusEnum.NORMAL_LOGIN.getCode()));
				}
			}
		}else{
			ctx.fireChannelRead(msg);
		}
		
	}
	
	private TettyMessage buildLoginResp(byte result){
		Header header = new Header();
		header.setType(Header.Type.LOGIN_RESP);
		
		TettyMessage loginMessage = new TettyMessage();
		loginMessage.setHeader(header);
		loginMessage.setBody(result);
		
		return loginMessage;
	}

}
