package springbook.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import springbook.user.domain.User;

public class UserDao {

	/*
	 * Not changeable -> singleton
	 */
	private ConnectionMaker connectionMaker;
	/*
	 * Changeable -> Not singleton
	 */
	public User user;
	public Connection c;

	public UserDao(ConnectionMaker connectionMaker) {
		connectionMaker = this.connectionMaker;
	}

	public void add(User user) throws ClassNotFoundException, SQLException {
		this.c = this.connectionMaker.makeConnection();

		PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?,?,?)");
		ps.setString(1, user.getId());
		ps.setString(2, user.getName());
		ps.setString(3, user.getPassword());

		ps.executeUpdate();

		ps.close();
		this.c.close();
	}

	public User get(String id) throws ClassNotFoundException, SQLException {
		this.c = this.connectionMaker.makeConnection();
		PreparedStatement ps = c.prepareStatement("select * from users where id = ?");
		ps.setString(1, id);

		ResultSet rs = ps.executeQuery();
		rs.next();
		this.user = new User();
		this.user.setId(rs.getString("id"));
		this.user.setName(rs.getString("name"));
		this.user.setPassword(rs.getString("password"));

		rs.close();
		ps.close();
		this.c.close();

		return user;
	}
}
