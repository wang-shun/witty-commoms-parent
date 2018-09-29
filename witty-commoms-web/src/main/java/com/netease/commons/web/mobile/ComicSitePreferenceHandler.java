package com.netease.commons.web.mobile;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;
import org.springframework.mobile.device.site.SitePreferenceRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ComicSitePreferenceHandler implements SitePreferenceHandler {

	private final SitePreferenceRepository sitePreferenceRepository;
	
	/**
	 * Creates a new site preference handler.
	 * @param sitePreferenceRepository the store for recording user site preference
	 */
	public ComicSitePreferenceHandler(SitePreferenceRepository sitePreferenceRepository) {
		this.sitePreferenceRepository = sitePreferenceRepository;
	}

	public SitePreference handleSitePreference(HttpServletRequest request, HttpServletResponse response) {
		SitePreference preference = getSitePreferenceQueryParameter(request);
		if (preference != null) {
			sitePreferenceRepository.saveSitePreference(preference, request, response);
		} else {
			preference = sitePreferenceRepository.loadSitePreference(request);
		}
		if (preference == null) {
			preference = getDefaultSitePreferenceForDevice(DeviceUtils.getCurrentDevice(request));
		}
		if (preference != null) {
			request.setAttribute(CURRENT_SITE_PREFERENCE_ATTRIBUTE, preference);
		}
		return preference;
	}
	
	// impl helpers
	
	private SitePreference getSitePreferenceQueryParameter(HttpServletRequest request) {
		String string = request.getParameter(SITE_PREFERENCE_PARAMETER);
		try {
			return string != null && string.length() > 0 ? SitePreference.valueOf(string.toUpperCase()) : null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private SitePreference getDefaultSitePreferenceForDevice(Device device) {
		if (device == null) {
			return null;
		}
		if (device.isMobile()) {
			return SitePreference.MOBILE;
		}
		else if (device.isTablet()) {
			return SitePreference.TABLET;
		}
		else {
			return SitePreference.NORMAL;
		}
		
	}
	
	public static final String SITE_PREFERENCE_PARAMETER = "comic_site";

}
