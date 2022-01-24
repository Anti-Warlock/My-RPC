package cn.anti.rpc.client.net;

import cn.anti.rpc.client.serializeProtocol.SerializeProtocol;
import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;
import cn.anti.rpc.model.ServiceUrl;

/**
 * 网络请求客户端
 * @author zhuyusheng
 * @date 2022/1/3
 */
public interface NetClient {

    /**
     * 发送请求
     * @param request
     * @param serviceUrl
     * @param serializeProtocol
     * @return
     */
    RpcResponse sendRequest(RpcRequest request, ServiceUrl serviceUrl, SerializeProtocol serializeProtocol);
}
