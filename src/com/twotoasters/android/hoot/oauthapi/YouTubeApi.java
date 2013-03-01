package com.twotoasters.android.hoot.oauthapi;


import java.util.regex.Pattern;

public class YouTubeApi extends OAuthApi {

	private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth";
	private static final String REQUEST_TOKEN_RESOURCE = "https://accounts.google.com/o/oauth2/token";
	private static final String ACCESS_TOKEN_RESOURCE = "https://accounts.google.com/o/oauth2/token";
	private static final String OAUTH_VERSION = "1.0";
	public static final Pattern CODE_REGEX = Pattern.compile("code=([^&]*)");
	public static final Pattern ACCESS_TOKEN_REGEX = Pattern.compile("access_token=([^&]*)");
	
	
	public YouTubeApi(){
		
	}
	
	@Override
	public String getAuthorizeUrl() {
		return AUTHORIZE_URL;
	}
	
	@Override
	public String getRequestTokenResource() {
		return null;
	}
	public String getAccessTokenResource() {
		return ACCESS_TOKEN_RESOURCE;
	}
	public String getOauthVersion() {
		return OAUTH_VERSION;
	}
	
	
}
