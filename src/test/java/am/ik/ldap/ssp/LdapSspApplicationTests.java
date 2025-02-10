package am.ik.ldap.ssp;

import am.ik.ldap.ssp.password.PasswordBuilder;
import am.ik.ldap.ssp.password.PasswordService;
import am.ik.yavi.core.Validated;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.docker.compose.enabled=false", "ldap.id-attribute=uid", "ldap.user-search-base=",
				"ldap.user-search-filter=(uid={0})", "spring.http.client.factory=simple" })
class LdapSspApplicationTests {

	RestClient restClient;

	@LocalServerPort
	int port;

	@Value("${maildev.port}")
	int mainDevPort;

	@BeforeEach
	void setUp(@Autowired PasswordService passwordService, @Autowired RestClient.Builder restClientBuilder) {
		// reset password
		passwordService.changePassword(Validated
			.successWith(PasswordBuilder.password().userId("anna.meier").newPassword("password456").build()));
		this.restClient = restClientBuilder.defaultStatusHandler(__ -> true, (req, res) -> {
		}).build();
		// clear email
		this.restClient.delete().uri("http://localhost:" + mainDevPort + "/email/all").retrieve().toBodilessEntity();
	}

	@Test
	void getMe() {
		ResponseEntity<JsonNode> response = this.restClient.get()
			.uri("http://localhost:" + port + "/me")
			.headers(headers -> headers.setBasicAuth("anna.meier", "password456"))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.get("dn"))
			.isEqualTo(new TextNode("uid=anna.meier,ou=IT,uid=anna.meier,ou=IT,dc=example,dc=org"));
		assertThat(body.get("uid")).isEqualTo(new TextNode("anna.meier"));
		assertThat(body.get("mail")).isEqualTo(new TextNode("anna.meier@example.org"));
		assertThat(body.get("sn")).isEqualTo(new TextNode("Meier"));
		assertThat(body.get("cn")).isEqualTo(new TextNode("Anna Meier"));
		assertThat(body.get("objectClass")).isEqualTo(new TextNode("inetOrgPerson"));
	}

	@Test
	void getMe_wrongPassword() {
		ResponseEntity<JsonNode> response = this.restClient.get()
			.uri("http://localhost:" + port + "/me")
			.headers(headers -> headers.setBasicAuth("anna.meier", "password"))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void changePassword() {
		{
			ResponseEntity<JsonNode> response = this.restClient.post()
				.uri("http://localhost:" + port + "/change_password")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"newPassword": "password789", "oldPassword":  "password456"}
						""")
				.headers(headers -> headers.setBasicAuth("anna.meier", "password456"))
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
		{
			ResponseEntity<JsonNode> response = this.restClient.get()
				.uri("http://localhost:" + port + "/me")
				.headers(headers -> headers.setBasicAuth("anna.meier", "password456"))
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}
		{
			ResponseEntity<JsonNode> response = this.restClient.get()
				.uri("http://localhost:" + port + "/me")
				.headers(headers -> headers.setBasicAuth("anna.meier", "password789"))
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
	}

	@Test
	void changePassword_wrongOldPassword() {
		ResponseEntity<JsonNode> response = this.restClient.post()
			.uri("http://localhost:" + port + "/change_password")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{"newPassword": "password789", "oldPassword":  "password"}
					""")
			.headers(headers -> headers.setBasicAuth("anna.meier", "password456"))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void changePassword_invalidNewPassword() {
		ResponseEntity<JsonNode> response = this.restClient.post()
			.uri("http://localhost:" + port + "/change_password")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{"newPassword": "password", "oldPassword":  "password456"}
					""")
			.headers(headers -> headers.setBasicAuth("anna.meier", "password456"))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void resetPassword() {
		{
			ResponseEntity<JsonNode> response = this.restClient.post()
				.uri("http://localhost:" + port + "/reset_password/send_link")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"email": "anna.meier@example.org"}
						""")
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
		{
			ResponseEntity<JsonNode> response = this.restClient.post()
				.uri("http://localhost:" + port + "/reset_password")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"resetId": "%s", "password": "password789"}
						""".formatted(retrieveResetId()))
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
		{
			ResponseEntity<JsonNode> response = this.restClient.get()
				.uri("http://localhost:" + port + "/me")
				.headers(headers -> headers.setBasicAuth("anna.meier", "password456"))
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}
		{
			ResponseEntity<JsonNode> response = this.restClient.get()
				.uri("http://localhost:" + port + "/me")
				.headers(headers -> headers.setBasicAuth("anna.meier", "password789"))
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
	}

	@Test
	void resetPassword_expiredResetId() {
		ResponseEntity<JsonNode> response = this.restClient.post()
			.uri("http://localhost:" + port + "/reset_password")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{"resetId": "%s", "password": "password789"}
					""".formatted(UUID.randomUUID()))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
	}

	@Test
	void resetPassword_invalidPassword() {
		{
			ResponseEntity<JsonNode> response = this.restClient.post()
				.uri("http://localhost:" + port + "/reset_password/send_link")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"email": "anna.meier@example.org"}
						""")
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
		{
			ResponseEntity<JsonNode> response = this.restClient.post()
				.uri("http://localhost:" + port + "/reset_password")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"resetId": "%s", "password": "password"}
						""".formatted(retrieveResetId()))
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
	}

	String retrieveResetId() {
		ResponseEntity<JsonNode> emailsResponse = restClient.get()
			.uri("http://127.0.0.1:" + mainDevPort + "/email")
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(emailsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode emails = emailsResponse.getBody();
		assertThat(emails).isNotNull();
		assertThat(emails.size()).isEqualTo(1);
		JsonNode email = emails.get(0);
		String body = email.get("text").asText();
		Pattern pattern = Pattern
			.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
		Matcher matcher = pattern.matcher(body);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

}
