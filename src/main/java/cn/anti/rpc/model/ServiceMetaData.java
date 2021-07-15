package cn.anti.rpc.model;

import lombok.Data;

/**
 * 服务元数据
 * @author zhuyusheng
 * @date 2021/7/14
 */
@Data
public class ServiceMetaData {

    private String serviceName;

    private Integer version;

    private Class<?> clazz;
}
