package dev.samster.granthalay.identity;

import java.util.Map;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

@Service
public class RevokeUserSessionsUseCase {

	private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

	public RevokeUserSessionsUseCase(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
		this.sessionRepository = sessionRepository;
	}

	public int execute(String email) {
		if (email == null || email.isBlank()) {
			return 0;
		}

		var normalizedEmail = email.trim().toLowerCase();
		Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(normalizedEmail);
		int revokedCount = sessions.size();
		for (String sessionId : sessions.keySet()) {
			sessionRepository.deleteById(sessionId);
		}
		return revokedCount;
	}

}
