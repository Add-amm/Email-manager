package com.java.auth;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

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
    public abstract String getAuthUrl() throws Exception;

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