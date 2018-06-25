package springbook.user;

import java.sql.SQLException;

import springbook.user.dao.NUserDao;
import springbook.user.dao.UserDao;
import springbook.user.domain.User;

public class App {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		UserDao dao = new NUserDao();
		User user = new User();

		user.setId("myid");
		user.setName("myName");
		user.setPassword("myPassword");
		// dao.add(user);

		System.out.println("My id is " + user.getId());
	}
}
