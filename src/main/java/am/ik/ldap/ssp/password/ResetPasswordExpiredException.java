package am.ik.ldap.ssp.password;

public class ResetPasswordExpiredException extends RuntimeException {

	public ResetPasswordExpiredException(String message) {
		super(message);
	}

}
