package cn.anti.rpc.config;

import cn.anti.rpc.annotation.LoadBalanceAno;
import cn.anti.rpc.annotation.SerializeProtocolAno;
import cn.anti.rpc.client.ClientProxyFactory;
import cn.anti.rpc.client.loadBalance.LoadBalance;
import cn.anti.rpc.client.net.NettyClient;
import cn.anti.rpc.client.serializeProtocol.SerializeProtocol;
import cn.anti.rpc.exception.RpcException;
import cn.anti.rpc.processor.RpcStarter;
import cn.anti.rpc.properties.RpcConfig;
import cn.anti.rpc.registry.ServiceRegistry;
import cn.anti.rpc.registry.nacos.NacosRegistry;
import cn.anti.rpc.registry.zookeeper.ZookeeperRegistry;
import cn.anti.rpc.server.NettyNettyServer;
import cn.anti.rpc.server.RequestHandler;
import cn.anti.rpc.server.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Rpc自动配置类,注入需要使用的Bean
 * @author zhuyusheng
 * @date 2022/1/11
 */
@Configuration
@EnableConfigurationProperties(RpcConfig.class)
public class RpcAutoConfiguration {

    @Bean
    public RpcConfig rpcConfig(){
        return new RpcConfig();
    }

    @Bean
    public ServiceRegistry serviceRegistry(@Autowired RpcConfig rpcConfig){
        switch (rpcConfig.getRegisterType()){
            case "zk":
                return new ZookeeperRegistry(rpcConfig.getAddress(),rpcConfig.getServerPort(),rpcConfig.getProtocol(),rpcConfig.getWeight());
            case "nacos":
                return new NacosRegistry(rpcConfig.getAddress(),rpcConfig.getServerPort(),rpcConfig.getProtocol(),rpcConfig.getWeight());
            default:
                throw new RpcException("invalid register type config!");
        }
    }

    @Bean
    public RequestHandler requestHandler(@Autowired ServiceRegistry serviceRegistry,@Autowired RpcConfig rpcConfig){
        return new RequestHandler(getProtocol(rpcConfig.getProtocol()),serviceRegistry);
    }

    @Bean
    public NettyServer rpcServer(@Autowired RpcConfig rpcConfig, @Autowired RequestHandler requestHandler){
        return new NettyNettyServer(rpcConfig.getServerPort(),rpcConfig.getProtocol(),requestHandler);
    }

    @Bean
    public ClientProxyFactory proxyFactory(@Autowired ServiceRegistry serviceRegistry,@Autowired RpcConfig rpcConfig){
        //1.新建客户端代理工厂
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        //2.设置服务发现者
        clientProxyFactory.setServiceRegistry(serviceRegistry);
        //3.设置支持的协议
        Map<String, SerializeProtocol> supportProtocol = getSupportProtocol();
        clientProxyFactory.setProtocolMap(supportProtocol);
        //4.设置负载均衡算法
        clientProxyFactory.setLoadBalance(getLoadBalance(rpcConfig.getLoadBalance()));
        //4.设置网络通信客户端
        clientProxyFactory.setNetClient(new NettyClient());
        return clientProxyFactory;
    }

    /**
     * 使用spi返回符合配置的序列化算法
     * @param name
     * @return
     */
    private SerializeProtocol getProtocol(String name){
        ServiceLoader<SerializeProtocol> loader = ServiceLoader.load(SerializeProtocol.class);
        Iterator<SerializeProtocol> iterator = loader.iterator();
        while (iterator.hasNext()){
            SerializeProtocol serializeProtocol = iterator.next();
            SerializeProtocolAno annotation = serializeProtocol.getClass().getAnnotation(SerializeProtocolAno.class);
            Assert.notNull(annotation, "serialize protocol can not be empty!");
            if(name.equals(annotation.value())){
                return serializeProtocol;
            }
        }
        throw new RpcException("invalid serialize protocol config!");
    }

    /**
     * 使用spi返回所有支持的序列化算法
     * @return
     */
    private Map<String,SerializeProtocol> getSupportProtocol(){
        Map<String,SerializeProtocol> supportProtocolMap = new HashMap<>();
        ServiceLoader<SerializeProtocol> load = ServiceLoader.load(SerializeProtocol.class);
        Iterator<SerializeProtocol> iterator = load.iterator();
        while (iterator.hasNext()){
            SerializeProtocol serializeProtocol = iterator.next();
            SerializeProtocolAno annotation = serializeProtocol.getClass().getAnnotation(SerializeProtocolAno.class);
            Assert.notNull(annotation, "serialize protocol can not be empty!");
            supportProtocolMap.put(annotation.value(),serializeProtocol);
        }
        return supportProtocolMap;
    }

    /**
     * 使用spi匹配符合配置的负载均衡算法
     * @param name
     * @return
     */
    private LoadBalance getLoadBalance(String name){
        ServiceLoader<LoadBalance> load = ServiceLoader.load(LoadBalance.class);
        Iterator<LoadBalance> iterator = load.iterator();
        while (iterator.hasNext()){
            LoadBalance loadBalance = iterator.next();
            LoadBalanceAno annotation = loadBalance.getClass().getAnnotation(LoadBalanceAno.class);
            Assert.notNull(annotation,"load balance can not be empty!");
            if(name.equals(annotation.value())){
                return loadBalance;
            }
        }
        throw new RpcException("invalid load balance config!");
    }

    @Bean
    public RpcStarter rpcStarter(@Autowired ClientProxyFactory clientProxyFactory,
                                 @Autowired ServiceRegistry serviceRegistry,
                                   @Autowired NettyServer nettyServer) {
        return new RpcStarter(clientProxyFactory, serviceRegistry, nettyServer);
    }
}
