package cn.anti.rpc.client.serializeProtocol;

import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;

/**
 * 根据不同的序列化协议
 * 编组请求,解组请求。编组响应,解组响应
 * @author zhuyusheng
 * @date 2021/12/24
 */
public interface SerializeProtocol {

    /**
     * 编组请求
     * @param request 请求对象
     * @return
     * @throws Exception
     */
    byte[] marshallingRequest(RpcRequest request) throws Exception;

    /**
     * 解组请求
     * @param reqData 请求字节数组
     * @return
     * @throws Exception
     */
    RpcRequest unmarshallRequest(byte[] reqData) throws Exception;

    /**
     * 编组响应
     * @param response 响应对象
     * @return
     * @throws Exception
     */
    byte[] marshallingResponse(RpcResponse response) throws Exception;

    /**
     * 解组响应
     * @param resData 响应字节数组
     * @return
     * @throws Exception
     */
    RpcResponse unmarshallResponse(byte[] resData) throws Exception;
}
