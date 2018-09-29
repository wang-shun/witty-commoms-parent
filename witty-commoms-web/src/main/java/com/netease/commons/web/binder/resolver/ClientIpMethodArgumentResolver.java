package com.netease.commons.web.binder.resolver;

import com.netease.commons.web.binder.annotation.ClientIp;
import com.netease.commons.web.util.HttpUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

public class ClientIpMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ClientIp.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String ip = HttpUtils.getRemoteIP(request);
        if (parameter.getParameterType().equals(String.class)) {
			return ip;
		} else if (parameter.getParameterType().equals(InetAddress.class)) {
			return InetAddress.getByName(ip);
		}
        return null;
    }
}
