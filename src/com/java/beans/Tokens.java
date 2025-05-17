package com.java.beans;

import java.time.LocalDateTime;
import java.util.UUID;

public class Tokens {
	private UUID user_id;
	private String company;
	private String access_token;
	private int expire_in; private LocalDateTime expire_at;
	private String refresh_token;
	private int refresh_token_expires_in; private LocalDateTime refresh_token_expires_at;
	
	public Tokens() {}

	public Tokens(UUID user_id, String company, String access_token, int expire_in, String refresh_token, int refresh_token_expires_in) {
		this.user_id = user_id;
		this.company = company;
		this.access_token = access_token;
		this.expire_in = expire_in;
		this.refresh_token = refresh_token;
		this.refresh_token_expires_in = refresh_token_expires_in;
	}
	
	public Tokens(UUID user_id, String company, String access_token, LocalDateTime expire_at, String refresh_token, LocalDateTime refresh_token_expires_at) {
		this.user_id = user_id;
		this.company = company;
		this.access_token = access_token;
		this.expire_at = expire_at;
		this.refresh_token = refresh_token;
		this.refresh_token_expires_at = refresh_token_expires_at;
	}

	public UUID getUser_id() {
		return user_id;
	}

	public LocalDateTime getExpire_at() {
		return expire_at;
	}

	public void setExpire_at(LocalDateTime expire_at) {
		this.expire_at = expire_at;
	}

	public LocalDateTime getRefresh_token_expires_at() {
		return refresh_token_expires_at;
	}

	public void setRefresh_token_expires_at(LocalDateTime refresh_token_expires_at) {
		this.refresh_token_expires_at = refresh_token_expires_at;
	}

	public void setUser_id(UUID uuid) {
		this.user_id = uuid;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public int getExpire_in() {
		return expire_in;
	}

	public void setExpire_in(int expire_in) {
		this.expire_in = expire_in;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	public int getRefresh_token_expires_in() {
		return refresh_token_expires_in;
	}

	public void setRefresh_token_expires_in(int refresh_token_expires_in) {
		this.refresh_token_expires_in = refresh_token_expires_in;
	}

	@Override
	public String toString() {
		return "Tokens [company=" + company + ", access_token=" + access_token + ", expire_in=" + expire_in
				+ ", refresh_token=" + refresh_token + ", refresh_token_expires_in=" + refresh_token_expires_in + "]";
	}
}
