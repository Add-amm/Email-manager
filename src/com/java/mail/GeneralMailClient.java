package com.java.mail;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class GeneralMailClient {

    protected Session smtpSession;
    protected Session imapSession;
    protected Store store;
    protected String imapHost;
    protected String smtpHost;
    protected String mail;
    protected String access_token;
    protected int imapPort;
    protected int smtpPort;
    protected Transport transport;

    protected void initSessions() throws MessagingException {
        // Initialisation SMTP
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host", smtpHost);
        smtpProps.put("mail.smtp.port", String.valueOf(smtpPort));
        smtpProps.put("mail.smtp.auth.mechanisms", "XOAUTH2");  // Ajouter XOAUTH2 ici

        smtpSession = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mail, access_token);
            }
        });

        // Initialisation IMAP
        Properties imapProps = new Properties();
        imapProps.put("mail.imap.host", imapHost);
        imapProps.put("mail.imap.port", String.valueOf(imapPort));
        imapProps.put("mail.imap.ssl.enable", "true");
        imapProps.put("mail.imap.auth.mechanisms", "XOAUTH2"); // obligatoire pour OAuth2

        imapSession = Session.getInstance(imapProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mail, access_token);
            }
        });

        store = imapSession.getStore("imap");
        // connect avec identifiants (mail + token)
        store.connect(imapHost, imapPort, mail, access_token);
    }
    
    public List<MimeMessage> loadSentEmails() {
        try {
        	List<MimeMessage> messages = new ArrayList<>();
            // Essayons différents noms possibles pour le dossier "Sent"
            String[] possibleSentFolders = {
                "[Gmail]/Sent Mail",   // Gmail
                "Sent Items",
                "Messages envoyés",
                "[Gmail]/Messages envoyés",
                "Sent",                // Outlook
                "Envoyés",             // Yahoo
            };

            Folder sentFolder = null;

            for (String folderName : possibleSentFolders) {
                try {
                    Folder folder = store.getFolder(folderName);
                    if (folder.exists()) {
                        sentFolder = folder;
                        break;
                    }
                } catch (MessagingException e) {
                    // Dossier introuvable, on continue avec le suivant
                }
            }

            if (sentFolder == null) {
                System.out.println("Dossier 'Envoyés' non trouvé.");
                return null;
            }

            sentFolder.open(Folder.READ_ONLY);

            Message[] msgs = sentFolder.getMessages();
            
            for (int i = 0; i < msgs.length; i++) {
                Message msg = msgs[i];
                if (msg instanceof MimeMessage) {
                    MimeMessage mimeMsg = (MimeMessage) msg;
                    messages.add(mimeMsg);
                }
            }
            return messages;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendEmail(MimeMessage message) {
        try {
			Transport.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
    }

    public List<MimeMessage> receiveAllEmails() throws MessagingException {
    	List<MimeMessage> messages = new ArrayList<>();
        Folder inbox = null;
        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        Message[] msgs = inbox.getMessages();

        for (int i = 0; i < msgs.length; i++) {
            Message msg = msgs[i];
            if (msg instanceof MimeMessage) {
                MimeMessage mimeMsg = (MimeMessage) msg;
                messages.add(mimeMsg);
            }
        }
        return messages;
    }
    
    public List<MimeMessage> receiveEmails(int maxMessages) throws MessagingException {
        List<MimeMessage> messages = new ArrayList<>();
        Folder inbox = null;
        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        Message[] msgs = inbox.getMessages();

        int count = Math.min(maxMessages, msgs.length);
        for (int i = 0; i < count; i++) {
            Message msg = msgs[i];
            if (msg instanceof MimeMessage) {
                MimeMessage mimeMsg = (MimeMessage) msg;
                messages.add(mimeMsg);
            }
        }
        return messages;
    }
    
    // Getters and Setters
	public Session getSmtpSession() {
		return smtpSession;
	}

	public void setSmtpSession(Session smtpSession) {
		this.smtpSession = smtpSession;
	}

	public Session getImapSession() {
		return imapSession;
	}

	public void setImapSession(Session imapSession) {
		this.imapSession = imapSession;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public String getImapHost() {
		return imapHost;
	}

	public void setImapHost(String imapHost) {
		this.imapHost = imapHost;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public int getImapPort() {
		return imapPort;
	}

	public void setImapPort(int imapPort) {
		this.imapPort = imapPort;
	}

	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
}