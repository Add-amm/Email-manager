package com.java.App;

import java.time.LocalDateTime;
import java.util.Scanner;

import com.java.auth.*;
import com.java.beans.Tokens;
import com.java.beans.User;
import com.java.beans.Cookies;
import com.java.jdbc.DataSource;
import com.java.jdbc.Database;
import com.java.jdbc.MySQLDataSource;

import org.json.JSONObject;

public class Main {
	@SuppressWarnings("unused")
	private static String token;
	private static String company;
	private static OAuth2Provider provider;
	
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
        		provider = getProvider(company);
        		
        	} else if (cookie != null) { 
        		// Cas ou on trouve le token dans le cache + bd, mais il n'est pas valide dans le cache
        		String user_id = cookie.getUser_id().toString();
        		company = cookie.getCompany();
        		provider = getProvider(company);
        		
        		// Recherche du token dans la base de données et extraire la date d'expiration du refresh_token
        		Tokens T = db.selectTokens(user_id);
        		LocalDateTime refresh_token_expiration_date = T.getRefresh_token_expires_at();
        		
        		if (refresh_token_expiration_date.isAfter(LocalDateTime.now())) {
        			// Utiliser le refresh_token pour génerer un nouveau access_token
        			Cookies c = provider.getRefreshedAccessToken(user_id, T.getRefresh_token());
        			
        			// Mise à jour dans le cache et la base de données
        			TokenManager.saveToken(c);
        			db.UpdateTokens(c);
        			token = c.getToken();
        			
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
	}
	
	public static void Authentification() throws Exception {
		Scanner scanner = new Scanner(System.in);
		
        System.out.println("Choisissez un fournisseur :");
        System.out.println("Gmail");
        System.out.println("Outlook");
        System.out.println("Yahoo");
        System.out.println("Votre choix : ");
        String choix = scanner.nextLine();
        scanner.close();

        provider = getProvider(choix.trim());
		
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
		
		// Extraction des données de l'utilisateur
		User u = provider.getCustomerInfos(access_token);
		String user_id = u.getId();
		
		// Création d'un objet Token
		Tokens t = new Tokens(user_id, company, access_token, expires_in, refresh_token, refresh_token_expires_in);
		
        // Insertion dans la base de données (Il est important d'inserer le User avant le token, car le user_id dans tokens est une clé étrangére de user_id dans user)
		System.out.println(db.insertUser(u));
		boolean exists = db.insertTokens(t) == -1;
		
		// 4. Mémorisation du token en cache
		LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expires_in);
		Cookies c = new Cookies(access_token, user_id, expiresAt, company);
		
		// Emregistrer ou modifier dans le cache
		TokenManager.saveToken(c);
		
		// Check si le user_id du token existait déjà pour mettre à jour
		if (exists) System.out.println(db.UpdateTokens(c));
		
		// Renommer access_token comme token pour homogeniser le reste du code
		token = access_token;
	}
	
	public static void Deconnexion() {
		TokenManager.clearToken();
	}
	
	public static OAuth2Provider getProvider(String company) throws Exception {
		switch (company) {
			case "Gmail":
				GmailProvider Gmailprovider = new GmailProvider();
				Gmailprovider.loadCredentials("resources/gmail_client.json");
				Main.company = "Gmail";
				return Gmailprovider;
			case "Outlook" :
				OutlookProvider Outlookprovider = new OutlookProvider();
				Outlookprovider.loadCredentials("resources/outlook_client.json");
				Main.company = "Outlook";
				return Outlookprovider;
			case "Yahoo" :
				YahooProvider Yahooprovider = new YahooProvider();
				Yahooprovider.loadCredentials("resources/yahoo_client.json");
				Main.company = "Yahoo";
				return Yahooprovider;
			default :
				return null;
		}
	}
}