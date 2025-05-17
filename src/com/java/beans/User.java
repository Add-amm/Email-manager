package com.java.beans;

public class User {
	private String id;
	private String mail;
	private String fullName;
	
	public User(String id, String mail, String fullName) {
		this.id = id;
		this.mail = mail;
		this.fullName = fullName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
