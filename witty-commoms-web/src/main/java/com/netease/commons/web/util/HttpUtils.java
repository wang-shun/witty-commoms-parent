/**
 * 
 */
package com.netease.commons.web.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;


/**
 * @author hzgongyefeng
 *
 */
public abstract class HttpUtils {
	
    /**
     * 获取客户端真实ip
     */
	public static String getRemoteIP(HttpServletRequest request) {
		String uip = request.getHeader("X-From-IP");
		if (StringUtils.isBlank(uip)) {
			uip = request.getHeader("X-Real-IP");
		}

		if (StringUtils.isBlank(uip)) {
			uip = request.getHeader("X-Forwarded-For");
			if (uip != null) {
				String[] ips = uip.split(",");
				if (ips.length > 1)
					uip = ips[0];
			}
		}

		if (StringUtils.isBlank(uip)) {
			uip = request.getRemoteAddr();
		}

		return StringUtils.trimToEmpty(uip);
	}

	public static String getRequestScheme(HttpServletRequest request) {
		String scheme = request.getScheme();
		String protocolHeader = request.getHeader("X-Forwarded-Proto");
		if (org.springframework.util.StringUtils.hasText(protocolHeader)) {
			String[] protocols = org.springframework.util.StringUtils.commaDelimitedListToStringArray(protocolHeader);
			scheme = protocols[0];
		}
		return scheme;
	}

	public static String getRequestHost(HttpServletRequest request) {
		String host = request.getHeader("Host");

		String hostHeader = request.getHeader("X-Forwarded-Host");
		if (org.springframework.util.StringUtils.hasText(hostHeader)) {
			// nginx代理，认为不可能存在指定的特殊端口
			String[] hosts = org.springframework.util.StringUtils.commaDelimitedListToStringArray(hostHeader);
			host = hosts[0];
		}

		return StringUtils.defaultString(host, request.getServerName());
	}

	/**
	 * 获取基本url， scheme://serverName， 最后没有 /
	 * @param request
	 * @return
	 */
	public static String getRequestBaseUrl(HttpServletRequest request) {
		String scheme = getRequestScheme(request);
		StringBuilder sb = new StringBuilder(scheme);
		sb.append("://");
		sb.append(getRequestHost(request));

		return sb.toString();
	}

	/**
	 * 获取请求的url全内容，会识别前端nginx的特殊头信息
	 * @param request
	 * @return
	 */
	public static String getRequestUrl(HttpServletRequest request) {

		StringBuilder sb = new StringBuilder(getRequestBaseUrl(request));
		sb.append(request.getRequestURI());

		if (StringUtils.isNotBlank(request.getQueryString())) {
			sb.append("?");
			sb.append(request.getQueryString());
		}

		return sb.toString();
	}

	/**
	 * 判断是否是ajax请求
	 * @param request
	 * @return
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {

		return StringUtils.endsWith(request.getRequestURI(), ".json") ||
				StringUtils.containsIgnoreCase(request.getContentType(), "json") ||
				StringUtils.containsIgnoreCase(request.getHeader("X-Requested-With"), "XMLHttpRequest");
	}
}
