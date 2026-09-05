package dev.samster.granthalay.identity;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignInUseCase {

	private final UserAccountRepository accountRepository;

	private final PasswordEncoder passwordEncoder;

	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	public SignInUseCase(UserAccountRepository accountRepository, PasswordEncoder passwordEncoder) {
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public AccountResponse execute(SignInRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		var normalizedEmail = request.email().trim().toLowerCase();
		var account = accountRepository.findByEmailIgnoreCase(normalizedEmail)
			.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

		if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
			throw new BadCredentialsException("Invalid credentials");
		}

		if (account.getStatus() != AccountStatus.ACTIVE) {
			throw new BadCredentialsException("Account is not active");
		}

		var session = httpRequest.getSession(false);
		if (session != null) {
			httpRequest.changeSessionId();
		}

		var auth = new UsernamePasswordAuthenticationToken(account.getEmail(), null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
		var securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(auth);
		SecurityContextHolder.setContext(securityContext);
		securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

		return new AccountResponse(account.getId(), account.getEmail(), account.getStatus(), account.getCreatedAt());
	}

}
