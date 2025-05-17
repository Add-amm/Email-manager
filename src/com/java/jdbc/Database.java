package com.java.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.java.beans.Cookies;
import com.java.beans.Tokens;

public class Database {
	private DataSource ds;
	private Connection cnx;
	
	public Database() {}
	
	public Database(DataSource ds) {
		setDs(ds);
	}

	public DataSource getDs() {
		return ds;
	}

	public void setDs(DataSource ds) {
		this.ds = ds;
		cnx = ds.getConnection();
	}
	
	public int insertTokens(Tokens T) {
		try {
			String req = "insert into oauth_tokens(user_id, provider, access_token, access_token_expires_at, refresh_token, refresh_token_expires_at) "
					+ "values('" + T.getUser_id().toString() + "','" + T.getCompany() + "', '" +  T.getAccess_token() + "', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL " + T.getExpire_in() + " SECOND), '" 
					+ T.getRefresh_token() + "', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL " + T.getRefresh_token_expires_in() + " SECOND));";
			Statement stm = cnx.createStatement();
			return stm.executeUpdate(req);
		} catch (Exception e) {
			e.getStackTrace();
			return -1;
		}
	}
	
	/*
	public int insertCookie(Cookies C) {
		try {
			String req = "insert into remember_me_tokens(token, user_id, expires_at) "
					+ "values('" + C.getToken() + "','" + C.getUser_id() + "', '" +  C.getExpires_at() + "');";
			Statement stm = cnx.createStatement();
			return stm.executeUpdate(req);
		} catch (Exception e) {
			e.getStackTrace();
			return -1;
		}
	}
	*/
	
	public Tokens selectTokens(String user_id) {
		try {
			String req = "select * from oauth_tokens where user_id = '" + user_id + "';";
			Statement stm = cnx.createStatement();
			ResultSet rs = stm.executeQuery(req);
			if (rs.next()) {
				String company = rs.getString(3);
				String access_token = rs.getString(4);
				String expire_at = rs.getString(5);
				String refresh_token = rs.getString(6);
				String refresh_token_expires_at = rs.getString(7);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				
				return new Tokens(UUID.fromString(user_id), company, access_token, LocalDateTime.parse(expire_at, formatter), refresh_token, LocalDateTime.parse(refresh_token_expires_at, formatter));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} return null;
	}
}