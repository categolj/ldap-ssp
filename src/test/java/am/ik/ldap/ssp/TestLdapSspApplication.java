package am.ik.ldap.ssp;

import org.springframework.boot.SpringApplication;

public class TestLdapSspApplication {

	public static void main(String[] args) {
		SpringApplication.from(LdapSspApplication::main)
			.with(TestcontainersConfiguration.class)
			.run("--spring.docker.compose.enabled=false");
	}

}
