package com.netease.commons.web.binder.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by hzgongyefeng on 2016/10/8.
 */
public class RequestAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestAttribute.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		RequestAttribute annotation = parameter.getParameterAnnotation(RequestAttribute.class);
		return request.getAttribute(annotation.value());
	}
}
