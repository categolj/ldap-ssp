package am.ik.ldap.ssp;

import java.net.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("ssp")
public record SspProps(@DefaultValue("http://localhost:8080") URL externalUrl) {
}
