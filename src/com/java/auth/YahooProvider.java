package com.java.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class YahooProvider extends OAuth2Provider {

    public YahooProvider() {
        this.scopes = new String[] {
            "mail-r",
            "mail-w"
        };
    }

    @Override
    public String getAuthUrl() throws Exception {
        String baseUrl = "https://api.login.yahoo.com/oauth2/request_auth";
        StringJoiner scopeJoiner = new StringJoiner(" ");
        for (String scope : scopes) scopeJoiner.add(scope);

        String url = baseUrl
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scopeJoiner.toString(), StandardCharsets.UTF_8);
        return url;
    }
}