package cn.anti.rpc.annotation;

import java.lang.annotation.*;

/**
 * 该注解表示可注入远程服务
 * @author zhuyusheng
 * @date 2021/12/24
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcClient {
}
