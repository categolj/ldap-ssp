package am.ik.ldap.ssp.password;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ApplicativeValidator;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.Validated;
import org.jilt.Builder;
import org.jilt.BuilderStyle;
import org.jilt.Opt;

@Builder(style = BuilderStyle.STAGED_PRESERVING_ORDER)
public record Password(String userId, @Opt String oldPassword, String newPassword) {

	enum ChangeType implements ConstraintGroup {

		CHANGE, RESET

	}

	static ApplicativeValidator<Password> validator = ValidatorBuilder.<Password>of()
		._string(Password::userId, "userId", c -> c.notBlank())
		.constraintOnGroup(ChangeType.CHANGE, b -> b._string(Password::oldPassword, "oldPassword", c -> c.notBlank()))
		._string(Password::newPassword, "newPassword",
				c -> c.notBlank()
					.greaterThanOrEqual(8)
					.lessThanOrEqual(128)
					.password(policy -> policy.alphabets().numbers().build()))
		.build()
		.applicative();

	public static Validated<Password> validateForChange(Password password) {
		return validator.validate(password, ChangeType.CHANGE);
	}

	public static Validated<Password> validateForReset(Password password) {
		return validator.validate(password, ChangeType.RESET);
	}
}
