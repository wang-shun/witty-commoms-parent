package com.netease.commons.limit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author binbinli
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedRateLimiterMethod {

    /**
     * 限流的QPS值,必填
     *
     * @return
     */
    long qps();

    /**
     * 请求被限流后的降级处理方法(该方法签名需要和被限流的方法保持一致)
     *
     * @return
     */
    String fallBackMethod() default "";

    /**
     * 设置开启单个用户访问频率控制，单位秒，默认禁用,缺省为限流总并发数/请求数据
     *
     * @return
     */
    String uk() default "";

}
