package dev.samster.granthalay.identity;

import org.springframework.stereotype.Component;

@Component
class NoOpEmailDeliveryProvider implements EmailDeliveryProvider {

	@Override
	public void sendVerificationEmail(String toEmail, String verificationToken) {
		// Development and test fallback adapter.
	}

}
