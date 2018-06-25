package springbook.user;

import java.sql.SQLException;

import springbook.user.dao.UserDao;
import springbook.user.dao.UserDaoFactory;
import springbook.user.domain.User;

public class App {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		UserDao dao = new UserDaoFactory().userDao();
		User user = new User();

		user.setId("myid");
		user.setName("myName");
		user.setPassword("myPassword");
		// dao.add(user);

		System.out.println("My id is " + user.getId());
	}
}
