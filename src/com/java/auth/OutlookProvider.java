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

public class OutlookProvider extends OAuth2Provider {

    public OutlookProvider() {
        this.scopes = new String[] {
            "offline_access",
            "https://outlook.office.com/IMAP.AccessAsUser.All",
            "https://outlook.office.com/SMTP.Send",
            "https://graph.microsoft.com/User.Read"
        };
    }

    @Override
    public String getOAuth2Code() throws Exception {
        String baseUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
        StringJoiner scopeJoiner = new StringJoiner(" ");
        for (String scope : scopes) scopeJoiner.add(scope);

        String url = baseUrl
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scopeJoiner.toString(), StandardCharsets.UTF_8)
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
    	return super.getUsersInfos("https://graph.microsoft.com/v1.0/me", token);
    }
}