package com.twotoasters.android.hoot.oauthapi;

public class TwitterApi extends OAuthApi {

	private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize?oauth_token=%s";
	private static final String REQUEST_TOKEN_RESOURCE = "https://api.twitter.com/oauth/request_token";
	private static final String ACCESS_TOKEN_RESOURCE = "https://api.twitter.com/oauth/access_token";
	private static final String OAUTH_VERSION = "1.0";
	
	
	public TwitterApi(){
		
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
