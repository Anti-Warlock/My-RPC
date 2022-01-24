package cn.anti.rpc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rpc配置
 * @author zhuyusheng
 * @date 2022/1/11
 */
@Data
@ConfigurationProperties(prefix = "anti.rpc")
public class RpcConfig {

    /**
     * 注册中心类型,默认zk,可选Nacos
     */
    private String registerType = "zk";
    /**
     * 注册中心地址
     */
    private String address = "127.0.0.1:2181";

    /**
     * 服务暴露端口
     */
    private Integer serverPort = 9999;

    /**
     * 序列化协议
     */
    private String protocol = "java";

    /**
     * 负载均衡实现方式
     */
    private String loadBalance = "random";

    /**
     * 权重
     */
    private Integer weight = 1;

}
