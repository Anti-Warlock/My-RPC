package cn.anti.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接节点的初始化属性
 * @author zhuyusheng
 * @date 2021/12/07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceUrl {

    /**
     * 服务名称
     */
    private String name;

    /**
     * 序列化协议
     */
    private String protocol;

    /**
     * 服务地址 ip:port
     */
    private String address;

    /**
     * 服务权重 越大优先级越高
     */
    private Integer weight;


}
