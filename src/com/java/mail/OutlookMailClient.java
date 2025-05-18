package com.java.mail;

public class OutlookMailClient extends GeneralMailClient {

    public OutlookMailClient(String username, String password) throws Exception {
        this.mail = username;
        this.access_token = password;
        this.imapHost = "imap-mail.outlook.com";
        this.smtpHost = "smtp-mail.outlook.com";
        this.imapPort = 993;
        this.smtpPort = 587;

        initSessions();
    }
}