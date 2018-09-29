/**
 * 
 */
package com.netease.commons.web.mobile;


import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author hzgongyefeng
 *
 */
public class EnableComicSiteSwitcherHandlerInterceptor extends HandlerInterceptorAdapter {

	private Set<String> enableDomains = new HashSet<>();
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		try {
			URL url = new URL(request.getRequestURL().toString());
			if (enableDomains.contains(url.getHost().toLowerCase())) {
				request.setAttribute(ComicSiteSwitcherHandlerInterceptor.ENABLE_SWITCH, Boolean.TRUE);
			}
		} catch (Exception e) {
			// nothing
		}
		return super.preHandle(request, response, handler);
	}
	
	public void setEnableDomains(Set<String> enableDomains) {
		this.enableDomains = enableDomains;
	}
}
