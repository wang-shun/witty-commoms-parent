/*
 * Copyright 2010-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.commons.web.mobile;
import com.netease.commons.web.util.HttpUtils;
import org.springframework.mobile.device.switcher.SiteUrlFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * copy from org.springframework.mobile.device.switcher.StandardSiteUrlFactory
 */
public class ComicSiteUrlFactory implements SiteUrlFactory {
	
	private final String serverName;

	private PathMatcher pathMatcher = new AntPathMatcher();
	
	private List<String> swtichUrlPatterns;
	
	/**
	 * Creates a new {@link ComicSiteUrlFactory}.
	 * @param serverName the server name
	 */
	public ComicSiteUrlFactory(String serverName) {
		this.serverName = serverName;
	}

	public boolean isRequestForSite(HttpServletRequest request) {
		return serverName.equals(request.getServerName());
	}

	public String createSiteUrl(HttpServletRequest request) {
		return createSiteUrlInternal(request, this.serverName, request.getRequestURI());
	}
	
	public void setSwtichUrlPatterns(List<String> swtichUrlPatterns) {
		this.swtichUrlPatterns = swtichUrlPatterns;
	}
	
//	/**
//	 * Returns the HTTP port specified on the given request if it's a non-standard port.
//	 * The port is considered non-standard if it's not port 80 for insecure request and not
//	 * port 443 of secure requests.
//	 * @param request the <code>HttpServletRequest</code> to check for a non-standard port.
//	 * @return the HTTP port specified on the given request if it's a non-standard port, <code>null<code> otherwise
//	 */
//	protected String optionalPort(HttpServletRequest request) {
//		if ("http".equals(request.getScheme()) && request.getServerPort() != 80 || "https".equals(request.getScheme())
//				&& request.getServerPort() != 443) {
//			return ":" + request.getServerPort();
//		} else {
//			return null;
//		}
//	}
	
	protected String createSiteUrlInternal(HttpServletRequest request, String serverName, String path) {
		StringBuilder builder = new StringBuilder();
		builder.append(HttpUtils.getRequestScheme(request)).append("://").append(serverName);
//		String optionalPort = optionalPort(request);
//		if (optionalPort != null) {
//			builder.append(optionalPort);
//		}

		if (canSwtich(path)) {
			builder.append(path);
		} else {
			builder.append("/");
		}
		
		if (request.getQueryString() != null) {		
			builder.append('?').append(request.getQueryString());
		}
		return builder.toString();
	}
	
	protected boolean canSwtich(String path) {
		if (swtichUrlPatterns != null) {
			for (String urlPattern : swtichUrlPatterns) {
				if (pathMatcher.match(urlPattern, path)) {
					// 这个url可以切换
					return true;
				}
			}
		}

		return false;
	}
}
