package com.java.beans;

import java.time.LocalDateTime;

public class Cookies {
	private String token;
	private String user_id;
	private LocalDateTime expires_at;
	private String company;

	public Cookies() {}

	public Cookies(String token, String user_id, LocalDateTime expires_at, String company) {
		this.token = token;
		this.user_id = user_id;
		this.expires_at = expires_at;
		this.company = company;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public LocalDateTime getExpires_at() {
		return expires_at;
	}

	public void setExpires_at(LocalDateTime expires_at) {
		this.expires_at = expires_at;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
}