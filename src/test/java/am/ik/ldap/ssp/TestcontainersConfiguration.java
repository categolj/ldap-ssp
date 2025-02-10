package am.ik.ldap.ssp;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection(name = "osixia/openldap")
	GenericContainer<?> ldapContainer() {
		return new GenericContainer<>(DockerImageName.parse("osixia/openldap"))
			.withEnv("LDAP_BASE_DN", "dc=example,dc=org")
			.withEnv("LDAP_ORGANISATION", "Example Organization")
			.withEnv("LDAP_DOMAIN", "example.org")
			.withEnv("LDAP_ADMIN_PASSWORD", "admin")
			.withEnv("LDAP_TLS", "false")
			.withExposedPorts(389)
			.withCommand("--copy-service")
			.withFileSystemBind("./ldap/bootstrap.ldif",
					"/container/service/slapd/assets/config/bootstrap/ldif/custom/50-bootstrap.ldif",
					BindMode.READ_ONLY)
			.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("openldap")))
			.waitingFor(Wait.forLogMessage(".*slapd starting.*", 1))
			.withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));
	}

	@Bean
	GenericContainer<?> sendgrid() {
		return new GenericContainer<>("ykanazawa/sendgrid-maildev").withEnv("SENDGRID_DEV_API_SERVER", ":3030")
			.withEnv("SENDGRID_DEV_API_KEY", "SG.test")
			.withEnv("SENDGRID_DEV_SMTP_SERVER", "127.0.0.1:1025")
			.withExposedPorts(3030, 1080)
			.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("sendgrid-maildev")))
			.waitingFor(Wait.forLogMessage(".*sendgrid-dev entered RUNNING state.*", 1))
			.withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));
	}

	@Bean
	DynamicPropertyRegistrar dynamicPropertyRegistrar(GenericContainer<?> sendgrid) {
		return registry -> {
			registry.add("sendgrid.url", () -> "http://127.0.0.1:" + sendgrid.getMappedPort(3030));
			registry.add("sendgrid.api-key", () -> "SG.test");
			registry.add("maildev.port", () -> sendgrid.getMappedPort(1080));
		};
	}

}
