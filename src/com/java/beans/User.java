package com.java.beans;

public class User {
	private String id;
	private String mail;
	private String fullName;
	private String Name;
	private String Surname;
	
	public User(String id, String mail, String fullName, String name, String surname) {
		this.id = id;
		this.mail = mail;
		this.fullName = fullName;
		Name = name;
		Surname = surname;
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

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getSurname() {
		return Surname;
	}

	public void setSurname(String surname) {
		Surname = surname;
	}
	
	
}
