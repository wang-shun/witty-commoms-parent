package com.netease.commons.web.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates that the remote ip should be bound to a web request parameter.
 * 
 * @author [[mailto:hzmaoyinjie@corp.netease.com][Mao Yinjie]]
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface ClientIp {
    //
}
