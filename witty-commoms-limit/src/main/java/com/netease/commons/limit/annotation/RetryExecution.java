package com.netease.commons.limit.annotation;

import java.lang.annotation.*;

/**
 * @author binbinli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface RetryExecution {
    int retryTimes() default 1;
}