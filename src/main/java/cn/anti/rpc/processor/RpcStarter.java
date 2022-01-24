package cn.anti.rpc.processor;

import cn.anti.rpc.annotation.RpcClient;
import cn.anti.rpc.annotation.RpcServer;
import cn.anti.rpc.client.ClientProxyFactory;
import cn.anti.rpc.model.ServiceMetaData;
import cn.anti.rpc.registry.ServiceRegistry;
import cn.anti.rpc.registry.cache.ServerServiceMetadataCache;
import cn.anti.rpc.server.NettyServer;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * Rpc处理器,支持服务启动暴露,自动注入Service
 * @author zhuyusheng
 * @date 2021/7/15
 */
@Slf4j
public class RpcStarter implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger logger = LoggerFactory.getLogger(RpcStarter.class);

    private ClientProxyFactory clientProxyFactory;

    private ServiceRegistry serviceRegistry;

    private NettyServer nettyServer;

    public RpcStarter(ClientProxyFactory clientProxyFactory,ServiceRegistry serviceRegistry,NettyServer nettyServer){
        this.clientProxyFactory = clientProxyFactory;
        this.serviceRegistry = serviceRegistry;
        this.nettyServer = nettyServer;
    }


    /**
     * 服务端启动流程如下:
     * 订阅ContextRefreshedEvent事件 --> 解析所有带RpcServer的Bean --> 封装服务数据 --> 注册服务 --> 监听客户端请求
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //Spring容器初始化完毕后,会收到一个事件通知,只需监控顶层容器即可
        if(Objects.isNull(event.getApplicationContext().getParent())){
            ApplicationContext context = event.getApplicationContext();
            //开启服务
            registerService(context);
            //注入服务
            injectService(context);
            //订阅服务
//            subscribeService(context);
        }
    }

    private void registerService(ApplicationContext context){
        Map<String, Object> beansMap = context.getBeansWithAnnotation(RpcServer.class);
        if(MapUtil.isNotEmpty(beansMap)){
            boolean startServerFlag = true;
            for (Object obj:beansMap.values()){
                try {
                    Class<?> clazz = obj.getClass();
                    Class<?>[] interfaces = clazz.getInterfaces();
                    ServiceMetaData serviceMetaData = null;
                    if (interfaces.length != 1){
                        RpcServer rpcServer = clazz.getAnnotation(RpcServer.class);
                        if(StrUtil.isBlank(rpcServer.name())){
                            startServerFlag = false;
                            log.error("服务名称未配置,请先配置服务名!");
                            throw new RuntimeException(
                                    "RpcServer Class:" + clazz.getName() + "need config service name!"
                            );
                        }
                        serviceMetaData = new ServiceMetaData();
                        serviceMetaData.setServiceName(rpcServer.name());
                        serviceMetaData.setClazz(clazz);
                        serviceMetaData.setObj(obj);
                    }else {
                        Class<?> supperClass = interfaces[0];
                        serviceMetaData = new ServiceMetaData();
                        serviceMetaData.setServiceName(supperClass.getName());
                        serviceMetaData.setClazz(supperClass);
                        serviceMetaData.setObj(obj);
                    }
                    serviceRegistry.register(serviceMetaData);
                } catch (Exception e) {
                    log.error("RpcServer Class:{},register error:{}",e);
                }
            }
            //启动Netty客户端监听客户端的请求
            if(startServerFlag){
                nettyServer.start();
            }
        }
    }

    private void injectService(ApplicationContext context){
        String[] names = context.getBeanDefinitionNames();
        for (String name:names){
            Class<?> clazz = context.getType(name);
            if(Objects.isNull(clazz)){
                continue;
            }
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field:declaredFields){
                //找出标记了RpcClient注解的属性
                RpcClient rpcClient = field.getAnnotation(RpcClient.class);
                if(null == rpcClient){
                    continue;
                }
                Class<?> fieldClass = field.getType();
                Object object = context.getBean(name);
                field.setAccessible(true);
                try {
                    field.set(object,clientProxyFactory.getProxy(fieldClass));
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
                ServerServiceMetadataCache.SERVICE_CLASS_NAMES.add(fieldClass.getName());
            }
        }
        ServerServiceMetadataCache.SERVICE_CLASS_NAMES.forEach(name -> {
            try {
                serviceRegistry.subscribeOnServiceChange(name);
                logger.info("subscribe service successfully!");
            } catch (NacosException e) {
                logger.error("subscribe service failed!");
            }
        });
    }

//    private void subscribeService(ApplicationContext context){
//        Map<String, Object> beansMap = context.getBeansWithAnnotation(RpcServer.class);
//        if(MapUtil.isNotEmpty(beansMap)){
//            for (Object obj:beansMap.values()){
//                Class<?> clazz = obj.getClass();
//                try {
//                    RpcServer rpcServer = clazz.getAnnotation(RpcServer.class);
//                    if(StrUtil.isBlank(rpcServer.name())){
//                        log.error("服务名称未配置,请先配置服务名!");
//                        throw new RuntimeException(
//                                "RpcServer Class:" + clazz.getName() + "need config service name!"
//                        );
//                    }
//                    serviceRegistry.subscribeOnServiceChange(rpcServer.name());
//                } catch (Exception e) {
//                    log.error("RpcServer Class:{},subscribe error:{}",clazz.getName(),e);
//                    throw new RuntimeException(
//                            "RpcServer Class:" + clazz.getName() + "subscribe error!"
//                    );
//                }
//            }
//        }
//    }
}
