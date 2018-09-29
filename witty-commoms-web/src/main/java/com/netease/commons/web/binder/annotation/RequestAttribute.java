package com.netease.commons.web.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于从请求中获取属性
 * Created by hzgongyefeng on 2016/10/8.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface RequestAttribute {
	String value();
}
