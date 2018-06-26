package springbook.user.dao;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
public class DaoFactory {

	@Bean
	public DataSource dataSource() {
		SimpleDriverDataSource datasource = new SimpleDriverDataSource();

		datasource.setDriverClass(com.mysql.jdbc.Driver.class);
		datasource.setUrl("jdbc:mysql://localhost/springbook?characterEncoding=UTF-8");
		datasource.setUsername("spring");
		datasource.setPassword("book");
		return datasource;
	}

	@Bean
	public UserDao userDao() {
		UserDao userDao = new UserDao();
		userDao.setDataSource(dataSource());
		return userDao;
	}
}
