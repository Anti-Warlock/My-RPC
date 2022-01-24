package cn.anti.rpc.client.net;

import cn.anti.rpc.client.serializeProtocol.SerializeProtocol;
import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;
import cn.anti.rpc.model.ServiceUrl;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Netty客户端
 * @author zhuyusheng
 * @date 2022/1/4
 */
public class NettyClient implements NetClient{

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static ExecutorService threadPool = new ThreadPoolExecutor(4,10,200,
            TimeUnit.SECONDS,new LinkedBlockingQueue<>(1000),new ThreadFactoryBuilder().setNameFormat("RPC_Client-%d").build());

    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);

    /**
     * 已连接的节点缓存
     * 服务地址 格式：ip:port
     */
    public static Map<String,SendHandler> nodes = new ConcurrentHashMap<>();

    @Override
    public RpcResponse sendRequest(RpcRequest request, ServiceUrl serviceUrl, SerializeProtocol serializeProtocol) {
        String address = serviceUrl.getAddress();
        synchronized (address){
            if(nodes.containsKey(address)){
                SendHandler sendHandler = nodes.get(address);
                logger.info("using existing connection!");
                return sendHandler.sendRequest(request);
            }
            String[] addrArr = address.split(":");
            final String serverIp = addrArr[0];
            final String serverPort = addrArr[1];
            final SendHandler sendHandler = new SendHandler(serializeProtocol, address);
            threadPool.submit(() -> {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                pipeline.addLast(sendHandler);
                            }
                        });
                ChannelFuture channelFuture = bootstrap.connect(serverIp, Integer.parseInt(serverPort));
                channelFuture.addListener((ChannelFutureListener) channelFuture1 -> nodes.put(address, sendHandler));
            });
            logger.info("using new connection!");
            return sendHandler.sendRequest(request);
        }
    }

//    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);
//
//    private static ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 200,
//            TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadFactoryBuilder()
//            .setNameFormat("rpcClient-%d")
//            .build());
//
//    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);
//
//    /**
//     * 已连接的服务缓存
//     * key: 服务地址，格式：ip:port
//     */
//    public static Map<String, SendHandler> connectedServerNodes = new ConcurrentHashMap<>();
//
//    @Override
//    public RpcResponse sendRequest(RpcRequest rpcRequest, ServiceUrl service, SerializeProtocol messageProtocol) {
//
//        String address = service.getAddress();
//        synchronized (address) {
//            if (connectedServerNodes.containsKey(address)) {
//                SendHandler handler = connectedServerNodes.get(address);
//                logger.info("使用现有的连接");
//                return handler.sendRequest(rpcRequest);
//            }
//
//            String[] addrInfo = address.split(":");
//            final String serverAddress = addrInfo[0];
//            final String serverPort = addrInfo[1];
//            final SendHandler handler = new SendHandler(messageProtocol, address);
//            threadPool.submit(() -> {
//                        // 配置客户端
//                        Bootstrap b = new Bootstrap();
//                        b.group(loopGroup).channel(NioSocketChannel.class)
//                                .option(ChannelOption.TCP_NODELAY, true)
//                                .handler(new ChannelInitializer<SocketChannel>() {
//                                    @Override
//                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
//                                        ChannelPipeline pipeline = socketChannel.pipeline();
//                                        pipeline
////                                                .addLast(new FixedLengthFrameDecoder(20))
//                                                .addLast(handler);
//                                    }
//                                });
//                        // 启用客户端连接
//                        ChannelFuture channelFuture = b.connect(serverAddress, Integer.parseInt(serverPort));
//                        channelFuture.addListener(new ChannelFutureListener() {
//                            @Override
//                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                                connectedServerNodes.put(address, handler);
//                            }
//                        });
//                    }
//            );
//            logger.info("使用新的连接。。。");
//            return handler.sendRequest(rpcRequest);
//        }
//    }
}
