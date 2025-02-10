package am.ik.ldap.ssp.password.sendgrid;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "sendgrid")
public record SendGridProps(@DefaultValue("https://api.sendgrid.com") URI url, String apiKey,
		@DefaultValue("noreply@example.com") String from) {

}