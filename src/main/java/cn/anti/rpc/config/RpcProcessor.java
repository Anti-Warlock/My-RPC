package cn.anti.rpc.config;

import cn.anti.rpc.annotation.RpcServer;
import cn.anti.rpc.model.ServiceMetaData;
import cn.anti.rpc.registry.ServiceRegistry;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;
import java.util.Objects;

/**
 * Rpc处理器
 * @author zhuyusheng
 * @date 2021/7/15
 */
@Slf4j
public class RpcProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private ServiceRegistry serviceRegistry;

    /**
     * 服务端启动流程如下:
     * 订阅ContextRefreshedEvent事件 --> 解析所有带RpcServer的Bean --> 封装服务数据 --> 注册服务 --> 监听客户端请求
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //Spring容器初始化完毕后,会收到一个事件通知,只需监控顶层容器即可
        if(Objects.isNull(event.getApplicationContext().getParent())){
            ApplicationContext context = event.getApplicationContext();
            //注册服务
            registerService(context);
            //订阅服务

        }
    }

    private void registerService(ApplicationContext context){
        Map<String, Object> beansMap = context.getBeansWithAnnotation(RpcServer.class);
        if(MapUtil.isNotEmpty(beansMap)){
            for (Object obj:beansMap.values()){
                Class<?> clazz = obj.getClass();
                try {
                    RpcServer rpcServer = clazz.getAnnotation(RpcServer.class);
                    if(StrUtil.isBlank(rpcServer.name())){
                        log.error("服务名称未配置,请先配置服务名!");
                        throw new RuntimeException(
                                "RpcServer Class:" + clazz.getName() + "need config service name!"
                        );
                    }
                    ServiceMetaData serviceMetaData = new ServiceMetaData();
                    serviceMetaData.setServiceName(rpcServer.name());
                    serviceMetaData.setVersion(rpcServer.version());
                    serviceMetaData.setClazz(clazz);
                    serviceRegistry.register(serviceMetaData);
                } catch (Exception e) {
                    log.error("RpcServer Class:{},register error:{}",clazz.getName(),e);
                    throw new RuntimeException(
                            "RpcServer Class:" + clazz.getName() + "register error!"
                    );
                }
            }

        }
    }
}
