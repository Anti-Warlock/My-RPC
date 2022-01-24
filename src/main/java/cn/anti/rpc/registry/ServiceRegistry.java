package cn.anti.rpc.registry;

import cn.anti.rpc.model.ServiceMetaData;
import cn.anti.rpc.model.ServiceUrl;
import com.alibaba.nacos.api.exception.NacosException;
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
     * 根据服务名称获取服务元数据
     * @param serviceName
     * @return
     * @throws Exception
     */
    ServiceMetaData getServiceMetaData(String serviceName) throws Exception;

    /**
     * 根据服务名获取全部实例
     * @param serviceName
     * @return
     * @throws NacosException
     */
    List<ServiceUrl> getServiceList(String serviceName) throws NacosException;

    /**
     * 订阅服务节点变化
     * @param serviceName
     * @throws NacosException
     */
    void subscribeOnServiceChange(String serviceName) throws NacosException;

}
