package cn.anti.rpc.model;

import lombok.Data;

/**
 * 服务元数据
 * @author zhuyusheng
 * @date 2021/7/14
 */
@Data
public class ServiceMetaData {

    /**
     * 服务名称
     */
    private String serviceName;

//    /**
//     * 服务IP
//     */
//    private String ip;
//
//    /**
//     * 服务端口
//     */
//    private Integer port;
//
//    /**
//     * 服务版本
//     */
//    private Integer version;

    /**
     * 服务Class
     */
    private Class<?> clazz;

    /**
     * 具体服务
     */
    private Object obj;
}
