package com.om;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * This class configures all the security settings.
 * 
 * @author Maneendra
 *
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable().authorizeRequests().antMatchers("/static/**").permitAll().anyRequest().authenticated()
				.and().formLogin().loginPage("/login").passwordParameter("password").usernameParameter("username")
				.failureUrl("/login?error") // default one
				// .failureUrl("/loginfailed")
				.permitAll().and().logout().logoutSuccessUrl("/login?logout") // default
																				// one
				// .logoutSuccessUrl("/logoutsuccess")
				.permitAll();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("test").password("123").roles("USER");
	}
}
