package dev.samster.granthalay.operations;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ApiIndexController {

	@GetMapping("/api/v1")
	ApiIndexResponse index() {
		return new ApiIndexResponse("Granthalay API", "v1", "/openapi/granthalay-api-v1.yaml");
	}

}
