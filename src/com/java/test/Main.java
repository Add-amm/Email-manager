package com.java.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.json.JSONObject;

public class Main {
	private static String token;
	private static String company;
	private static OAuth2Provider provider;
	private static String mail;
	
	private static GeneralMailClient client;
	
	private static List<MimeMessage> mailMessages;
	private static List<MimeMessage> sentMessages;
	private static List<MimeMessage> programmedMessages = new ArrayList<>();
	
	private static int mailMessagesCurr = 0;
	private static int sentMessagesCurr = 0;
	@SuppressWarnings("unused")
	private static int programmedMessagesCurr = 0;
	
	private static int MaxMails;
	private static int MaxSentMails;
	
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
        refreshAll();
        
        // Lancer le menu
        menu();
        
        // ----------------------------------------------------------------------------------------------------------------------------------------------------------
        // TESTS
        
        /*
        // Test d'envoi
        // String destinataire = "adamsorouri@gmail.com";
        String destinataire = "adamsorouri@gmail.com adamsorouri8901@gmail.com";
        String sujet = "CV";
        String msg = "Bonjour !";
        LocalDateTime futureTime = LocalDateTime.of(2025, 5, 18, 17, 31); // 18 mai 2025 à 17h31
        
        // Envois
        
        
        SendScheduledMail(destinataire, sujet, msg, futureTime);
        
        // Test si elle est stocker dans le programmedMessages
        for (MimeMessage m: programmedMessages) {
        	showMessage(m);
        }
         // Test si elle s'auto supprime lorsque le message est envoyée (VERIFIER)
        Thread.sleep(60 * 1000);
        
        for (MimeMessage m: programmedMessages) {
        	showMessage(m);
        }
        
        
        // Pour l'envoi groupé, le champ destinataire récuperer depuis l'interface graphique contiendra tous les destinataies séparés par un espace
        if (destinataire.contains(" ")) {
        	String[] destinataires = destinataire.split(" ");
        	SendGroupedMail(destinataires, sujet, msg);
        }
        
        // Test de reception pour 20 messages
        int count = Math.min(20, MaxMails);
       
        for (int i = 0; i < count; i++) {
        	showMessage(mailMessages.get(i));
        }
        
        // Test pour les mail envoyés

        // Test de reception pour 25 messages
        int count = Math.min(25, MaxSentMails);
       
        for (int i = 0; i < count; i++) {
        	showMessage(sentMessages.get(i));
        }
        */
	}
	
	public static void menu() throws Exception {
		User u = provider.getCustomerInfos(token);
		String fullname = u.getFullName();
		
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Bonjour " + fullname);
		System.out.println("1 - Envoyer mail");
		System.out.println("2 - Lire les mails");
		System.out.println("3 - Lire les mails envoyés");
		System.out.println("4 - Envoyer un mail groupé");
		System.out.println("5 - Programmer un mail");
		System.out.println("6 - Programmer un mail groupé");
		System.out.println("7 - Lire les mails programmés");
		System.out.println("0 - Se déconnecter");
		System.out.println("Votre choix : ");
		
        int choix = scanner.nextInt();
        
        switch (choix) {
        	case 1:
        		SendMailMenu();
        	case 2:
        		ShowMails();
        	case 3:
        		ShowSentMails();
        	case 4:
        		SendGroupedMailMenu();
        	case 5:
        		ProgrammedMailMenu();
        	case 6:
        		ProgrammedGroupedMailMenu();
        	case 7:
        		ShowProgrammedMails();
        	case 0:
				Deconnexion();
        	default:
        		menu();
        }
	}
	
	public static void ShowProgrammedMails() {
		for (MimeMessage m: programmedMessages) {
			showMessage(m);
		}
	}
	
	public static void ProgrammedGroupedMailMenu() throws Exception {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Veuillez entrer l'adresse mail des destinataires séparées par un espace : ");
		String to = scanner.nextLine();
		System.out.println("Veuillez entrer l'objet du mail : ");
		String object = scanner.nextLine();
		System.out.println("Veuillez entrer le sujet du mail : ");
		String subject = scanner.nextLine();
		System.out.println("Veuillez entrer la date de l'envoi de la même forme que (yyyy-MM-dd HH:mm:ss) : ");
		String date = scanner.nextLine();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");
		LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
		
		String[] destinataires = to.split(" ");
		for (String dest: destinataires) {
			try {
				SendScheduledMail(dest, object, subject, dateTime);
			} catch(Exception e) {
				menu();
			}
		}
		
			
		System.out.println("1 - Programmer un nouveau mail groupé");
		System.out.println("0 - Revenir au menu");
        System.out.println("Votre choix : ");
		int choix = scanner.nextInt();
		
		switch (choix) {
		case 1:
			ProgrammedGroupedMailMenu();
		default: // meme le cas de 0 est inclus ici
			menu();
		}
	}
	
	public static void ProgrammedMailMenu() throws Exception {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Veuillez entrer l'adresse mail du destinataire : ");
		String to = scanner.nextLine();
		System.out.println("Veuillez entrer l'objet du mail : ");
		String object = scanner.nextLine();
		System.out.println("Veuillez entrer le sujet du mail : ");
		String subject = scanner.nextLine();
		System.out.println("Veuillez entrer la date de l'envoi de la même forme que (yyyy-MM-dd HH:mm:ss) : ");
		String date = scanner.nextLine();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");
		LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
		
		try {
			SendScheduledMail(to, object, subject, dateTime);
		} catch(Exception e) {
			menu();
		}
			
		System.out.println("1 - Programmer un nouveau mail");
		System.out.println("0 - Revenir au menu");
        System.out.println("Votre choix : ");
		int choix = scanner.nextInt();
		
		switch (choix) {
		case 1:
			ProgrammedMailMenu();
		default: // meme le cas de 0 est inclus ici
			menu();
		}
	}
	
	public static void SendGroupedMailMenu() throws Exception {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Veuillez entrer l'adresse mail des destinataires séparées par un espace : ");
		String to = scanner.nextLine();
		System.out.println("Veuillez entrer l'objet du mail : ");
		String object = scanner.nextLine();
		System.out.println("Veuillez entrer le sujet du mail : ");
		String subject = scanner.nextLine();
		
		try {
		SendGroupedMail(to.split(" "), object, subject);
		} catch(Exception e) {
			menu();
		}
	
		System.out.println("1 - Envoyer un nouveau mail groupé");
		System.out.println("0 - Revenir au menu");
        System.out.println("Votre choix : ");
		int choix = scanner.nextInt();
		
		switch (choix) {
		case 1:
			SendGroupedMailMenu();
		default: // meme le cas de 0 est inclus ici
			menu();
		}
	}
	
	public static void ShowSentMails() throws Exception {
		int curr = sentMessagesCurr;
		for (int i = curr; i < Math.min(curr + 20, MaxSentMails); i++) {
			showMessage(sentMessages.get(i));
			sentMessagesCurr++;
		}

		if (sentMessagesCurr != MaxSentMails) {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			System.out.println("1 - Page suivante");
			System.out.println("0 - Revenir au menu");
	        System.out.println("Votre choix : ");
			int choix = scanner.nextInt();
			
			switch (choix) {
			case 1:
				ShowSentMails();
			default: // meme le cas de 0 est inclus ici
				sentMessagesCurr = 0;
				menu();
			}
		} else {
			System.out.println("Vous êtes arrivé(e) à la fin des mails envoyés, retour au menu !");
			sentMessagesCurr = 0;
			menu();
		}
	}
	
	public static void ShowMails() throws Exception {
		int curr = mailMessagesCurr;
		for (int i = curr; i < Math.min(curr + 20, MaxMails); i++) {
			showMessage(mailMessages.get(i));
			mailMessagesCurr++;
		}
		
		if (mailMessagesCurr != MaxMails) {
			System.out.println("1 - Page suivante");
			System.out.println("0 - Revenir au menu");
	        System.out.println("Votre choix : ");

			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			int choix = scanner.nextInt();
			
			switch (choix) {
			case 1:
				ShowMails();
			default: // meme le cas de 0 est inclus ici
				mailMessagesCurr = 0;
				menu();
			}
		} else {
			System.out.println("Vous êtes arrivé(e) à la fin des mails, retour au menu !");
			mailMessagesCurr = 0;
			menu();
		}
	}
	
	public static void SendMailMenu() throws Exception {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Veuillez entrer l'adresse mail du destinataire : ");
		String to = scanner.nextLine();
		System.out.println("Veuillez entrer l'objet du mail : ");
		String object = scanner.nextLine();
		System.out.println("Veuillez entrer le sujet du mail : ");
		String subject = scanner.nextLine();
		
		try {
		SendMail(to, object, subject);
		} catch(Exception e) {
			menu();
		}
		
		System.out.println("1 - Envoyer un nouveau mail");
		System.out.println("0 - Revenir au menu");
        System.out.println("Votre choix : ");
		int choix = scanner.nextInt();
		
		switch (choix) {
		case 1:
			SendMailMenu();
		default: // meme le cas de 0 est inclus ici
			menu();
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
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
        System.out.println("Choisissez le type de votre compte :");
        System.out.println("1 - Gmail");
        System.out.println("2 - Outlook");
        System.out.println("3 - Yahoo");
        System.out.println("Votre choix : ");
        String choix = scanner.nextLine();
        
        switch (choix) {
        	case "1":
        		company = "Gmail";
        	case "2":
        		company = "Outlook";
        	case "3":
        		company = "Yahoo";
        	default:
        		company = choix.trim();
        }

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
	
	public static void Deconnexion() throws Exception {
		TokenManager.clearToken();
		token = null;
		company = null;
		provider = null;
		mail = null;
		
		client = null;
		
		mailMessages = null;
		sentMessages = null;
		programmedMessages = new ArrayList<>();
		
		mailMessagesCurr = 0;
		sentMessagesCurr = 0;
		programmedMessagesCurr = 0;
		
		MaxMails = 0;
		MaxSentMails = 0;
		
		Authentification();
	}
	
	public static OAuth2Provider getProvider(String company) throws Exception {
		switch (company) {
			case "Gmail", "1":
				GmailProvider Gmailprovider = new GmailProvider();
				Gmailprovider.loadCredentials("resources/gmail_client.json");
				Main.company = "Gmail";
				return Gmailprovider;
			case "Outlook", "2" :
				OutlookProvider Outlookprovider = new OutlookProvider();
				Outlookprovider.loadCredentials("resources/outlook_client.json");
				Main.company = "Outlook";
				return Outlookprovider;
			case "Yahoo", "3" :
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
	
	
	public static void showMessage(MimeMessage message) {
		
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
        
        MaxMails = mailMessages.size();
	}
	
	public static void refreshSentMessages() {
		sentMessages = client.loadSentEmails();
		
		// Inverser l'ordre des mails pour commencer par le plus récent
        Collections.reverse(sentMessages);
        
        MaxSentMails = sentMessages.size();
	}
	
	public static void refreshAll() throws MessagingException {
		refreshMailMessages();
		refreshSentMessages();
	}
}