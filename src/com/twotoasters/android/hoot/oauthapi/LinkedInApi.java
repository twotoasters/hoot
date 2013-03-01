package com.twotoasters.android.hoot.oauthapi;

import java.util.regex.Pattern;

public class LinkedInApi extends OAuthApi {

	private static final String AUTHORIZE_URL = "https://www.linkedin.com/uas/oauth2/authorization";
	//private static final String REQUEST_TOKEN_RESOURCE = "https://api.twitter.com/oauth/request_token";
	private static final String ACCESS_TOKEN_RESOURCE = "https://api.linkedin.com/uas/oauth2/accessToken";
	private static final String OAUTH_VERSION = "1.0";
	public static final Pattern CODE_REGEX = Pattern.compile("code=([^&]*)");
	
	
	public LinkedInApi(){
		
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
