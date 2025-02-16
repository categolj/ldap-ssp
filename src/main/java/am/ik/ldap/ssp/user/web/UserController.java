package am.ik.ldap.ssp.user.web;

import am.ik.ldap.ssp.user.UserService;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping(path = "/me")
	public Map<String, Object> me(@AuthenticationPrincipal UserDetails user) {
		return this.userService.findUser(user.getUsername());
	}

	@GetMapping(path = "/csrf")
	public Map<String, String> csrf(CsrfToken csrfToken) {
		return Map.of("csrfToken", csrfToken.getToken());
	}

}
