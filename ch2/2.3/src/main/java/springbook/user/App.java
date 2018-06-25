package springbook.user;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import springbook.user.dao.UserDao;
import springbook.user.domain.User;

public class App {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		App app = new App();
		app.addAndGet();
	}

	@Test
	public void addAndGet() throws SQLException, ClassNotFoundException {

		ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
		UserDao dao = context.getBean("userDao", UserDao.class);
		User user = new User();

		user.setId("myid");
		user.setName("myName");
		user.setPassword("myPassword");
		// dao.add(user);

		System.out.println("My id is " + user.getId());
		assertThat(dao.getCount(), is(1));
		User user2 = dao.get(user.getId());

		assertThat(user2.getName(), is(user.getName()));
		assertThat(user2.getPassword(), is(user.getPassword()));
	}
}
