package dev.samster.granthalay.identity;

public interface EmailDeliveryProvider {

	void sendVerificationEmail(String toEmail, String verificationToken);

}
