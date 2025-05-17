package com.java.jdbc;

public class MySQLDataSource extends DataSource {

	public MySQLDataSource(String host, String dbname, String login, String passwd) {
		super("com.mysql.cj.jdbc.Driver", "jdbc:mysql://" + host + "/" + dbname, login, passwd);
	}
	
	public MySQLDataSource(String dbname, String login, String passwd) {
		super("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost/" + dbname, login, passwd);
	}
	
	public MySQLDataSource(String dbname) {
		super("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost/" + dbname, "root", "");
	}
}