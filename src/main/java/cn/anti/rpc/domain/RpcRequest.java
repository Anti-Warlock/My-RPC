package cn.anti.rpc.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhuyusheng
 * @date 2021/7/15
 */
@Data
public class RpcRequest implements Serializable {

    /**
     * 服务类名
     */
    private String className;

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
}
