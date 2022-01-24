package cn.anti.rpc.client;

import cn.anti.rpc.client.loadBalance.LoadBalance;
import cn.anti.rpc.client.net.NetClient;
import cn.anti.rpc.client.serializeProtocol.SerializeProtocol;
import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;
import cn.anti.rpc.exception.RpcException;
import cn.anti.rpc.model.ServiceUrl;
import cn.anti.rpc.registry.ServiceRegistry;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端代理工厂:创建远程服务代理类
 * 封装编组请求、发送请求、编组响应
 * @author zhuyusheng
 * @date 2022/1/14
 */
@Data
public class ClientProxyFactory {

    private static Logger logger = LoggerFactory.getLogger(ClientProxyFactory.class);

    private ServiceRegistry serviceRegistry;

    private NetClient netClient;

    private Map<String, SerializeProtocol> protocolMap;

    private Map<Class<?>,Object> objectCache = new HashMap<>();

    private LoadBalance loadBalance;

    public <T> T getProxy(Class<T> clazz){
        return (T) objectCache.computeIfAbsent(clazz,clz ->
                Proxy.newProxyInstance(clz.getClassLoader(),new Class[]{clz},new ClientInvocationHandler(clz)));
    }

    private class ClientInvocationHandler implements InvocationHandler{

        private Class<?> clazz;

        public ClientInvocationHandler(Class<?> clazz){
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")) {
                return proxy.toString();
            }

            if (method.getName().equals("hashCode")) {
                return 0;
            }
            //1.获取服务信息
            String serviceName = clazz.getName();
            List<ServiceUrl> serviceList = serviceRegistry.getServiceList(serviceName);
            //负载均衡
            ServiceUrl serviceUrl = loadBalance.getOne(serviceList);
            //2.构造Request对象
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestId(UUID.randomUUID().toString());
            rpcRequest.setServiceName(serviceUrl.getName());
            rpcRequest.setMethodName(method.getName());
            rpcRequest.setParameters(args);
            rpcRequest.setParameterTypes(method.getParameterTypes());
            //3.协议层编组
            SerializeProtocol serializeProtocol = protocolMap.get(serviceUrl.getProtocol());
            //4.发送请求
            logger.debug("NetClient:{}",netClient.toString());
            logger.debug("RpcRequest:{}",rpcRequest.toString());
            logger.debug("ServiceUrl:{}",serviceUrl.toString());
            logger.debug("SerializeProtocol:{}",serializeProtocol.toString());
            RpcResponse response = netClient.sendRequest(rpcRequest, serviceUrl, serializeProtocol);
            if (response == null){
                throw new RpcException("the response is null");
            }
            if (response.getException() != null) {
                return response.getException();
            }
            return response.getData();
        }
    }
}
