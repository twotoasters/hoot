package com.twotoasters.android.hoot.oauth;

public class Token {

	private String access_token = null;
	private String user_secret = null;
	
	public Token(){
	}
	
	public Token(String access_token, String user_secret){
		this.access_token = access_token;
		this.user_secret = user_secret;
	}
	
	public String getAccess_token() {
		return access_token;
	}
	
	public String getUser_secret() {
		return user_secret;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public void setUser_secret(String user_secret) {
		this.user_secret = user_secret;
	}
		
}
