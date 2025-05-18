package com.java.mail;

public class YahooMailClient extends GeneralMailClient {

    public YahooMailClient(String username, String password) throws Exception {
        this.mail = username;
        this.access_token = password;
        this.imapHost = "imap.mail.yahoo.com";
        this.smtpHost = "smtp.mail.yahoo.com";
        this.imapPort = 993;
        this.smtpPort = 587;

        initSessions();
    }
}