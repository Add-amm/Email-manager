package com.java.App;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.UUID;

import com.java.auth.*;
import com.java.beans.Tokens;
import com.java.beans.Cookies;
import com.java.jdbc.DataSource;
import com.java.jdbc.Database;
import com.java.jdbc.MySQLDataSource;

import org.json.JSONObject;

public class Main {
	private static String token;
	private static String company;
	
	// Initialisation de la connexion avec la base de données
    private static DataSource ds = new MySQLDataSource("mail_manager");
	private static Database db = new Database(ds);

	public static void main(String[] args) throws Exception {
        try {
        	// I. Verifier le cache
        	Cookies cookie = TokenManager.loadToken();
        	
        	if (cookie != null && cookie.getToken() != null) { 
        		// Cas ou on trouve le token et il est valide
        		token = cookie.getToken();
        		company = cookie.getCompany();
        		
        	} else if (cookie != null) { 
        		// Cas ou on trouve le token dans le cache + bd, mais il n'est pas valide dans le cache
        		String user_id = cookie.getUser_id().toString();
        		company = cookie.getCompany();
        		
        		// Recherche du token dans la base de données et extraire la date d'expiration du refresh_token
        		Tokens T = db.selectTokens(user_id);
        		LocalDateTime refresh_token_expiration_date = T.getRefresh_token_expires_at();
        		
        		if (refresh_token_expiration_date.isAfter(LocalDateTime.now())) {
        			// Utiliser le refresh_token pour génerer un nouveau access_token
        			System.out.println("On teste le refresh_token");
        			
        			
        		} else {
	        		// Autre cas (Ce cas est le cas ou le refresh_token est expirer)
	        		Authentification();
	        	}
	        } else {
	        		// Autre cas (Ce cas est le cas ou l'utilisateur se connecte pour la 1ère fois)
	        	Authentification();
	        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(token);
        System.out.println("Token length: " + token.length());
        System.out.println(GetInfos());
	}
	
	public static void Authentification() throws Exception {
		Scanner scanner = new Scanner(System.in);
		
        System.out.println("Choisissez un fournisseur :");
        System.out.println("1 - Gmail");
        System.out.println("2 - Outlook");
        System.out.println("3 - Yahoo");
        System.out.print("Votre choix : ");
        int choix = scanner.nextInt();
        scanner.nextLine();  // Consommer la nouvelle ligne
        scanner.close();

        OAuth2Provider provider;
        switch (choix) {
            case 1:
                provider = new GmailProvider();
                provider.loadCredentials("resources/gmail_client.json");
                provider = (GmailProvider) provider;
                company = "Gmail";
                break;
            case 2:
                provider = new OutlookProvider();
                provider.loadCredentials("resources/outlook_client.json");
                provider = (OutlookProvider) provider;
                company = "Outlook";
                break;
            case 3:
                provider = new YahooProvider();
                provider.loadCredentials("resources/yahoo_client.json");
                provider = (YahooProvider) provider;
                company = "Yahoo";
                break;
            default:
                System.out.println("Choix invalide.");
                scanner.close();
                return;
        }
		
		// 1. Rediriger l'utilisateur vers la bonne page de connexion pour obtenir le code OAuth2
        String code = provider.getOAuth2Code();

        // 2. Echanger le code contre le access_token et le refresh_token
        String jsonResponse = TokenManager.exchangeCodeForToken(provider, code);
        
        // 3. Sauvegarder les tokens dans la base de données
		
		// Extraction des données à partir du fichier JSON
        JSONObject json = new JSONObject(jsonResponse);
		String access_token = json.getString("access_token").trim();
		int expires_in = json.getInt("expires_in");
		String refresh_token = json.getString("refresh_token").trim();
		int refresh_token_expires_in = json.getInt("refresh_token_expires_in");
		
		// Création d'un objet Token
		UUID uuid = UUID.randomUUID();
		Tokens t = new Tokens(uuid, company, access_token, expires_in, refresh_token, refresh_token_expires_in);
		
        // Insertion dans la base de données
		System.out.println(db.insertTokens(t));
		
		// 4. Mémorisation du token en cache
		LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expires_in);
		Cookies c = new Cookies(access_token, uuid, expiresAt, company);
		
		// Emregistrer ou modifier dans le cache
		TokenManager.saveToken(c);
		
		// Renommer access_token comme token pour homogeniser le reste du code
		token = access_token;
	}
	
	public static void Deconnexion() {
		TokenManager.clearToken();
	}
	
	public static String GetInfos() throws UnsupportedEncodingException, IOException, URISyntaxException {
		String response;
		
		switch(company) {
			case "Gmail" :
				GmailProvider Gmailprovider = new GmailProvider();
				response = Gmailprovider.GetAccountInfos(token);
				break;
			case "Outlook" :
				OutlookProvider Outlookprovider = new OutlookProvider();
				response = Outlookprovider.GetAccountInfos(token);
				break;
			case "Yahoo" :
				YahooProvider Yahooprovider = new YahooProvider();
				response = Yahooprovider.GetAccountInfos(token);
				break;
			default :
				response = null;
		}
		
		return response;
	}
}