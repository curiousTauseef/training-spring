package com.example;

import com.example.dictionary.Controller;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.MySQL5Dialect;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

public class AppJavaConfig {

	public static void main(String... args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().setActiveProfiles("jpa");
        ctx.register(AppConfiguration.class);
        ctx.refresh();

        Controller c = ctx.getBean(Controller.class);
		c.run();

		ctx.close();
	}

	@Configuration
	@ComponentScan(value = { "com.example.dictionary", "com.example.helloworld" },
		excludeFilters = @ComponentScan.Filter(
				value = Configuration.class, 
				type = FilterType.ANNOTATION)
	)
    @PropertySource("classpath:META-INF/spring/dict.properties")
	@EnableAspectJAutoProxy
	@EnableTransactionManagement
    @Import({JdbcConfiguration.class,
            HibernateConfiguration.class,
            JpaConfiguration.class})
	public static class AppConfiguration {

		@Bean(name = "validator")
		public LocalValidatorFactoryBean validator() {
			return new LocalValidatorFactoryBean();
		}

		@Bean
		public DataSource dataSource() {
			DriverManagerDataSource ds = new DriverManagerDataSource();
			ds.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
			ds.setUrl("jdbc:mysql://localhost:3306/translations?useUnicode=true&characterEncoding=utf-8");
			ds.setUsername("root");
			ds.setPassword("root");
			return ds;
		}

		@Bean
		public static PropertySourcesPlaceholderConfigurer properties() {
			return new PropertySourcesPlaceholderConfigurer();
		}

	}

    @Configuration
    @Profile("jdbc")
    public static class JdbcConfiguration {

        @Bean
        public DataSourceTransactionManager transactionManager(DataSource ds) {
            return new DataSourceTransactionManager(ds);
        }

    }

    @Configuration
    @Profile("jpa")
    public static class JpaConfiguration {

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setShowSql(Boolean.TRUE);

            LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
            emf.setJpaVendorAdapter(vendorAdapter);
            emf.setPackagesToScan("com.example.dictionary.model");
            emf.setDataSource(ds);
            return emf;
        }

        @Bean
        public JpaTransactionManager jpaTxManager(DataSource ds) {
            JpaTransactionManager tx = new JpaTransactionManager();
            tx.setEntityManagerFactory(entityManagerFactory(ds).getObject());
            return tx;
        }

    }

    @Configuration
    @Profile("hibernate")
    public static class HibernateConfiguration {

        @Bean
        public LocalSessionFactoryBean session(DataSource ds) {
            LocalSessionFactoryBean session = new LocalSessionFactoryBean();
            session.setDataSource(ds);
            session.setPackagesToScan(new String[] { "com.example.dictionary.model" });

            Properties props = new Properties();
            props.put("hibernate.dialect", MySQL5Dialect.class.getName());
            props.put("hibernate.show_sql", true);
            session.setHibernateProperties(props);

            return session;
        }

        @Bean
        public HibernateTransactionManager hibernateTxManager(SessionFactory factory) {
            HibernateTransactionManager tx = new HibernateTransactionManager();
            tx.setSessionFactory(factory);
            return tx;
        }
    }

}
