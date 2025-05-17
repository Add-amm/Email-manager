package com.java.auth;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import org.json.JSONObject;

import com.java.beans.Cookies;
import com.java.beans.User;
import com.java.server.OAuthCallbackServer;

public class YahooProvider extends OAuth2Provider {

    public YahooProvider() {
        this.scopes = new String[] {
            "mail-r",
            "mail-w",
            "openid",
            "profile",
            "email"
        };
    }

    @Override
    public String getOAuth2Code() throws Exception {
        String baseUrl = "https://api.login.yahoo.com/oauth2/request_auth";
        StringJoiner scopeJoiner = new StringJoiner(" ");
        for (String scope : scopes) scopeJoiner.add(scope);

        String url = baseUrl
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scopeJoiner.toString(), StandardCharsets.UTF_8);

        // Start local server to handle redirect
        OAuthCallbackServer.startServer();
        
        // Open user's default browser to authorize
        Desktop.getDesktop().browse(new URI(url));
        
        // Wait for authorization code
        while (OAuthCallbackServer.getAuthorizationCode() == null) {
            Thread.sleep(1000);
        }
        
        return OAuthCallbackServer.getAuthorizationCode();
    }
    
    @Override
    public Cookies getRefreshedAccessToken(String user_id, String refresh_token) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
    
    public String GetAccountInfos(String token) throws UnsupportedEncodingException, IOException, URISyntaxException {
    	return super.getUsersInfos("https://api.login.yahoo.com/openid/v1/userinfo", token);
    }
    
    @Override
	public User getCustomerInfos(String token) {
    	try {
			String json = super.getUsersInfos("https://www.googleapis.com/oauth2/v3/userinfo", token);
			
			// Convertion en fichier JSON
			JSONObject js = new JSONObject(json);
			String user_id = js.getString("sub");
			String mail = js.getString("email");
			String fullName = js.getString("name");
			
			return new User(user_id, mail, fullName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}