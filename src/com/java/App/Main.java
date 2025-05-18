package com.java.App;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.java.auth.*;

import com.java.beans.*;

import com.java.jdbc.*;

import com.java.mail.*;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.json.JSONObject;

public class Main {
	private static String token;
	private static String company;
	private static OAuth2Provider provider;
	private static String mail;
	
	private static GeneralMailClient client;
	
	private static List<MimeMessage> mailMessages;
	
	// Initialisation de la connexion avec la base de données
    private static DataSource ds = new MySQLDataSource("mail_manager");
	private static Database db = new Database(ds);

	public static void main(String[] args) throws Exception {
        try {
        	// 1. Verifier le cache
        	Cookies cookie = TokenManager.loadToken();
        	
        	if (cookie != null && cookie.getToken() != null) { // Si le token est present et valide, le loadToken retourne un Objet Cookie contenant les bons informations
        		
        		AuthentificationFromCache(cookie);
        		
        	} else if (cookie != null) { // Si le token est present et non valide, le loadToken retourne un Objet Cookie contenant uniquement le user_id et les autres valeurs en null
        		
        		AuthentificationWithRefreshToken(cookie);
        		
	        } else { // Si le token n'est pas present, le loadToken retoune null
	        	
	        	// Autre cas (Ce cas est le cas ou l'utilisateur se connecte pour la 1ère fois)
	        	Authentification();
	        	
	        }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // Initialisation du client selon le fournisseur
        client = getClient(company);
        
        // Charger tous les mails de la boîte
        mailMessages = client.receiveAllEmails();
        
        // Inverser l'ordre des mails pour commencer par le plus récent
        Collections.reverse(mailMessages);
        
        // Nombre total de mails
        int MaxMails = mailMessages.size();
        
        // Test d'envoi
        String destinataire = "adamsorouri@gmail.com";
        String sujet = "Test 1";
        String msg = "Ceci est le 1er test";
        String filepath = "D:\\ENSAM\\INDIA\\S2\\Statistique";
        
        // Envoi
        SendMail(destinataire, sujet, msg);
        SendMailWithAttachment(destinataire, sujet, msg, filepath);
        
        
        // Pour l'envoi groupé, le champ destinataire récuperer depuis l'interface graphique contiendra tous les destinataies séparés par un espace
        if (destinataire.contains(" ")) {
        	String[] destinataires = destinataire.split(" ");
        	SendGroupedMail(destinataires, sujet, msg);
        }
        
        // Test de reception pour 10 messages
        int count = Math.min(10, MaxMails);
       
        for (int i = 0; i < count; i++) {
        	showMessageForTest(mailMessages.get(i));
        }
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
		mail = u.getMail();
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
	
	// A marche
	public static String SendMail(String destinataire, String Objet, String message) {
        try {
        	MimeMessage msg = new MimeMessage(client.getSmtpSession());
			msg.setFrom(new InternetAddress(mail));
	        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destinataire));
	        msg.setSubject("Test");
	        msg.setText("Bonjour, ceci est un test.");

	        client.sendEmail(msg);
		} catch (Exception e) {
			e.printStackTrace();
			return "Erreur !";
		}
		
    	return "Email envoyé avec succès !";
    }
	
	public static String SendMailWithAttachment(String destinataire, String Objet, String message, String filepath) {
		try {
			MimeMessage msg = new MimeMessage(client.getSmtpSession());
			msg.setFrom(new InternetAddress(mail));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
			msg.setSubject(Objet);

			// Créer Multipart pour le message
			Multipart multipart = new MimeMultipart();

			// Partie texte
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText("Voici le corps du message.");
			multipart.addBodyPart(textPart);

			// Partie pièce jointe
			MimeBodyPart attachmentPart = new MimeBodyPart();
			jakarta.activation.DataSource source = new FileDataSource(filepath);
			attachmentPart.setDataHandler(new DataHandler(source));
			attachmentPart.setFileName(new File(filepath).getName());
			multipart.addBodyPart(attachmentPart);

			// Affecter multipart au message (PAS dans un MimeBodyPart)
			msg.setContent(multipart);

            // Envoi du message
            client.sendEmail(msg);

            return "Email envoyé avec pièce jointe.";

        } catch (MessagingException e) {
            e.printStackTrace();
            return "Erreur !";
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
	
	public static void showMessageForTest(MimeMessage message) {
	    try {
	        System.out.println("----- Message Info -----");
	        System.out.println("From: " + Arrays.toString(message.getFrom()));
	        System.out.println("To: " + Arrays.toString(message.getRecipients(Message.RecipientType.TO)));
	        System.out.println("Subject: " + message.getSubject());
	        System.out.println("Sent Date: " + message.getSentDate());

	        Object content = message.getContent();

	        if (content instanceof String) {
	            System.out.println("Body:\n" + content);
	        } else if (content instanceof Multipart) {
	            Multipart multipart = (Multipart) content;
	            StringBuilder bodyText = new StringBuilder();
	            for (int i = 0; i < multipart.getCount(); i++) {
	                BodyPart part = multipart.getBodyPart(i);
	                if (part.isMimeType("text/plain")) {
	                    bodyText.append(part.getContent());
	                } else if (part.isMimeType("text/html")) {
	                    bodyText.append("[HTML content suppressed]");
	                }
	            }
	            System.out.println("Body (text/plain parts):\n" + bodyText);
	        } else {
	            System.out.println("Unsupported content type: " + content.getClass().getName());
	        }

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
	
	public static String SendScheduledMail(String destinataire, String Objet, String message) {
		return null;
	}
}