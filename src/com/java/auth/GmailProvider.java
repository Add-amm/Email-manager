package com.java.auth;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.StringJoiner;

import com.java.beans.Cookies;
import com.java.beans.User;
import com.java.server.OAuthCallbackServer;

import org.json.JSONObject;

public class GmailProvider extends OAuth2Provider {
	
    public GmailProvider() {
        // Scopes pour accès complet à Gmail
        this.scopes = new String[] { "https://mail.google.com/", "openid", "email", "profile" };
    }

    @Override
    public String getOAuth2Code() throws Exception {
        StringJoiner scopeJoiner = new StringJoiner(" ");
        for (String scope : scopes) scopeJoiner.add(scope);
        
        String baseUrl = "https://accounts.google.com/o/oauth2/v2/auth";
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
    
    @Override
	public Cookies getRefreshedAccessToken(String user_id, String refresh_token) throws Exception {
    	String baseUrl = "https://oauth2.googleapis.com/token";
    	URI uri = new URI(baseUrl);
		URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String params = "client_id=" + URLEncoder.encode(clientId, "UTF-8") +
                        "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8") +
                        "&refresh_token=" + URLEncoder.encode(refresh_token, "UTF-8") +
                        "&grant_type=refresh_token";

        byte[] postData = params.getBytes(StandardCharsets.UTF_8);

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postData.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData);
        }

        int responseCode = conn.getResponseCode();

        InputStream is = (responseCode == HttpURLConnection.HTTP_OK) ? conn.getInputStream() : conn.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Convertir la réponse JSON en objet JSONObject
        JSONObject json = new JSONObject(response.toString());
        String access_token = json.getString("access_token");
        int expires_in = json.getInt("expires_in");
        
        return new Cookies(access_token, user_id, LocalDateTime.now().plusSeconds(expires_in), "Gmail");
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