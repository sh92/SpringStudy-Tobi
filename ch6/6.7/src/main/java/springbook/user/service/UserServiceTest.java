package springbook.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {
	@Autowired
	UserService userService;
	@Autowired
	UserService testUserService;
	@Autowired
	UserDao userDao;
	@Autowired
	MailSender mailSender;
	@Autowired
	PlatformTransactionManager transactionManager;
	@Autowired
	ApplicationContext context;

	List<User> users;

	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("aaaaaa", "aaa", "p1", "user1@mail.com", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0),
				new User("bbbbbb", "bbb", "p2", "user2@mail.com", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("cccccc", "ccc", "p3", "user3@mail.com", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD - 1),
				new User("dddddd", "ddd", "p4", "user4@mail.com", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD),
				new User("eeeeee", "eee", "p5", "user5@mail.com", Level.GOLD, 100, Integer.MAX_VALUE));
	}

	@Test
	public void upgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl();

		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImpl.setUserDao(mockUserDao);

		MockMailSender mockMailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockMailSender);

		userServiceImpl.upgradeLevels();

		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "bbbbbb", Level.SILVER);
		checkUserAndLevel(updated.get(1), "dddddd", Level.GOLD);

		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}

	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}

	static class MockUserDao implements UserDao {
		private List<User> users;
		private List<User> updated = new ArrayList();

		private MockUserDao(List<User> users) {
			this.users = users;
		}

		public List<User> getUpdated() {
			return this.updated;
		}

		public List<User> getAll() {
			return this.users;
		}

		public void update(User user) {
			updated.add(user);
		}

		public void add(User user) {
			throw new UnsupportedOperationException();
		}

		public void deleteAll() {
			throw new UnsupportedOperationException();
		}

		public User get(String id) {
			throw new UnsupportedOperationException();
		}

		public int getCount() {
			throw new UnsupportedOperationException();
		}
	}

	static class MockMailSender implements MailSender {
		private List<String> requests = new ArrayList<String>();

		public List<String> getRequests() {
			return requests;
		}

		public void send(SimpleMailMessage mailMessage) throws MailException {
			requests.add(mailMessage.getTo()[0]);
		}

		public void send(SimpleMailMessage[] mailMessage) throws MailException {
		}
	}

	@Test
	public void mockUpgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl();

		UserDao mockUserDao = mock(UserDao.class);
		when(mockUserDao.getAll()).thenReturn(this.users);
		userServiceImpl.setUserDao(mockUserDao);

		MailSender mockMailSender = mock(MailSender.class);
		userServiceImpl.setMailSender(mockMailSender);

		userServiceImpl.upgradeLevels();

		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao).update(users.get(1));
		assertThat(users.get(1).getLevel(), is(Level.SILVER));
		verify(mockUserDao).update(users.get(3));
		assertThat(users.get(3).getLevel(), is(Level.GOLD));

		ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
	}

	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}

	@Test
	public void add() {
		userDao.deleteAll();

		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);

		userService.add(userWithLevel);
		userService.add(userWithoutLevel);

		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
	}

	@Test
	public void upgradeAllOrNothing() {
		userDao.deleteAll();
		for (User user : users)
			userDao.add(user);

		try {
			testUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (TestUserServiceException e) {
		}

		checkLevelUpgraded(users.get(1), false);
	}

	@Test(expected = TransientDataAccessResourceException.class)
	public void readOnlyTransactionAttribute() {
		testUserService.getAll();
	}

	@Test
	@Transactional(propagation = Propagation.NEVER)
	public void transactionSync() {
		userService.deleteAll();
		userService.add(users.get(0));
		userService.add(users.get(1));
	}

	static class TestUserService extends UserServiceImpl {
		private String id = "bbbbbb";

		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id))
				throw new TestUserServiceException();
			super.upgradeLevel(user);
		}

		public List<User> getAll() {
			for (User user : super.getAll()) {
				super.update(user);
			}
			return null;
		}

	}

	static class TestUserServiceException extends RuntimeException {
	}

}
