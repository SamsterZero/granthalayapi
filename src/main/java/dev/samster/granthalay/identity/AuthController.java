package dev.samster.granthalay.identity;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final RegisterAccountUseCase registerAccountUseCase;

	private final VerifyEmailUseCase verifyEmailUseCase;

	private final SignInUseCase signInUseCase;

	private final SignOutUseCase signOutUseCase;

	private final UserAccountRepository accountRepository;

	public AuthController(RegisterAccountUseCase registerAccountUseCase, VerifyEmailUseCase verifyEmailUseCase,
			SignInUseCase signInUseCase, SignOutUseCase signOutUseCase, UserAccountRepository accountRepository) {
		this.registerAccountUseCase = registerAccountUseCase;
		this.verifyEmailUseCase = verifyEmailUseCase;
		this.signInUseCase = signInUseCase;
		this.signOutUseCase = signOutUseCase;
		this.accountRepository = accountRepository;
	}

	@PostMapping("/register")
	public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
		MessageResponse response = registerAccountUseCase.execute(request);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}

	@PostMapping("/verify-email")
	public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
		MessageResponse response = verifyEmailUseCase.execute(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/sign-in")
	public ResponseEntity<AccountResponse> signIn(@Valid @RequestBody SignInRequest request,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		AccountResponse response = signInUseCase.execute(request, httpRequest, httpResponse);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/sign-out")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void signOut(HttpServletRequest httpRequest) {
		signOutUseCase.execute(httpRequest);
	}

	@GetMapping("/me")
	public ResponseEntity<AccountResponse> getCurrentAccount(Principal principal) {
		if (principal == null || principal.getName() == null) {
			throw new BadCredentialsException("Not authenticated");
		}
		var account = accountRepository.findByEmailIgnoreCase(principal.getName())
			.orElseThrow(() -> new BadCredentialsException("Not authenticated"));
		var response = new AccountResponse(account.getId(), account.getEmail(), account.getStatus(),
				account.getCreatedAt());
		return ResponseEntity.ok(response);
	}

}
