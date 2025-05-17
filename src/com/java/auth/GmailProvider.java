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

public class GmailProvider extends OAuth2Provider {

    public GmailProvider() {
        // Scopes pour accès complet à Gmail
        this.scopes = new String[] { "https://mail.google.com/", "openid", "email", "profile" };
    }

    @Override
    public String getOAuth2Code() throws Exception {
        String baseUrl = "https://accounts.google.com/o/oauth2/v2/auth";
        StringJoiner scopeJoiner = new StringJoiner(" ");
        for (String scope : scopes) scopeJoiner.add(scope);

        String url = baseUrl
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scopeJoiner.toString(), StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=consent";
        
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
    	return super.getUsersInfos("https://www.googleapis.com/oauth2/v3/userinfo", token);
    }
}