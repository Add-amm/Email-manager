package com.java.auth;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import com.java.beans.Cookies;
import com.java.beans.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class OAuth2Provider {

    protected String clientId;
    protected String clientSecret;
    protected String redirectUri;
    protected String[] scopes;
    

    // Charge le fichier JSON de credentials depuis ressources
    public void loadCredentials(String jsonResourcePath) throws Exception {
    	// Lire tout le contenu du fichier JSON en String
        String content = new String(Files.readAllBytes(Paths.get(jsonResourcePath)));
        // Créer un objet JSON à partir du contenu String
        JSONObject json = new JSONObject(content);
        // Extraire la valeur d'une clé, par exemple "client_id"
        this.clientId = json.getString("client_id");
        this.clientSecret = json.getString("client_secret");
        this.redirectUri = json.getString("redirect_uris");
    }

    // Retourne l'URL d'autorisation OAuth2 pour ce provider
    public abstract String getOAuth2Code() throws Exception;
    
    public abstract Cookies getRefreshedAccessToken(String user_id, String refresh_token) throws Exception;
    
    public abstract User getCustomerInfos(String token);
    
    public String getUsersInfos(String url, String accessToken) throws UnsupportedEncodingException, IOException, URISyntaxException {
    	
    	URI uri = new URI(url);
		URL requestUrl = uri.toURL();
		
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setConnectTimeout(10000); // facultatif
        connection.setReadTimeout(10000);    // facultatif

        int responseCode = connection.getResponseCode();
        InputStream stream;

        if (responseCode >= 200 && responseCode < 400) {
            stream = connection.getInputStream();
        } else {
            stream = connection.getErrorStream();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();
        connection.disconnect();
        
        return response.toString();
    }

    public String getClientId() {
        return clientId;
    }
    public String getClientSecret() {
        return clientSecret;
    }
    public String getRedirectUri() {
        return redirectUri;
    }
    public String[] getScopes() {
        return scopes;
    }
}