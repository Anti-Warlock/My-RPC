package cn.anti.rpc.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * NettyRpcServer
 * @author zhuyusheng
 * @date 2022/1/16
 */
public class NettyNettyServer extends NettyServer {

    private static Logger logger = LoggerFactory.getLogger(NettyNettyServer.class);

    private Channel channel;

    private static ExecutorService threadPool = new ThreadPoolExecutor(4,10,200,
            TimeUnit.SECONDS,new LinkedBlockingQueue<>(1000),new ThreadFactoryBuilder().setNameFormat("RPC_Server-%d").build());

    public NettyNettyServer(Integer port, String protocol, RequestHandler requestHandler) {
        super(port, protocol, requestHandler);
    }

    @Override
    public void start() {
        // 配置服务器
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new ChannelRequestHandler());
                        }
                    });
            //启动服务
            ChannelFuture future = b.bind(port).sync();
            logger.debug("start netty rpc server success!");
            channel = future.channel();
            //等待服务通道关闭
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
            logger.error("start netty rpc server failed,msg:{}",e.getMessage());
        }finally {
            //释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        this.channel.close();
    }

    private class ChannelRequestHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("channel active:{}",ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            threadPool.submit(() -> {
                try {
                    logger.debug("the server receives message:{}",msg);
                    ByteBuf byteBuf = (ByteBuf) msg;
                    //1.将消息写入到reqData
                    byte[] reqData = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(reqData);
                    //2.手动回收
                    ReferenceCountUtil.release(byteBuf);
                    byte[] resData = requestHandler.handRequest(reqData);
                    ByteBuf buffer = Unpooled.buffer(resData.length);
                    buffer.writeBytes(resData);
                    logger.debug("send response:{}",buffer);
                    ctx.writeAndFlush(buffer);
                } catch (Exception e) {
                    logger.error("server read exception,msg:{}",e.getMessage());
                }
            });
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            logger.error("exception caused:{}",cause.getMessage());
            ctx.close();
        }
    }
}
