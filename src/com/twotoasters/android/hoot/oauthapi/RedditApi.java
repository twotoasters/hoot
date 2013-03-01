package com.twotoasters.android.hoot.oauthapi;

import java.util.regex.Pattern;

public class RedditApi extends OAuthApi {

	
	private static final String AUTHORIZE_URL = "https://ssl.reddit.com/api/v1/authorize";
	private static final String ACCESS_TOKEN_RESOURCE = "https://ssl.reddit.com/api/v1/access_token";
	public static final Pattern CODE_REGEX = Pattern.compile("code=([^&]*)");
	private static final String OAUTH_VERSION = "1.0";
	
	
	@Override
	public String getAuthorizeUrl() {
		return AUTHORIZE_URL;
	}
	@Override
	public String getRequestTokenResource() {
		return null;
	}
	@Override
	public String getAccessTokenResource() {
		return ACCESS_TOKEN_RESOURCE;
	}
	@Override
	public String getOauthVersion() {
		return OAUTH_VERSION;
	}
	
	
	
}
