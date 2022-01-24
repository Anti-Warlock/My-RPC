package cn.anti.rpc.registry.cache;

import cn.anti.rpc.model.ServiceUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 服务端:服务名和服务元数据映射缓存
 * @author zhuyusheng
 * @Data 2021/12/06
 */
public class ServerServiceMetadataCache {

    /**
     * 服务端地址缓存
     * name:nodeList
     */
    private static final Map<String, List<ServiceUrl>> SERVER_MAP = new ConcurrentHashMap<>();

    /**
     * 客户端注入的远程服务service class
     */
    public static final List<String> SERVICE_CLASS_NAMES = new ArrayList<>();

    /**
     * 存入缓存
     * @param serviceName
     * @param serviceUrlList
     */
    public static void put(String serviceName,List<ServiceUrl> serviceUrlList){
        SERVER_MAP.put(serviceName,serviceUrlList);
    }

    /**
     * 移除掉某个服务下的特定临时节点
     * @param serviceName
     * @param serviceUrl
     */
    public static void remove(String serviceName,ServiceUrl serviceUrl){
        SERVER_MAP.computeIfPresent(serviceName,(key,value) ->
                value.stream().filter(val -> !val.toString().equals(serviceUrl.toString())).collect(Collectors.toList()));
    }

    /**
     * 移除该服务缓存
     * @param serviceName
     */
    public static void removeAll(String serviceName) {
        SERVER_MAP.remove(serviceName);
    }

    /**
     * 判断该服务节点映射的临时地址是否为空
     * @param serviceName
     * @return
     */
    public static boolean isEmpty(String serviceName) {
        return SERVER_MAP.get(serviceName) == null || SERVER_MAP.get(serviceName).size() == 0;
    }

    /**
     * 根据服务名称获取其下的节点列表
     * @param serviceName
     * @return
     */
    public static List<ServiceUrl> get(String serviceName) {
        return SERVER_MAP.get(serviceName);
    }

}
