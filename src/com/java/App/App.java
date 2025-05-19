
// Not completed

package com.java.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import com.java.auth.*;

import com.java.beans.*;

import com.java.jdbc.*;

import com.java.mail.*;
import com.java.ui.LoginWindow;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.json.JSONObject;

public class App {
	private static String token;
	private static String company;
	private static OAuth2Provider provider;
	private static String mail;
	
	private static GeneralMailClient client;
	
	private static List<MimeMessage> mailMessages;
	private static List<MimeMessage> sentMessages;
	private static List<MimeMessage> programmedMessages = new ArrayList<>();
	
	// Initialisation de la connexion avec la base de données
    private static DataSource ds = new MySQLDataSource("mail_manager");
	private static Database db = new Database(ds);

	public static void main(String[] args) throws Exception {

    	TokenManager.clearToken();
    	
        try {
        	// 1. Verifier le cache
        	Cookies cookie = TokenManager.loadToken();
        	
        	if (cookie != null && cookie.getToken() != null) { // Si le token est present et valide, le loadToken retourne un Objet Cookie contenant les bons informations
        		
        		AuthentificationFromCache(cookie);
        		
        	} else if (cookie != null) { // Si le token est present et non valide, le loadToken retourne un Objet Cookie contenant uniquement le user_id et les autres valeurs en null
        		
        		AuthentificationWithRefreshToken(cookie);
        		
	        } else { // Si le token n'est pas present, le loadToken retoune null
	        	
	        	// Autre cas (Ce cas est le cas ou l'utilisateur se connecte pour la 1ère fois)
	        	new LoginWindow();
	        	
	        }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // Initialisation du client selon le fournisseur
        client = getClient(company);
        
        // Charger tous les mails de la boîte
        refreshAll();
        
        // Nombre total de mails
        int MaxMails = mailMessages.size();
        int MaxSentMails = sentMessages.size();
        
        
        // ----------------------------------------------------------------------------------------------------------------------------------------------------------
        // TESTS
        
        // Test d'envoi
        // String destinataire = "adamsorouri@gmail.com";
        String destinataire = "adamsorouri@gmail.com adamsorouri8901@gmail.com";
        String sujet = "CV";
        String msg = "Bonjour !";
        LocalDateTime futureTime = LocalDateTime.of(2025, 5, 18, 17, 31); // 18 mai 2025 à 17h31
        
        // Envois
        
        /*
        SendMail(destinataire, sujet, msg);
        SendScheduledMail(destinataire, sujet, msg, futureTime);
        
        // Test si elle est stocker dans le programmedMessages
        for (MimeMessage m: programmedMessages) {
        	showMessageForTest(m);
        }
         // Test si elle s'auto supprime lorsque le message est envoyée (VERIFIER)
        Thread.sleep(60 * 1000);
        
        for (MimeMessage m: programmedMessages) {
        	showMessageForTest(m);
        }
        */
        
        // Pour l'envoi groupé, le champ destinataire récuperer depuis l'interface graphique contiendra tous les destinataies séparés par un espace
        if (destinataire.contains(" ")) {
        	String[] destinataires = destinataire.split(" ");
        	SendGroupedMail(destinataires, sujet, msg);
        }
        
        // Test de reception pour 20 messages
        int count = Math.min(20, MaxMails);
       
        for (int i = 0; i < count; i++) {
        	showMessageForTest(mailMessages.get(i));
        }
        
        // Test pour les mail envoyés

        // Test de reception pour 25 messages
        /*
        int count = Math.min(25, MaxSentMails);
       
        for (int i = 0; i < count; i++) {
        	showMessageForTest(sentMessages.get(i));
        }
        */
	}
	
	public static void AuthentificationFromCache(Cookies cookie) {
		try {
			// Cas ou on trouve le token et il est valide
			token = cookie.getToken();
			company = cookie.getCompany();
			provider = getProvider(company);
			mail = provider.getCustomerInfos(token).getMail();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void AuthentificationWithRefreshToken(Cookies cookie) {
		try {
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
				mail = provider.getCustomerInfos(token).getMail();
				
			} else { // Cas où le token est présent mais expiré
	    		Authentification();
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void Authentification(String choix) throws Exception {
		
        company = choix;

        provider = getProvider(choix);
        
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
		mail = u.getMail();
	}
	
	public static void Deconnexion() {
		TokenManager.clearToken();
		token = null;
		company = null;
		provider = null;
		mail = null;
		
		client = null;
		
		mailMessages = null;
		sentMessages = null;
		programmedMessages = new ArrayList<>();
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
	
	public static GeneralMailClient getClient(String company) throws Exception {
		switch (company) {
			case "Gmail":
				return new GmailMailClient(mail, token);
			case "Outlook" :
				return new OutlookMailClient(mail, token);
			case "Yahoo" :
				return new YahooMailClient(mail, token);
			default :
				return null;
		}
	}
	
	public static int SendMail(String destinataire, String Objet, String message) {
        try {
        	MimeMessage msg = new MimeMessage(client.getSmtpSession());
        	msg.setHeader("Message-ID", "<" + UUID.randomUUID().toString() + "@" + "domaine.com" + ">");
			msg.setFrom(new InternetAddress(mail));
	        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destinataire));
	        msg.setSubject(Objet);
	        msg.setText(message);

	        client.sendEmail(msg);
	        return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
    }
	
	public static List<MimeMessage> LoadMails(int number) {
		try {
			return client.receiveEmails(number);
		} catch (MessagingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String lireContenu(InputStream inputStream) throws IOException {
	    StringBuilder contenu = new StringBuilder();
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
	        String ligne = reader.readLine();
	        if (ligne != null) {
	            contenu.append(ligne);
	            while ((ligne = reader.readLine()) != null) {
	                contenu.append("\n").append(ligne);
	            }
	        }
	    }
	    
	    if (contenu.toString().length() > 100) return "Unknown";
	    return contenu.toString();
	}
	
	
	public static void showMessageForTest(MimeMessage message) {
		
		if (message == null) System.out.println("Il n y a rien dans la boite");
	    try {
	        System.out.println("----- Message Info -----");
	        String CompanyName = ((InternetAddress) message.getFrom()[0]).getPersonal();
	        String CompanyMail = ((InternetAddress) message.getFrom()[0]).getAddress();
	        String sys = "";
	        if (CompanyName != null)
	        	sys = "From : " + CompanyName + ", ";
	        System.out.println(sys + "Adresse : " + CompanyMail);
	        System.out.println("To: " + Arrays.toString(message.getRecipients(Message.RecipientType.TO)));
	        System.out.println("Subject: " + message.getSubject());
	        System.out.println("Sent Date: " + message.getSentDate());

	        Object content = message.getContent();

	        System.out.println("Body:\n" + lireContenu( (InputStream) content));

	        System.out.println("-------------------------");
	    } catch (Exception e) {
	        System.err.println("Error displaying message: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	public static String SendGroupedMail(String[] destinataires, String Objet, String message) {
		for (String destinataire: destinataires) {
			SendMail(destinataire, Objet, message);
		}
		return "Email envoyé avec succès !";
	}
	
	public static int SendScheduledMail(String destinataire, String Objet, String message, LocalDateTime at) {
		
		MimeMessage msg = new MimeMessage(client.getSmtpSession());
		try {
			msg.setFrom(new InternetAddress(mail));
			msg.setReplyTo(InternetAddress.parse(mail));
	        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destinataire));
	        msg.setSubject(Objet);
	        msg.setText(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
        programmedMessages.add(msg);
		
		Runnable emailTask = () -> {
	        try {
	            SendMail(destinataire, Objet, message);
	            programmedMessages.remove(msg);
	        } catch (Exception e) {
	            programmedMessages.remove(msg);
	            e.printStackTrace();
	        }
	    };
	    
	    EmailScheduler.scheduleEmail(emailTask, at);
		return 1;
	}
	
	public static void refreshMailMessages() throws MessagingException {
		mailMessages = client.receiveAllEmails();
		
		// Inverser l'ordre des mails pour commencer par le plus récent
        Collections.reverse(mailMessages);
	}
	
	public static void refreshSentMessages() {
		sentMessages = client.loadSentEmails();
		
		// Inverser l'ordre des mails pour commencer par le plus récent
        Collections.reverse(sentMessages);
	}
	
	public static void refreshAll() throws MessagingException {
		refreshMailMessages();
		refreshSentMessages();
	}
}