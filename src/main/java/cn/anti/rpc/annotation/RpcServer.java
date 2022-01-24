package cn.anti.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 被此注解标记的服务可提供远程RPC访问功能
 * @author zhuyusheng
 * @date 2020/7/15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcServer {
    String name() default "";
    int version() default 1;
}
