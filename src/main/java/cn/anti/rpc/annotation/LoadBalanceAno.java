package cn.anti.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 负载均衡注解
 * @author zhuyusheng
 * @date 2021/12/24
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoadBalanceAno {

    String value() default "";
}
