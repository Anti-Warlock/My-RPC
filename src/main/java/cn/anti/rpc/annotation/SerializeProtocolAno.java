package cn.anti.rpc.annotation;

import java.lang.annotation.*;

/**
 * 序列化协议注解
 * @author zhuyusheng
 * @date 2021/12/24
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SerializeProtocolAno {

    String value() default "";
}
