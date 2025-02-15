package am.ik.ldap.ssp.config;

import am.ik.ldap.ssp.LdapProps;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	private final LdapProps ldapProps;

	public SecurityConfig(LdapProps ldapProps) {
		this.ldapProps = ldapProps;
	}

	@Bean
	public DefaultLdapAuthoritiesPopulator authorities(LdapContextSource contextSource) {
		DefaultLdapAuthoritiesPopulator authorities = new DefaultLdapAuthoritiesPopulator(contextSource,
				this.ldapProps.groupSearchBase());
		if (StringUtils.hasLength(this.ldapProps.groupSearchFilter())) {
			authorities.setGroupSearchFilter(this.ldapProps.groupSearchFilter());
		}
		return authorities;
	}

	@Bean
	public AuthenticationManager authenticationManager(LdapContextSource contextSource) {
		LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
		if (StringUtils.hasLength(this.ldapProps.userSearchBase())) {
			factory.setUserSearchBase(this.ldapProps.userSearchBase());
		}
		if (StringUtils.hasLength(this.ldapProps.userSearchFilter())) {
			factory.setUserSearchFilter(this.ldapProps.userSearchFilter());
		}
		return factory.createAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(auth -> auth.requestMatchers(EndpointRequest.toAnyEndpoint())
				.permitAll()
				.requestMatchers("/api/reset_password", "/api/reset_password/**", "/", "/index.html",
						"/reset_password/**", "/assets/**")
				.permitAll()
				.anyRequest()
				.authenticated())
			.formLogin(form -> {
			})
			.httpBasic(basic -> {
			})
			.csrf(csrf -> csrf.disable())
			.build();
	}

}
