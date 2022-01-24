package cn.anti.rpc.domain;

import cn.anti.rpc.constant.RpcStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Rpc响应对象
 * @author zhuyusheng
 * @date 2021/7/15
 */
@Data
public class RpcResponse implements Serializable {

    /**
     * 请求ID
     */
    private String requestId;

    private Map<String,String> headers = new HashMap<>();
    /**
     * 异常
     */
    private Exception exception;
    /**
     * 状态码
     */
    private RpcStatusEnum rpcStatus;
    /**
     * 返回数据
     */
    private Object data;

    public RpcResponse(){}

    public RpcResponse(RpcStatusEnum rpcStatus){
        this.rpcStatus = rpcStatus;
    }
}
