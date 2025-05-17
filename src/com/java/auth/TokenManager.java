package com.java.auth;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Permet d’échanger un code d'autorisation contre un access_token + refresh_token.
 */
public class TokenManager {

	public static String exchangeCodeForToken(OAuth2Provider provider, String code) throws IOException {
		String clientId = provider.getClientId();
	    String clientSecret = provider.getClientSecret();
	    String redirectUri = provider.getRedirectUri();
	    String tokenEndpoint = getTokenUrl(provider);
		
		URL url = new URL(tokenEndpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Configuration POST
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Construction des paramètres
        String params = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        // Envoi des paramètres
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Lecture de la réponse
        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        if (responseCode == 200) {
            return response.toString();  // JSON contenant access_token, refresh_token, expires_in, etc.
        } else {
            throw new IOException("Erreur lors de l'échange du token : " + response.toString());
        }
    }

    private static String getTokenUrl(OAuth2Provider provider) {
        if (provider instanceof GmailProvider) {
            return "https://oauth2.googleapis.com/token";
        } else if (provider instanceof OutlookProvider) {
            return "https://login.microsoftonline.com/common/oauth2/v2.0/token";
        } else if (provider instanceof YahooProvider) {
            return "https://api.login.yahoo.com/oauth2/get_token";
        } else {
            throw new IllegalArgumentException("Provider inconnu pour URL token");
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        try {
        	GmailProvider provider = new GmailProvider();
            provider.loadCredentials("resources/gmail_client.json");
            // Lire tout le contenu du fichier JSON en String
            String content = new String(Files.readAllBytes(Paths.get("resources/oauth2token_credentials.json")));
            // Créer un objet JSON à partir du contenu String
            JSONObject json = new JSONObject(content);
            // Extraire la valeur d'une clé, par exemple "client_id"
            String code = json.getString("code");

            String jsonResponse = exchangeCodeForToken(provider, code);
            System.out.println("Réponse JSON token : " + jsonResponse);
            
            // Extraction du token
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
