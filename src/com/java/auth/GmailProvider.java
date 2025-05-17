package com.java.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class GmailProvider extends OAuth2Provider {

    public GmailProvider() {
        // Scopes pour accès complet à Gmail
        this.scopes = new String[] { "https://mail.google.com/" };
    }

    @Override
    public String getAuthUrl() throws Exception {
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
        return url;
    }
}