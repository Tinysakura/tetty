package com.tetty.channelhandler;

import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: cfh
 * @Date: 2018/10/2 16:49
 * @Description: 回声消息处理器
 */
public class EchoRespQueueHandler extends RespQueueHandler {
    static final Logger log = LoggerFactory.getLogger(EchoRespQueueHandler.class);

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
}
