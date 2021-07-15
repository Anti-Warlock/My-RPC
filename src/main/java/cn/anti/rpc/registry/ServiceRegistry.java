package cn.anti.rpc.registry;

import cn.anti.rpc.model.ServiceMetaData;
import com.alibaba.nacos.api.naming.pojo.Instance;
import java.util.List;

/**
 * 服务注册接口
 * @author zhuyusheng
 * @date 2021/7/14
 */
public interface ServiceRegistry {

    /**
     * 根据服务元数据注册到注册中心
     * @param serviceMetaData
     * @throws Exception
     */
    void register(ServiceMetaData serviceMetaData)throws Exception;

    /**
     * 根据服务名和集群获取全部实例
     * @param serviceName
     * @param clusters
     * @return
     */
    List<Instance> getServiceList(String serviceName,List<String> clusters);



}
