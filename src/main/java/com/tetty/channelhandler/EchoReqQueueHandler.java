package com.tetty.channelhandler;

import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author: cfh
 * @Date: 2018/10/2 16:48
 * @Description: 回声消息处理器
 */
public class EchoReqQueueHandler extends ReqQueueHandler{
    public void readResp(ChannelHandlerContext ctx, TettyMessage rec) {
        byte type = rec.getHeader().getType();
        if (type == Header.Type.ECHO_RESP) {
            log.info("echo:{}", rec.getBody());
        } else {
            //如果消息不是ECHO类型的则将消息透传给下一个ChannelHandler
            ctx.fireChannelRead(rec);
        }
    }
}
