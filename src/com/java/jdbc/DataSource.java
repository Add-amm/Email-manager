package com.java.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataSource {
	private String Driver;
	private String URL;
	private String login;
	private String passwd;
	
	public DataSource() {}
	
	public DataSource(String driver, String uRL, String login, String passwd) {
		Driver = driver;
		URL = uRL;
		this.login = login;
		this.passwd = passwd;
	}

	public String getDriver() {
		return Driver;
	}

	public void setDriver(String driver) {
		Driver = driver;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
	public Connection getConnection() {
		try {
			Class.forName(Driver);
			return DriverManager.getConnection(URL, login, passwd);
		} catch (Exception e) {
			System.out.println("Erreur : " + e.getMessage());
			return null;
		}
	}
}