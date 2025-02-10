package am.ik.ldap.ssp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LdapSspApplication {

	public static void main(String[] args) {
		SpringApplication.run(LdapSspApplication.class, args);
	}

}
