package com.tetty.server;

import com.tetty.channelhandler.HeartReqHandler;
import com.tetty.channelhandler.LoginAuthReqHandler;
import com.tetty.channelhandler.ReqQueueHandler;
import com.tetty.common.ChannelContext;
import com.tetty.consts.NettyConsts;
import com.tetty.decode.TettyMessageDecoder;
import com.tetty.encode.TettyMessageEncode;
import com.tetty.listener.RespHandlerListener;
import com.tetty.pojo.Header;
import com.tetty.pojo.TettyMessage;
import com.tetty.util.ChannelKeyGeneratorUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 具有断线重连能力的客户端
 *
 * @author Administrator
 */
public class NettyClient {
    static Logger log = LoggerFactory.getLogger(NettyClient.class);
    private List<ReqQueueHandler> reqQueueHandlers;
    private CountDownLatch latch;

    private ScheduledExecutorService scheduledExecutor =
            Executors.newScheduledThreadPool(1);

    public void setReqQueueHandlers(List<ReqQueueHandler> reqQueueHandler) {
        this.reqQueueHandlers = reqQueueHandler;
    }

    public void start(String host, int port, CountDownLatch latch) {
        new Thread(new Connect(host, port)).start();
        this.latch = latch;
    }

    private class Connect implements Runnable {
        private String host;
        private int port;

        public Connect(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public void run() {
            connect(host, port);
        }

    }

    public void connect(final String host, final int port) {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TettyMessageDecoder(1024 * 1024, 0, 4));
                            ch.pipeline().addLast(new TettyMessageEncode());
                            ch.pipeline().addLast(new ReadTimeoutHandler(50));//超时时间设置为50s
                            ch.pipeline().addLast("loginAuthReqHandler", new LoginAuthReqHandler());
                            ch.pipeline().addLast("heartBeatReqHandler", new HeartReqHandler());


                            //将所有的reqQueueHandler加入pipeline
                            if (reqQueueHandlers != null) {
                                for (ReqQueueHandler reqQueueHandler : reqQueueHandlers) {
                                    ch.pipeline().addLast(reqQueueHandler);
                                }
                            }
                        }
                    });

            ChannelFuture f = bootstrap.connect(
                    new InetSocketAddress(host, port),
                    new InetSocketAddress(NettyConsts.LOCAL_HOST, NettyConsts.LOCAL_IP));//绑定了本地ip与本地端口
            System.out.println("begin connect");
            f.addListener(new GenericFutureListener<Future<? super Void>>() {

                public void operationComplete(Future<? super Void> future)
                        throws Exception {
                    ChannelFuture f = (ChannelFuture) future;
                    System.out.println("connect result:" + future.isSuccess());
                    if (future.isSuccess()) {
                        if (f.channel() != null) {
                            //客户端连接建立完成后将消息的信道保存在ChannelContext中
                            String key = ChannelKeyGeneratorUtil.generator(host, port);
                            ChannelContext.addChannel(key, f.channel());
                        }
                    }

                    //释放闭锁
                    latch.countDown();
                }
            });

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();

            //断线重连
            scheduledExecutor.execute(new Runnable() {

                public void run() {
                    try {
                        Thread.currentThread().sleep(5000);//每间隔5s连接一次
                        log.info("断线重连============");

                        connect(host, port);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            });
        }
    }

    /**
     * 测试tetty的客户端
     *
     * @param args
     */
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8888;

        NettyClient nettyClient = new NettyClient();
        final ReqQueueHandler reqQueueHandler = new ReqQueueHandler(new RespHandlerListener() {
            //echo
            public void readResp(ChannelHandlerContext ctx, TettyMessage resp) {
                byte type = resp.getHeader().getType();
                if (type == Header.Type.ECHO_RESP) {
                    log.info("echo:{}", resp.getBody());
                } else {
                    //如果消息不是ECHO类型的则将消息透传给下一个ChannelHandler
                    ctx.fireChannelRead(resp);
                }
            }
        });

        List<ReqQueueHandler> reqQueueHandlers = new ArrayList<ReqQueueHandler>();
        reqQueueHandlers.add(reqQueueHandler);

        nettyClient.setReqQueueHandlers(reqQueueHandlers);

        //使用一个闭锁等待异步的连接完成
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            nettyClient.start(host, port, countDownLatch);
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Channel channel = ChannelContext.getChannel(ChannelKeyGeneratorUtil.generator(host, port));

        //测试使用从ChannelContext中获取的信道进行消息的发送
        if (channel != null) {
            log.info("this channel is not null");

            new Thread(new Runnable() {

                public void run() {
                    while (true) {
                        TettyMessage echo = buildEcho();
                        echo.setBody("卡面来打build");

                        channel.writeAndFlush(echo);

                        try {
                            Thread.currentThread().sleep(2000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * 构建echo类型的消息
     * @return
     */
    public static TettyMessage buildEcho() {
        TettyMessage req = new TettyMessage();
        Header header = new Header();
        header.setType(Header.Type.ECHO_REQ);

        req.setHeader(header);

        return req;
    }

}
