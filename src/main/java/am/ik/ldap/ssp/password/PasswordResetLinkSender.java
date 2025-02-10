package am.ik.ldap.ssp.password;

import java.util.UUID;

public interface PasswordResetLinkSender {

	void sendPasswordResetLink(String email, UUID resetId);

}
