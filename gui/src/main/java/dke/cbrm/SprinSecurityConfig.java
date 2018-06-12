package dke.cbrm;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SprinSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource(name = "authService")
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http.csrf().disable().exceptionHandling()
		.authenticationEntryPoint(
			new LoginUrlAuthenticationEntryPoint("/login"))
		.accessDeniedPage("/accessDenied").and().authorizeRequests()
		.antMatchers("/VAADIN/**", "/PUSH/**", "/UIDL/**", "/login",
			"/login/**", "/error/**", "/accessDenied/**",
			"/vaadinServlet/**", "/console/**")
		.permitAll().antMatchers("/authorized", "/**")
		.fullyAuthenticated();
	/** Frames only required for h2 console **/
	// http.headers().frameOptions().disable();
    }

    @Bean
    public DaoAuthenticationProvider createDaoAuthenticationProvider() {
	DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

	provider.setUserDetailsService(userDetailsService);
	provider.setPasswordEncoder(passwordEncoder());
	return provider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
	return new BCryptPasswordEncoder();
    }
}
