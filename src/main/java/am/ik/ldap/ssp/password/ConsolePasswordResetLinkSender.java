package am.ik.ldap.ssp.password;

import am.ik.ldap.ssp.SspProps;
import java.util.UUID;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

@Component
@Fallback
public class ConsolePasswordResetLinkSender implements PasswordResetLinkSender {

	private final SspProps props;

	public ConsolePasswordResetLinkSender(SspProps props) {
		this.props = props;
	}

	@Override
	public void sendPasswordResetLink(String email, UUID resetId) {
		String resetLink = this.props.externalUrl() + "/reset_password/" + resetId;
		System.out.printf("Sending password reset link (%s) to %s%n", resetLink, email);
	}

}
