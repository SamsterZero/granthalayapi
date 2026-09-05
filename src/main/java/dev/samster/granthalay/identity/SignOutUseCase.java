package dev.samster.granthalay.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SignOutUseCase {

	public void execute(HttpServletRequest httpRequest) {
		SecurityContextHolder.clearContext();
		HttpSession session = httpRequest.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

}
