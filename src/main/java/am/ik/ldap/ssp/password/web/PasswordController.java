package am.ik.ldap.ssp.password.web;

import am.ik.ldap.ssp.password.Password;
import am.ik.ldap.ssp.password.PasswordBuilder;
import am.ik.ldap.ssp.password.PasswordService;
import am.ik.ldap.ssp.password.ResetPasswordExpiredException;
import am.ik.yavi.core.Validated;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordController {

	private final PasswordService passwordService;

	public PasswordController(PasswordService passwordService) {
		this.passwordService = passwordService;
	}

	@PostMapping(path = "/reset_password/send_link")
	public void sendResetLink(@RequestBody SendResetLinkRequest request) {
		this.passwordService.sendResetLink(request.email());
	}

	@PostMapping(path = "/reset_password")
	public void resetPassword(@RequestBody ResetPasswordRequest request) {
		this.passwordService.resetPassword(request.resetId(), request.password);
	}

	@PostMapping(path = "/change_password")
	public void changePassword(@RequestBody ChangePasswordRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = userDetails.getUsername();
		this.passwordService.changePassword(request.toPassword(userId));
	}

	public record ChangePasswordRequest(String oldPassword, String newPassword) {
		Validated<Password> toPassword(String userId) {
			return Password.validateForChange(PasswordBuilder.password()
				.userId(userId)
				.oldPassword(oldPassword)
				.newPassword(newPassword)
				.build());
		}
	}

	public record SendResetLinkRequest(String email) {
	}

	public record ResetPasswordRequest(UUID resetId, String password) {
	}

	@ExceptionHandler(ResetPasswordExpiredException.class)
	public ResponseEntity<?> handleResetPasswordExpiredException(ResetPasswordExpiredException e) {
		return ResponseEntity.status(HttpStatus.GONE)
			.body(ProblemDetail.forStatusAndDetail(HttpStatus.GONE, e.getMessage()));
	}

}
