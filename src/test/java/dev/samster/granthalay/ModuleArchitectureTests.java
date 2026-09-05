package dev.samster.granthalay;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModuleArchitectureTests {

	@Test
	void verifiesFoundationModules() {
		var modules = ApplicationModules.of(GranthalayApplication.class);
		assertThat(modules.stream().map(module -> module.getIdentifier().toString())).containsExactlyInAnyOrder(
				"identity", "catalog", "publishing", "storage", "commerce", "entitlements", "delivery", "operations");
		modules.verify();
	}

	@Test
	void rejectsForbiddenDependencies() {
		// Include test classes for this deliberately invalid fixture outside the
		// application.
		assertThatThrownBy(() -> ApplicationModules.of("dev.samster.modulefixture", location -> true).verify())
			.isInstanceOf(Violations.class)
			.hasMessageContaining("Allowed targets: none");
	}

}
