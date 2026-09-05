package dev.samster.granthalay.identity;

import org.springframework.stereotype.Component;

@Component
class NoOpEmailDeliveryProvider implements EmailDeliveryProvider {

	@Override
	public void sendVerificationEmail(String toEmail, String verificationToken) {
		// Development and test fallback adapter.
	}

	@Override
	public void sendPasswordResetEmail(String toEmail, String resetToken) {
		// Development and test fallback adapter.
	}

}
