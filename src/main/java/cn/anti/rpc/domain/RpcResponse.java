package cn.anti.rpc.domain;

import lombok.Data;

/**
 * Rpc响应对象
 * @author zhuyusheng
 * @date 2021/7/15
 */
@Data
public class RpcResponse {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private Object data;
}
