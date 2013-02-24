package com.twotoasters.android.hoot.oauth;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.twotoasters.android.hoot.Hoot;
import com.twotoasters.android.hoot.HootRequest;
import com.twotoasters.android.hoot.HootRequest.HootRequestListener;
import com.twotoasters.android.hoot.HootResult;
import com.twotoasters.android.hoot.oauthapi.OAuthApi;

public class OAuthService {

	OAuthApi apiClass;
	String apiSecret;
	String apiKey;
	String apiCallback;	

	
	private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	private static final String OAUTH_SIGNATURE = "oauth_signature";
	private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
	private static final String OAUTH_NONCE = "oauth_nonce";
	private static final String OAUTH_VERSION = "oauth_version";
	private static final String OAUTH_CALLBACK = "oauth_callback";
	private static final String OAUTH_TOKEN = "oauth_token";
	private static final String OAUTH_VERIFIER = "oauth_verifier";
	
	private static final String UTF8 = "UTF-8";
	private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String METHOD = "HMAC-SHA1";
    private static final String EMPTY_STRING = "";
    private static final String CARRIAGE_RETURN = "\r\n";
    
    private static final Pattern TOKEN_REGEX = Pattern.compile("oauth_token=([^&]+)");
    private static final Pattern SECRET_REGEX = Pattern.compile("oauth_token_secret=([^&]*)");
	private WeakReference<OAuthServiceCallback> hostRef;
    
    public interface OAuthServiceCallback{
    	public void onOAuthRequestTokenReceived(Token token);
    	public void onOAuthAccessTokenReceived(Token accessToken);
    }
    
    public OAuthService(OAuthServiceCallback host){
    	this.hostRef = new WeakReference<OAuthServiceCallback>(host);
    }
    
	public void setApi(Class<? extends OAuthApi> api){
		try {
			this.apiClass = api.newInstance();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}
	
	public OAuthApi getApi(){
		return apiClass;
	}
	public void setApiSecret(String apiSecret){
		this.apiSecret = apiSecret;
	}
	
	public void setApiKey(String apiKey){
		this.apiKey = apiKey;
	}
	
	public void setApiCallback(String apiCallback){
		this.apiCallback = apiCallback;
	}
	
	public OAuthApi getApiClass() {
		return apiClass;
	}

	public void setApiClass(OAuthApi apiClass) {
		this.apiClass = apiClass;
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getApiCallback() {
		return apiCallback;
	}
	
	public static String percentEncode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return URLEncoder.encode(s, UTF8)
                    // OAuth encodes some characters differently:
                    .replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException wow) {
            throw new RuntimeException(wow.getMessage(), wow);
        }
    }

