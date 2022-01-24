package cn.anti.rpc.registry;

import cn.anti.rpc.exception.RpcException;
import cn.anti.rpc.model.ServiceMetaData;
import cn.anti.rpc.model.ServiceUrl;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务注册默认实现
 * @author zhuyusheng
 * @date 2022/1/15
 */
public abstract class DefaultServerRegistry implements ServiceRegistry{

    private Map<String, ServiceMetaData> serviceMap = new HashMap<>();

    /**
     * 序列化协议
     */
    protected String protocol;
    /**
     * 权重
     */
    protected Integer weight;
    /**
     * 服务端口
     */
    protected Integer port;


    @Override
    public void register(ServiceMetaData serviceMetaData) throws Exception {
        if(null == serviceMetaData){
            throw new RpcException("parameter cannot be empty!");
        }
        serviceMap.put(serviceMetaData.getServiceName(),serviceMetaData);
    }

    @Override
    public ServiceMetaData getServiceMetaData(String serviceName){
        return serviceMap.get(serviceName);
    }
}
