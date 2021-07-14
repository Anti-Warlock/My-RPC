package cn.anti.rpc.registry;


import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务注册接口
 * @author zhuyusheng
 * @date 2021/7/14
 */
public interface ServiceRegistry {

    /**
     * 根据服务名和服务实例注册到注册中心
     * @param serviceName
     * @param instance
     */
    void register(String serviceName, Instance instance);

    /**
     * 根据服务名和集群获取全部实例
     * @param serviceName
     * @param clusters
     * @return
     */
    List<Instance> getServiceList(String serviceName,List<String> clusters);

}
