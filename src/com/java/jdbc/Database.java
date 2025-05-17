package com.java.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.java.beans.Cookies;
import com.java.beans.Tokens;
import com.java.beans.User;

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
			String req = "insert into oauth_tokens(user_id, provider, access_token, access_token_expires_at, refresh_token, refresh_token_expires_at) " +
		             "values(?, ?, ?, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL ? SECOND), ?, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL ? SECOND))";

			PreparedStatement stm = cnx.prepareStatement(req);
			stm.setString(1, T.getUser_id());
			stm.setString(2, T.getCompany());
			stm.setString(3, T.getAccess_token());
			stm.setInt(4, T.getExpire_in());
			stm.setString(5, T.getRefresh_token());
			stm.setInt(6, T.getRefresh_token_expires_in());
	
			return stm.executeUpdate();
		} catch (Exception e) {
			e.getStackTrace();
			return -1;
		}
	}
	
	public int insertUser(User U) {
		try {
			String req = "insert into user(user_id, mail, fullName) VALUES(?, ?, ?)";
			PreparedStatement stm = cnx.prepareStatement(req);

			stm.setString(1, U.getId());
			stm.setString(2, U.getMail());
			stm.setString(3, U.getFullName());

			return stm.executeUpdate();
		} catch (Exception e) {
			e.getStackTrace();
			return -1;
		}
	}
	
	public Tokens selectTokens(String user_id) {
		try {
			String req = "select * from oauth_tokens where user_id = ?;";
			PreparedStatement stm = cnx.prepareStatement(req);
			stm.setString(1, user_id);

			ResultSet rs = stm.executeQuery();
			if (rs.next()) {
				String company = rs.getString(3);
				String access_token = rs.getString(4);
				String expire_at = rs.getString(5);
				String refresh_token = rs.getString(6);
				String refresh_token_expires_at = rs.getString(7);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				
				return new Tokens(user_id, company, access_token, LocalDateTime.parse(expire_at, formatter), refresh_token, LocalDateTime.parse(refresh_token_expires_at, formatter));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} return null;
	}
	
	public int UpdateTokens(Cookies c) {
		try {
			String req = "update oauth_tokens set access_token = ?, access_token_expires_at = ? where user_id = ?";

			PreparedStatement stm = cnx.prepareStatement(req);
			stm.setString(1, c.getToken());
			stm.setString(2, c.getExpires_at().toString());
			stm.setString(3, c.getUser_id());

			return stm.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
}