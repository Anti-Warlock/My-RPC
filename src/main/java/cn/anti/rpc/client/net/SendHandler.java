package cn.anti.rpc.client.net;

import cn.anti.rpc.client.serializeProtocol.SerializeProtocol;
import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;
import cn.anti.rpc.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * netty发送处理类
 * @author zhuyusheng
 * @date 2022/1/4
 */
public class SendHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SendHandler.class);

    /**
     * 等待通道建立最大时间
     */
    static final int CHANNEL_WAIT_TIME = 4;
    /**
     * 等待响应最大时间
     */
    static final int RESPONSE_WAIT_TIME = 8;

    private volatile Channel channel;

    private String remoteAddress;

    private static Map<String, RpcFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();

    private SerializeProtocol serializeProtocol;

    private CountDownLatch latch = new CountDownLatch(1);

    public SendHandler(SerializeProtocol serializeProtocol,String remoteAddress) {
        this.serializeProtocol = serializeProtocol;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        latch.countDown();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Connect Server Successful:{}",ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        super.channelInactive(ctx);
        logger.error("Channel Inactive With RemoteAddress:{}",remoteAddress);
        NettyClient.nodes.remove(remoteAddress);
    }

    /**
     * 读取服务端的响应信息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("Client Read Message:{}",msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] respData = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(respData);
        //手动释放ByteBuf
        ReferenceCountUtil.release(byteBuf);
        //解组响应
        String s = new String(respData);
        logger.debug("Response:{}",s);
        RpcResponse response = serializeProtocol.unmarshallResponse(respData);
        logger.debug("Response:{}",response);
        //根据缓存中的请求ID获取对应的future
        RpcFuture<RpcResponse> future = requestMap.get(response.getRequestId());
        //发送响应
        future.setResponse(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("Exception Occurred:{}",cause.getMessage());
        ctx.close();
    }

    public RpcResponse sendRequest(RpcRequest request){
        RpcResponse response;
        RpcFuture<RpcResponse> future = new RpcFuture<>();
        //发送请求前先把(请求ID和future)缓存到Map中
        requestMap.put(request.getRequestId(),future);
        //读取到消息后再讲消息存入到future中
        try {
            byte[] reqData = serializeProtocol.marshallingRequest(request);
            ByteBuf byteBuf = Unpooled.buffer(reqData.length);
            byteBuf.writeBytes(reqData);
            logger.debug("ByteBuf:{}",byteBuf.toString());
            if(latch.await(CHANNEL_WAIT_TIME, TimeUnit.SECONDS)){
                channel.writeAndFlush(byteBuf);
                logger.debug("Finish Write!");
                //等待响应
                response = future.get(RESPONSE_WAIT_TIME,TimeUnit.SECONDS);
                logger.debug("Response:{}",response);
            }else {
                throw new RpcException("channel time out");
            }
        }catch (Exception e){
            throw new RpcException(e.getMessage());
        }finally {
            //每次发送请求时都是新的requestId,需移除该requestId对应的future
            requestMap.remove(request.getRequestId());
        }
        return response;
    }
}


//public class SendHandler extends ChannelInboundHandlerAdapter {
//
//    private static Logger logger = LoggerFactory.getLogger(SendHandler.class);
//
//    /**
//     * 等待通道建立最大时间
//     */
//    static final int CHANNEL_WAIT_TIME = 4;
//    /**
//     * 等待响应最大时间
//     */
//    static final int RESPONSE_WAIT_TIME = 8;
//
//    private volatile Channel channel;
//
//    private String remoteAddress;
//
//    private static Map<String, RpcFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();
//
//    private SerializeProtocol messageProtocol;
//
//    private CountDownLatch latch = new CountDownLatch(1);
//
//    public SendHandler(SerializeProtocol messageProtocol,String remoteAddress) {
//        this.messageProtocol = messageProtocol;
//        this.remoteAddress = remoteAddress;
//    }
//
//    @Override
//    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//        this.channel = ctx.channel();
//        latch.countDown();
//    }
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        logger.debug("Connect to server successfully:{}", ctx);
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        logger.debug("Client reads message:{}", msg);
//        ByteBuf byteBuf = (ByteBuf) msg;
//        byte[] resp = new byte[byteBuf.readableBytes()];
//        byteBuf.readBytes(resp);
//        // 手动回收
//        ReferenceCountUtil.release(byteBuf);
//        String s = new String(resp);
//        logger.debug("Response:{}",s);
//        RpcResponse response = messageProtocol.unmarshallResponse(resp);
//        logger.debug("Response:{}",response);
//        RpcFuture<RpcResponse> future = requestMap.get(response.getRequestId());
//        future.setResponse(response);
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
//        logger.error("Exception occurred:{}", cause.getMessage());
//        ctx.close();
//    }
//
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.flush();
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        super.channelInactive(ctx);
//        logger.error("channel inactive with remoteAddress:[{}]",remoteAddress);
//        NettyClient.connectedServerNodes.remove(remoteAddress);
//    }
//
//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        super.userEventTriggered(ctx, evt);
//    }
//
//    public RpcResponse sendRequest(RpcRequest request) {
//        RpcResponse response;
//        RpcFuture<RpcResponse> future = new RpcFuture<>();
//        requestMap.put(request.getRequestId(), future);
//        try {
//            byte[] data = messageProtocol.marshallingRequest(request);
//            ByteBuf reqBuf = Unpooled.buffer(data.length);
//            reqBuf.writeBytes(data);
//            if (latch.await(CHANNEL_WAIT_TIME,TimeUnit.SECONDS)){
//                channel.writeAndFlush(reqBuf);
//                // 等待响应
//                response = future.get(RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
//            }else {
//                throw new RpcException("establish channel time out");
//            }
//        } catch (Exception e) {
//            throw new RpcException(e.getMessage());
//        } finally {
//            requestMap.remove(request.getRequestId());
//        }
//        return response;
//    }
//
//
//}
