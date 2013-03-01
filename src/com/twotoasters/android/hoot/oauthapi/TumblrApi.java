package com.twotoasters.android.hoot.oauthapi;

public class TumblrApi extends OAuthApi {

	private static final String AUTHORIZE_URL = "http://www.tumblr.com/oauth/authorize?oauth_token=%s&oauth_callback=%s";
	private static final String REQUEST_TOKEN_RESOURCE = "http://www.tumblr.com/oauth/request_token";
	private static final String ACCESS_TOKEN_RESOURCE = "http://www.tumblr.com/oauth/access_token";
	private static final String OAUTH_VERSION = "1.0";
	
	
	public TumblrApi(){
		
	}
	
	@Override
	public String getAuthorizeUrl() {
		return AUTHORIZE_URL;
	}
	
	@Override
	public String getRequestTokenResource() {
		return REQUEST_TOKEN_RESOURCE;
	}
	public String getAccessTokenResource() {
		return ACCESS_TOKEN_RESOURCE;
	}
	public String getOauthVersion() {
		return OAUTH_VERSION;
	}
	
}