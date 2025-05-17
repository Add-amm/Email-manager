package com.java.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class OutlookProvider extends OAuth2Provider {

    public OutlookProvider() {
        this.scopes = new String[] {
            "offline_access",
            "https://outlook.office.com/IMAP.AccessAsUser.All",
            "https://outlook.office.com/SMTP.Send"
        };
    }

    @Override
    public String getAuthUrl() throws Exception {
        String baseUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
        StringJoiner scopeJoiner = new StringJoiner(" ");
        for (String scope : scopes) scopeJoiner.add(scope);

        String url = baseUrl
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scopeJoiner.toString(), StandardCharsets.UTF_8)
                + "&prompt=consent";
        return url;
    }
}