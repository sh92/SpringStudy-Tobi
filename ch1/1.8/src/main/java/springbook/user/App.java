package springbook.user;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import springbook.user.dao.UserDao;
import springbook.user.domain.User;

public class App {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {

		ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
		UserDao dao = context.getBean("userDao", UserDao.class);
		User user = new User();

		user.setId("myid");
		user.setName("myName");
		user.setPassword("myPassword");
		// dao.add(user);

		System.out.println("My id is " + user.getId());
	}
}