	public String createSignature(Map<String, String> headersMap, String userSecret, String methodType, String baseUrl) throws UnsupportedEncodingException{
		
		String parameterString = "";
	
		int i=0;
		for (Map.Entry<String, String> entry : headersMap.entrySet())
		{
			if(i>0){
				parameterString += "&";
			}
		    parameterString += entry.getKey() + "=" + entry.getValue();    
		    i++;	
		}
	
		String signatureBaseString = "";
		signatureBaseString += methodType;
		signatureBaseString += "&";
		signatureBaseString += percentEncode(baseUrl);
		signatureBaseString += "&";
		signatureBaseString += percentEncode(parameterString);
				
		String signingKey = getApiSecret() + "&" + (userSecret==null ? "" : userSecret);
		
		try {
			return createHMACSignature(signatureBaseString, signingKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	

	private String createHMACSignature(String baseString, String signingKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException{
		
		SecretKeySpec key = new SecretKeySpec(signingKey.getBytes(UTF8), HMAC_SHA1);
	    Mac mac = Mac.getInstance(HMAC_SHA1);
	    mac.init(key);
	    byte[] bytes = mac.doFinal(baseString.getBytes(UTF8));
	    return new String(Base64.encodeToString(bytes, 0, bytes.length, Base64.DEFAULT)).trim();
	}
	
	private String buildOAuthHeader(String httpMethod, String url, Map<String,String> headers, String userSecret){
		Map<String, String> headersMap = headers;
		if(headersMap == null){
			headersMap = new TreeMap<String, String>();
			Log.v("into","headers are null");
		}else{
			Log.v("into","headers nto null");
		}
		
		long millis = System.currentTimeMillis();
		long timestamp = millis/1000;
		
		//build the headers that will get put in the authorization
		headersMap.put(OAUTH_CALLBACK,percentEncode(getApiCallback()));
		headersMap.put(OAUTH_CONSUMER_KEY,getApiKey());
		headersMap.put(OAUTH_NONCE,String.valueOf(millis+new Random().nextInt()));
		headersMap.put(OAUTH_SIGNATURE_METHOD,METHOD);
		headersMap.put(OAUTH_TIMESTAMP,String.valueOf(timestamp));
		headersMap.put(OAUTH_VERSION,getApi().getOauthVersion());
		try {
			headersMap.put(OAUTH_SIGNATURE, percentEncode(createSignature(headersMap, userSecret, httpMethod, url)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String authorizationHeader = "OAuth ";
		
		for (Map.Entry<String, String> entry : headersMap.entrySet())
		{
			authorizationHeader += entry.getKey();
			authorizationHeader += "=";
			authorizationHeader += "\"";
			authorizationHeader += entry.getValue();
			authorizationHeader += "\"";
			authorizationHeader += ", ";
		}
		
		//removes the final comma and space from the Authorization header
		authorizationHeader = authorizationHeader.substring(0, authorizationHeader.length()-2);
		
		return authorizationHeader;
	}
	
	public String signOAuthRequest(Token accessToken, String baseUrl, String httpMethod, Map<String,String> queryParameters) throws UnsupportedEncodingException{
		
		Map<String, String> headersMap = new TreeMap<String, String>();
		
		headersMap.put(OAUTH_TOKEN, accessToken.getAccess_token());
		
		if(queryParameters!=null && !queryParameters.isEmpty()){
			for (Map.Entry<String, String> entry : queryParameters.entrySet())
			{
			   headersMap.put(entry.getKey(), percentEncode(entry.getValue()));
			}
		}
		
		return buildOAuthHeader(httpMethod, baseUrl, headersMap, accessToken.getUser_secret());
	}
	
	public void getOAuthAccessToken(Token token, String url) throws UnsupportedEncodingException{
		
		Uri uri = Uri.parse(url);
		String verifier = uri.getQueryParameter("oauth_verifier");
		
		Map<String, String> headersMap = new TreeMap<String, String>();
		
		headersMap.put(OAUTH_TOKEN, token.getAccess_token());
		headersMap.put(OAUTH_VERIFIER, verifier);
			
		Properties headers = new Properties();
		headers.put("Authorization", buildOAuthHeader("POST", getApi().getAccessTokenResource(), headersMap));

		final Token accessToken = new Token();
		Hoot hoot = Hoot.createInstanceWithBaseUrl(getApi().getAccessTokenResource());
		HootRequest oAuthRequest = hoot.createRequest().setHeaders(headers);
		oAuthRequest.bindListener(new HootRequestListener() {
			
			@Override
			public void onSuccess(HootRequest request, HootResult result) {
				Log.v("into","on success: "+result.getResponseString());
				accessToken.setAccess_token(extract(result.getResponseString(), TOKEN_REGEX));
				accessToken.setUser_secret(extract(result.getResponseString(), SECRET_REGEX));
				if(hostRef.get()!=null){
					hostRef.get().onOAuthAccessTokenReceived(accessToken);
				}
			}
			
			@Override
			public void onRequestStarted(HootRequest request) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onRequestCompleted(HootRequest request) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(HootRequest request, HootResult result) {
				Log.v("into"," on failure: "+result.getResponseString());
			}
			
			@Override
			public void onCancelled(HootRequest request) {}
		});
		oAuthRequest.post().execute();
	}
	
	private String buildOAuthHeader(){
		return buildOAuthHeader("POST", getApi().getRequestTokenResource(), null);
	}
	
	private String buildOAuthHeader(String httpMethod, String url, Map<String,String> headers){
		return buildOAuthHeader(httpMethod, url, headers, null);
	}
	
	
	public void getOAuthToken() throws UnsupportedEncodingException{
		
		Hoot hoot = Hoot.createInstanceWithBaseUrl(getApi().getRequestTokenResource());
		
		Properties headers = new Properties();
		headers.put("Authorization", buildOAuthHeader());
		
		HootRequest oAuthRequest = hoot.createRequest().setHeaders(headers);
		
		oAuthRequest.bindListener(new HootRequestListener() {
			
			@Override
			public void onSuccess(HootRequest request, HootResult result) {
				Token token = new Token();
				token.setAccess_token(extract(result.getResponseString(), TOKEN_REGEX));
				token.setUser_secret(extract(result.getResponseString(), SECRET_REGEX));
				if(hostRef.get()!=null){
					hostRef.get().onOAuthRequestTokenReceived(token);
				}
			}
			
			@Override
			public void onRequestStarted(HootRequest request) {}
			
			@Override
			public void onRequestCompleted(HootRequest request) {}
			
			@Override
			public void onFailure(HootRequest request, HootResult result) {}
			
			@Override
			public void onCancelled(HootRequest request) {}
		});
		oAuthRequest.post().execute();
	}	
	
	 private String extract(String response, Pattern pattern)
	  {
		String extraction = null;
	    Matcher matcher = pattern.matcher(response);
	    if (matcher.find() && matcher.groupCount() >= 1)
	    {
	    	extraction = URLDecoder.decode(matcher.group(1));
	    }
	    return extraction;
	  }
}