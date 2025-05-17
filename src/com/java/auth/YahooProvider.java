package com.java.auth;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

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
    
    public String GetAccountInfos(String token) throws UnsupportedEncodingException, IOException, URISyntaxException {
    	return super.getUsersInfos("https://api.login.yahoo.com/openid/v1/userinfo", token);
    }
}