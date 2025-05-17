package com.java.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.prefs.Preferences;

import com.java.beans.Cookies;

/**
 * Permet d’échanger un code d'autorisation contre un access_token + refresh_token.
 */
public class TokenManager {
	
	private static final Preferences prefs = Preferences.userNodeForPackage(TokenManager.class);

	public static String exchangeCodeForToken(OAuth2Provider provider, String code) throws IOException, URISyntaxException {
		String clientId = provider.getClientId();
	    String clientSecret = provider.getClientSecret();
	    String redirectUri = provider.getRedirectUri();
	    String tokenEndpoint = getTokenUrl(provider);
		
	    URI uri = new URI(tokenEndpoint);
		URL url = uri.toURL();
		
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
    
    // Implémentation d'une logique Cookie
    
    /**
     * Enregistre le user_id + token dans un fichier sous la forme : {user_id: {"token": token, "expires_at": expires_at}}
     */
    public static void saveToken(Cookies C) {
    	String user_id = C.getUser_id();
    	String token = C.getToken();
    	LocalDateTime expires_at = C.getExpires_at();
    	String company = C.getCompany();
    	
    	try {
			prefs.put("user_id", user_id);
			prefs.put("token", token);
			prefs.put("expires_at", expires_at.toString());;
			prefs.put("company", company);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * @return le token si il est valide, sinon un objet Cookie contenant seulememt le user_id et les autres champ null
     */
    public static Cookies loadToken() {
    	String user_id = prefs.get("user_id", null);
    	String token = prefs.get("token", null);
    	String expires_at = prefs.get("expires_at", null);
    	String company = prefs.get("company", null);
    	
    	// Si le token pour cette utilisateur n'existe pas, on retourne null
    	if (token == null) return null;
    	
        // Reconvertion de la date en LocalDateTime
        LocalDateTime expiration_date = LocalDateTime.parse(expires_at);
        
        // Retourne le token si il n'est pas expirée sinon null
        return expiration_date.isAfter(LocalDateTime.now()) ? new Cookies(token, user_id, expiration_date, company) : new Cookies(null, user_id, null, company);
    }
    
    public static void clearToken() {
    	prefs.remove("user_id");
    	prefs.remove("token");
    	prefs.remove("expires_at");
    }
}