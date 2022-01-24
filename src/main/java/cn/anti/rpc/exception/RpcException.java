package cn.anti.rpc.exception;

/**
 * Rpc异常
 * @author zhuyusheng
 * @date 2022/1/3
 */
public class RpcException extends RuntimeException{

    public RpcException(String msg){
        super(msg);
    }
}
