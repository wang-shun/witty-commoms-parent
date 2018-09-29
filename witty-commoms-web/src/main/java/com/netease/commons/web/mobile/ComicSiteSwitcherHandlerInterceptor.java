/**
 * 
 */
package com.netease.commons.web.mobile;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.mobile.device.site.CookieSitePreferenceRepository;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;
import org.springframework.mobile.device.switcher.SiteUrlFactory;
import org.springframework.mobile.device.util.ResolverUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 站点切换
 * @author hzgongyefeng
 *
 */
public class ComicSiteSwitcherHandlerInterceptor extends HandlerInterceptorAdapter {
	
	/**
	 * 启用开关，不设置默认认为是true
	 */
	public final static String ENABLE_SWITCH =  ComicSiteSwitcherHandlerInterceptor.class + ".ENABLE_SWITCH";
	
	private SiteUrlFactory siteUrlFactory;
	private SitePreferenceHandler sitePreferenceHandler;
	private boolean mobileServer;
	private boolean tabletIsMobile;
	
	public ComicSiteSwitcherHandlerInterceptor(SiteUrlFactory siteUrlFactory,
			SitePreferenceHandler sitePreferenceHandler, boolean mobileServer, boolean tabletIsMobile) {
		this.siteUrlFactory = siteUrlFactory;
		this.sitePreferenceHandler = sitePreferenceHandler;
		this.mobileServer = mobileServer;
		this.tabletIsMobile = tabletIsMobile;
	}

	public static ComicSiteSwitcherHandlerInterceptor standard(
			String otherServerName, 
			String cookieDomain, 
			boolean mobileServer,
			boolean tabletIsMobile,
			List<String> swtichUrlPatterns) {
		return new ComicSiteSwitcherHandlerInterceptor(
				createSiteUrlFactory(otherServerName, swtichUrlPatterns),
				new ComicSitePreferenceHandler(createSitePreferenceRepository(cookieDomain)),
				mobileServer, tabletIsMobile);
	}
	
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		// 只有在设置了 Boolean.True的情况下才进行自动跳转
		Object swtich = request.getAttribute(ENABLE_SWITCH);
		if (swtich instanceof Boolean && (Boolean)swtich) {
			
			SitePreference sitePreference = sitePreferenceHandler.handleSitePreference(request, response);
			Device device = DeviceUtils.getRequiredCurrentDevice(request);
			
			boolean targetMobileServer = mobileServer;
			if (ResolverUtils.isTablet(device, sitePreference)) {
				if (tabletIsMobile) {
					targetMobileServer = true;
				} else {
					targetMobileServer = false;
				}
			} else if (ResolverUtils.isMobile(device, sitePreference)) {
				targetMobileServer = true;
			} else if (ResolverUtils.isNormal(device, sitePreference)) {
				targetMobileServer = false;
			}
			
			// 如果目标和当前站点不是一个类型则需要跳转
			if (targetMobileServer != mobileServer) {
				response.sendRedirect(response.encodeRedirectURL(siteUrlFactory.createSiteUrl(request)));
				return false;
			}
		}
		
		return true;
	}

	private static CookieSitePreferenceRepository createSitePreferenceRepository(String cookieDomain) {
		CookieSitePreferenceRepository repo = new CookieSitePreferenceRepository(cookieDomain);
		repo.setCookieName("nets_comic_site");
		return repo;
	}
	
	private static ComicSiteUrlFactory createSiteUrlFactory(String serverName, List<String> swtichUrlPatterns) {
		ComicSiteUrlFactory f = new ComicSiteUrlFactory(serverName);
		f.setSwtichUrlPatterns(swtichUrlPatterns);
		return f;
	}
	
}
