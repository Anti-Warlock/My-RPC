package cn.anti.rpc.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuyusheng
 * @date 2021/7/15
 */
@Data
public class RpcRequest implements Serializable {

    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数名
     */
    private Object[] parameters;

    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;

    private Map<String,String> headers = new HashMap<>();
}
