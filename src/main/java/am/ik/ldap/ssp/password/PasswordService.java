package am.ik.ldap.ssp.password;

import am.ik.ldap.ssp.LdapProps;
import am.ik.ldap.ssp.utils.LdapUtils;
import am.ik.ldap.ssp.utils.TTLCache;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import javax.naming.Name;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ConditionCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PasswordService {

	private final LdapTemplate ldapTemplate;

	private final PasswordResetLinkSender passwordResetLinkSender;

	private final TTLCache<UUID, PasswordUser> resetCache = new TTLCache<>(Duration.ofMinutes(10));

	private final LdapProps props;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public PasswordService(LdapTemplate ldapTemplate, PasswordResetLinkSender passwordResetLinkSender,
			LdapProps props) {
		this.ldapTemplate = ldapTemplate;
		this.passwordResetLinkSender = passwordResetLinkSender;
		this.props = props;
	}

	public void sendResetLink(String email) {
		ConditionCriteria criteria = LdapQueryBuilder.query().where(this.props.emailAttribute());
		boolean useLikeQuery = StringUtils.hasLength(this.props.emailPattern());
		if (useLikeQuery) {
			logger.debug("Using like query {}", this.props.emailPattern());
		}
		LdapQuery query = useLikeQuery ? criteria.like(this.props.emailPattern().formatted(email)) : criteria.is(email);
		List<DirContextAdapter> results = ldapTemplate.search(query,
				(ContextMapper<DirContextAdapter>) ctx -> (DirContextAdapter) ctx);
		if (results.isEmpty()) {
			throw new UserNotFoundException("User Not Found: %s=%s".formatted(this.props.emailAttribute(), email));
		}
		String userId = results.getFirst().getStringAttribute(this.props.idAttribute());
		UUID resetId = UUID.randomUUID();
		this.passwordResetLinkSender.sendPasswordResetLink(email, resetId);
		this.resetCache.put(resetId, new PasswordUser(userId, email), Duration.ofMinutes(5));
	}

	public void resetPassword(UUID resetId, String password) {
		PasswordUser passwordUser = this.resetCache.get(resetId);
		if (passwordUser == null) {
			throw new ResetPasswordExpiredException("The given Reset ID has already expired");
		}
		Password passwd = PasswordBuilder.password().userId(passwordUser.userId()).newPassword(password).build();
		this.changePassword(Password.validateForReset(passwd));
		this.resetCache.remove(resetId);
	}

	public void changePassword(Validated<Password> validated) {
		Password password = validated.orElseThrow(ConstraintViolationsException::new);
		LdapQuery query = LdapQueryBuilder.query().where(this.props.idAttribute()).is(password.userId());
		List<DirContextAdapter> results = ldapTemplate.search(query,
				(ContextMapper<DirContextAdapter>) ctx -> (DirContextAdapter) ctx);
		if (results.isEmpty()) {
			throw new UserNotFoundException(
					"User Not Found: %s=%s".formatted(this.props.idAttribute(), password.userId()));
		}
		DirContextAdapter userContext = results.getFirst();
		Name userDn = userContext.getDn();
		this.ldapTemplate.executeReadWrite(dirCtx -> {
			LdapContext ctx = (LdapContext) dirCtx;
			LdapName fullDn = LdapUtils.getFullDn((LdapName) userDn, ctx);
			PasswordModifyRequest request = new PasswordModifyRequest(fullDn.toString(), password.oldPassword(),
					password.newPassword());
			return ctx.extendedOperation(request);
		});
		logger.info("Successfully changed password for userId={}", password.userId());
	}

	/**
	 * An implementation of the
	 * <a target="_blank" href="https://tools.ietf.org/html/rfc3062"> LDAP Password Modify
	 * Extended Operation </a> client request.
	 * <p>
	 * Can be directed at any LDAP server that supports the Password Modify Extended
	 * Operation.
	 *
	 * @author Josh Cummings
	 * @since 4.2.9
	 */
	private static class PasswordModifyRequest implements ExtendedRequest {

		private static final byte SEQUENCE_TYPE = 48;

		private static final String PASSWORD_MODIFY_OID = "1.3.6.1.4.1.4203.1.11.1";

		private static final byte USER_IDENTITY_OCTET_TYPE = -128;

		private static final byte OLD_PASSWORD_OCTET_TYPE = -127;

		private static final byte NEW_PASSWORD_OCTET_TYPE = -126;

		private final ByteArrayOutputStream value = new ByteArrayOutputStream();

		public PasswordModifyRequest(String userIdentity, String oldPassword, String newPassword) {
			ByteArrayOutputStream elements = new ByteArrayOutputStream();

			if (userIdentity != null) {
				berEncode(USER_IDENTITY_OCTET_TYPE, userIdentity.getBytes(), elements);
			}

			if (oldPassword != null) {
				berEncode(OLD_PASSWORD_OCTET_TYPE, oldPassword.getBytes(), elements);
			}

			if (newPassword != null) {
				berEncode(NEW_PASSWORD_OCTET_TYPE, newPassword.getBytes(), elements);
			}

			berEncode(SEQUENCE_TYPE, elements.toByteArray(), this.value);
		}

		@Override
		public String getID() {
			return PASSWORD_MODIFY_OID;
		}

		@Override
		public byte[] getEncodedValue() {
			return this.value.toByteArray();
		}

		@Override
		public ExtendedResponse createExtendedResponse(String id, byte[] berValue, int offset, int length) {
			return null;
		}

		/**
		 * Only minimal support for <a target="_blank" href=
		 * "https://www.itu.int/ITU-T/studygroups/com17/languages/X.690-0207.pdf"> BER
		 * encoding </a>; just what is necessary for the Password Modify request.
		 */
		private void berEncode(byte type, byte[] src, ByteArrayOutputStream dest) {
			int length = src.length;

			dest.write(type);

			if (length < 128) {
				dest.write(length);
			}
			else if ((length & 0x0000_00FF) == length) {
				dest.write((byte) 0x81);
				dest.write((byte) (length & 0xFF));
			}
			else if ((length & 0x0000_FFFF) == length) {
				dest.write((byte) 0x82);
				dest.write((byte) ((length >> 8) & 0xFF));
				dest.write((byte) (length & 0xFF));
			}
			else if ((length & 0x00FF_FFFF) == length) {
				dest.write((byte) 0x83);
				dest.write((byte) ((length >> 16) & 0xFF));
				dest.write((byte) ((length >> 8) & 0xFF));
				dest.write((byte) (length & 0xFF));
			}
			else {
				dest.write((byte) 0x84);
				dest.write((byte) ((length >> 24) & 0xFF));
				dest.write((byte) ((length >> 16) & 0xFF));
				dest.write((byte) ((length >> 8) & 0xFF));
				dest.write((byte) (length & 0xFF));
			}

			try {
				dest.write(src);
			}
			catch (IOException e) {
				throw new IllegalArgumentException("Failed to BER encode provided value of type: " + type);
			}
		}

	}

}