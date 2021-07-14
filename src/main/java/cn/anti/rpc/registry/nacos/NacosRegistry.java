package cn.anti.rpc.registry.nacos;


import cn.anti.rpc.registry.ServiceRegistry;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Nacos注册
 * @author zhuyusheng
 * @date 2021/7/14
 */
@Slf4j
public class NacosRegistry implements ServiceRegistry {

    private static final String SERVER_ADDR = "127.0.0.1:8848";
    private static NamingService namingService;

    static {
        try {
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("连接Nacos失败,{}",e.getErrMsg());
        }
    }

    @Override
    public void register(String serviceName, Instance instance) {
        try {
            namingService.registerInstance(serviceName,instance);
        } catch (NacosException e) {
            log.error("服务注册失败:{}",e.getErrMsg());
        }
    }

    @Override
    public List<Instance> getServiceList(String serviceName, List<String> clusters){
        List<Instance> list = null;
        try {
            if(CollectionUtil.isNotEmpty(clusters)){
                list = namingService.getAllInstances(serviceName,clusters);
            }else {
                list = namingService.getAllInstances(serviceName);
            }
        } catch (NacosException e) {
            log.error("获取服务列表失败:{}",e.getErrMsg());
        }
        return list;
    }

    public void subscribeOnServiceChange(String serviceName, List<String> clusters){
        try {
            namingService.subscribe(serviceName, clusters, event -> {
                if(event instanceof NamingEvent){
                    List<Instance> instances = ((NamingEvent) event).getInstances();
                }
            });
        } catch (NacosException e) {
            log.error("订阅服务失败:{}",e.getErrMsg());
        }
    }
}
