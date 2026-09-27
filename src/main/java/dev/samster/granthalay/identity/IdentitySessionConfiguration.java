package dev.samster.granthalay.identity;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration(proxyBeanMethods = false)
class IdentitySessionConfiguration {

	@Bean
	RememberMeSessionPolicy rememberMeSessionPolicy(
			@Value("${granthalay.identity.remember-me-duration:30d}") Duration rememberMeDuration) {
		var durationSeconds = Math.toIntExact(rememberMeDuration.toSeconds());
		if (durationSeconds <= 0) {
			throw new IllegalArgumentException("Remember-me duration must be positive");
		}
		return new RememberMeSessionPolicy(durationSeconds);
	}

	@Bean
	CookieSerializer sessionCookieSerializer(
			@Value("${server.servlet.session.cookie.secure:false}") boolean secureCookie,
			RememberMeSessionPolicy rememberMeSessionPolicy) {
		var serializer = new DefaultCookieSerializer() {
			@Override
			public void writeCookieValue(CookieValue cookieValue) {
				if (cookieValue.getCookieMaxAge() != 0 && Boolean.TRUE
					.equals(cookieValue.getRequest().getAttribute(rememberMeSessionPolicy.requestAttributeName()))) {
					cookieValue.setCookieMaxAge(rememberMeSessionPolicy.durationSeconds());
				}
				super.writeCookieValue(cookieValue);
			}
		};
		serializer.setCookieName("SESSION");
		serializer.setCookiePath("/");
		serializer.setUseHttpOnlyCookie(true);
		serializer.setUseSecureCookie(secureCookie);
		serializer.setSameSite("Lax");
		return serializer;
	}

}

record RememberMeSessionPolicy(int durationSeconds) {

	String requestAttributeName() {
		return RememberMeSessionPolicy.class.getName() + ".ENABLED";
	}

}
