package cn.anti.rpc.registry.zookeeper;

import cn.anti.rpc.model.ServiceMetaData;
import cn.anti.rpc.registry.ServiceRegistry;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * Zookeeper注册
 * @author zhuyusheng
 * @date 2021/7/14
 */
public class ZookeeperRegistry implements ServiceRegistry {


    @Override
    public void register(ServiceMetaData serviceMetaData) throws Exception {

    }

    @Override
    public List<Instance> getServiceList(String serviceName, List<String> clusters) {

        return null;
    }
}
