package am.ik.ldap.ssp.password.sendgrid;

import am.ik.ldap.ssp.SspProps;
import am.ik.ldap.ssp.password.PasswordResetLinkSender;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SendGridPasswordResetSender implements PasswordResetLinkSender {

	private final SendGridClient sendGridClient;

	private final SspProps props;

	public SendGridPasswordResetSender(SendGridClient sendGridClient, SspProps props) {
		this.sendGridClient = sendGridClient;
		this.props = props;
	}

	@Override
	public void sendPasswordResetLink(String email, UUID resetId) {
		String content = """
				Please use this link to reset your password. It will expire in 5 minutes.

				%s/reset_password/%s
				""".formatted(this.props.externalUrl(), resetId);
		this.sendGridClient.sendMail(email, "Password Reset Link", content);
	}

}
