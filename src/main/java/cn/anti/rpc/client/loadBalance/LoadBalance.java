package cn.anti.rpc.client.loadBalance;

import cn.anti.rpc.model.ServiceUrl;

import java.util.List;

/**
 * 负载均衡接口
 * @author zhuyusheng
 * @date 2021/12/24
 */
public interface LoadBalance {

    /**
     * 根据负载均衡策略获取服务节点
     * @param serviceUrlList
     * @return
     */
    ServiceUrl getOne(List<ServiceUrl> serviceUrlList);
}
