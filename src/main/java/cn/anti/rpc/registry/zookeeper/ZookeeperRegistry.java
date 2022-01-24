package cn.anti.rpc.registry.zookeeper;

import cn.anti.rpc.constant.RpcConstant;
import cn.anti.rpc.model.ServiceMetaData;
import cn.anti.rpc.model.ServiceUrl;
import cn.anti.rpc.registry.DefaultServerRegistry;
import cn.anti.rpc.registry.ServiceRegistry;
import cn.anti.rpc.registry.cache.ServerServiceMetadataCache;
import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Zookeeper注册
 * @author zhuyusheng
 * @date 2021/7/14
 */
@Slf4j
public class ZookeeperRegistry extends DefaultServerRegistry {

    private  ZkClient zkClient;

    /**
     * zk注册中心构造
     * @param zkAddress
     */
    public ZookeeperRegistry(String zkAddress,Integer port,String protocol,Integer weight){
        zkClient = new ZkClient(zkAddress);
        this.port = port;
        this.protocol = protocol;
        this.weight = weight;
    }

    /**
     * 在zk上创建服务节点,即服务暴露
     * @param serviceUrl
     */
    private void createZookeeperServiceNode(ServiceUrl serviceUrl){
        String urlJson = JSON.toJSONString(serviceUrl);
        try {
            urlJson = URLEncoder.encode(urlJson,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("url encode error",e.getMessage());
        }
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.PATH_DELIMITER + serviceUrl.getName() + "/service";
        //如果不存在,创建服务节点(包含父级)
        if(!zkClient.exists(servicePath)){
            //服务名路径持久节点
            zkClient.createPersistent(servicePath,true);
        }
        String urlPath = servicePath + RpcConstant.PATH_DELIMITER + urlJson;
        if(zkClient.exists(urlPath)){
            //删除之前的节点
            zkClient.delete(urlPath);
        }
        //创建一个临时节点,会话失效即被清理
        zkClient.createEphemeral(urlPath);

    }

    /**
     * 服务注册
     * @param serviceMetaData
     * @throws Exception
     */
    @Override
    public void register(ServiceMetaData serviceMetaData) throws Exception {
        super.register(serviceMetaData);
        ServiceUrl serviceUrl = new ServiceUrl();
        String ipAddr = InetAddress.getLocalHost().getHostAddress();
        serviceUrl.setAddress(ipAddr + ":" + port);
        serviceUrl.setName(serviceMetaData.getClazz().getName());
        serviceUrl.setWeight(weight);
        serviceUrl.setProtocol(protocol);
        this.createZookeeperServiceNode(serviceUrl);
    }

    /**
     * 根据用户名获取服务列表
     * @param serviceName
     * @return
     */
    @Override
    public List<ServiceUrl> getServiceList(String serviceName) {
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.PATH_DELIMITER + serviceName + "/service";
        List<ServiceUrl> serviceUrlList;
        synchronized (serviceName){
            //判断缓存子节点是否为空
            if(ServerServiceMetadataCache.isEmpty(serviceName)){
                //若为空,找出子节点存入缓存
                List<String> children = zkClient.getChildren(servicePath);
                serviceUrlList = Optional.ofNullable(children).orElse(new ArrayList<>()).stream().map(str -> {
                    String dech = null;
                    try {
                        dech = URLDecoder.decode(str, RpcConstant.UTF_8);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return JSON.parseObject(dech, ServiceUrl.class);
                }).collect(Collectors.toList());
                if(CollectionUtil.isEmpty(serviceUrlList)){
                    throw new RuntimeException("没有可用的节点!");
                }
                ServerServiceMetadataCache.put(serviceName,serviceUrlList);
            }else {
                serviceUrlList = ServerServiceMetadataCache.get(serviceName);
            }
        }
        return serviceUrlList;
    }

    @Override
    public void subscribeOnServiceChange(String serviceName){
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.PATH_DELIMITER + serviceName + "/service";
        zkClient.subscribeChildChanges(servicePath, (parentPath, childList) -> {
            log.debug("Child change parentPath:[{}] -- childList:[{}]", parentPath, childList);
            //只要子节点有改动就清空缓存
            String[] arr = parentPath.split("/");
            //清空缓存
            ServerServiceMetadataCache.removeAll(arr[2]);
        });
    }
}
