package cn.anti.rpc.registry.nacos;


import cn.anti.rpc.exception.RpcException;
import cn.anti.rpc.model.ServiceMetaData;
import cn.anti.rpc.model.ServiceUrl;
import cn.anti.rpc.registry.DefaultServerRegistry;
import cn.anti.rpc.registry.ServiceRegistry;
import cn.anti.rpc.registry.cache.ServerServiceMetadataCache;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nacos注册
 * @author zhuyusheng
 * @date 2021/7/14
 */
@Slf4j
public class NacosRegistry extends DefaultServerRegistry {

    private static Logger logger = LoggerFactory.getLogger(NacosRegistry.class);

    private static NamingService namingService;

    public NacosRegistry(String serverAddr,Integer port,String protocol,Integer weight){
        try {
            namingService = NamingFactory.createNamingService(serverAddr);
            this.port = port;
            this.protocol = protocol;
            this.weight = weight;
        } catch (NacosException e) {
            logger.error("Connect Nacos Error!");
            throw new RpcException("Connect Nacos Error!");
        }
    }

    /**
     * 注册服务到Nacos注册中心
     * @param serviceMetaData
     * @throws Exception
     */
    @Override
    public void register(ServiceMetaData serviceMetaData) throws Exception {
        super.register(serviceMetaData);
        String ipAddr = InetAddress.getLocalHost().getHostAddress();
        namingService.registerInstance(serviceMetaData.getServiceName(),ipAddr,port);
    }

    @Override
    public List<ServiceUrl> getServiceList(String serviceName) throws NacosException {
        //增加默认处理,只获取健康的实例
        List<Instance> allInstances = namingService.getAllInstances(serviceName);
        List<ServiceUrl> serviceUrlList = allInstances.stream().map(ins -> new ServiceUrl(ins.getServiceName(),null,ins.getIp() + ":" + ins.getPort(), (int) ins.getWeight())).collect(Collectors.toList());
        ServerServiceMetadataCache.put(serviceName,serviceUrlList);
        return serviceUrlList;
    }

    @Override
    public void subscribeOnServiceChange(String serviceName) throws NacosException {
        namingService.subscribe(serviceName, event -> {
            if(event instanceof NamingEvent){
                List<Instance> instances = ((NamingEvent) event).getInstances();
                String name = ((NamingEvent) event).getServiceName();
                log.info("name:[{}]--instances:[{}]",name,instances);
                ServerServiceMetadataCache.removeAll(serviceName);
            }
        });
    }
}
