package cn.anti.rpc.server;

import cn.anti.rpc.client.serializeProtocol.SerializeProtocol;
import cn.anti.rpc.constant.RpcStatusEnum;
import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;
import cn.anti.rpc.model.ServiceMetaData;
import cn.anti.rpc.registry.ServiceRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Rpc请求处理者，解组请求,编组响应
 * @author zhuyusheng
 * @date 2022/1/11
 */
@Data
@AllArgsConstructor
public class RequestHandler {

    private static Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private SerializeProtocol protocol;

    private ServiceRegistry serviceRegistry;

    public byte[] handRequest(byte[] reqData) throws Exception{
        //1.解组消息
        RpcRequest rpcRequest = this.protocol.unmarshallRequest(reqData);
        logger.debug("RpcRequest:{}",rpcRequest);
        //2.获取对应的服务元数据
        ServiceMetaData serviceMetaData = serviceRegistry.getServiceMetaData(rpcRequest.getServiceName());
        RpcResponse response = null;
        if(null == serviceMetaData){
            response = new RpcResponse(RpcStatusEnum.NOT_FOUND);
        }else{
            try {
                //3.反射调用方法
                Method method = serviceMetaData.getClazz().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
                Object returnVal = method.invoke(serviceMetaData.getObj(), rpcRequest.getParameters());
                response = new RpcResponse(RpcStatusEnum.SUCCESS);
                response.setData(returnVal);
            }catch (Exception e){
                response = new RpcResponse(RpcStatusEnum.ERROR);
                response.setException(e);
            }
        }
        response.setRequestId(rpcRequest.getRequestId());
        logger.debug("RpcResponse:{}",response);
        //4.编组响应并返回
        return this.protocol.marshallingResponse(response);
    }
}
