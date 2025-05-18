package com.java.mail;

public class GmailMailClient extends GeneralMailClient {

    public GmailMailClient(String username, String password) throws Exception {
        this.mail = username;
        this.access_token = password;
        this.imapHost = "imap.gmail.com";
        this.smtpHost = "smtp.gmail.com";
        this.imapPort = 993;
        this.smtpPort = 587;

        initSessions();
    }
}