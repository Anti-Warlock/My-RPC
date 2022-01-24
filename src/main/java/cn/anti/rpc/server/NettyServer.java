package cn.anti.rpc.server;

import lombok.Data;

/**
 * Rpc服务端
 * @author zhuyusheng
 * @date 2022/1/16
 */
@Data
public abstract class NettyServer {

    /**
     * 服务端口
     */
    protected Integer port;

    /**
     * 序列化协议
     */
    protected String protocol;

    /**
     * 请求处理类
     */
    protected RequestHandler requestHandler;

    public NettyServer(Integer port, String protocol, RequestHandler requestHandler) {
        this.port = port;
        this.protocol = protocol;
        this.requestHandler = requestHandler;
    }

    /**
     * 启动服务
     */
    public abstract void start();

    /**
     * 关闭服务
     */
    public abstract void stop();
}
