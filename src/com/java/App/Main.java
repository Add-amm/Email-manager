package com.java.App;

import java.util.Scanner;

import com.java.auth.*;

public class Main {

	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);

        System.out.println("Choisissez un fournisseur :");
        System.out.println("1 - Gmail");
        System.out.println("2 - Outlook");
        System.out.println("3 - Yahoo");
        System.out.print("Votre choix : ");
        int choix = scanner.nextInt();
        scanner.nextLine();  // Consommer la nouvelle ligne

        OAuth2Provider provider;
        switch (choix) {
            case 1:
                provider = new GmailProvider();
                provider.loadCredentials("resources/gmail_client.json");
                provider = (GmailProvider) provider;
                break;
            case 2:
                provider = new OutlookProvider();
                provider.loadCredentials("resources/outlook_client.json");
                provider = (OutlookProvider) provider;
                break;
            case 3:
                provider = new YahooProvider();
                provider.loadCredentials("resources/yahoo_client.json");
                provider = (YahooProvider) provider;
                break;
            default:
                System.out.println("Choix invalide.");
                scanner.close();
                return;
        }

        try {
            // 1. Récupérer l'URL d'autorisation
            String authUrl = provider.getAuthUrl();
            System.out.println("Ouvrez ce lien dans votre navigateur et autorisez l'application :");
            System.out.println(authUrl);


        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
	}
}