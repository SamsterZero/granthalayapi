package dev.samster.granthalay;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static org.assertj.core.api.Assertions.assertThat;

class ApiContractCompatibilityTests {

	@Test
	void publishedContractDoesNotBreakTheV1Baseline() throws IOException {
		var current = load("/static/openapi/granthalay-api-v1.yaml");
		var baseline = load("/openapi/granthalay-api-v1-baseline.yaml");

		var currentPaths = map(current.get("paths"));
		map(baseline.get("paths")).forEach((path, baselinePathValue) -> {
			assertThat(currentPaths).as("published path %s", path).containsKey(path);
			var currentPath = map(currentPaths.get(path));
			map(baselinePathValue).forEach((method, baselineOperationValue) -> {
				assertThat(currentPath).as("operation %s %s", method, path).containsKey(method);
				var currentResponses = map(map(currentPath.get(method)).get("responses"));
				var baselineResponses = map(map(baselineOperationValue).get("responses"));
				assertThat(currentResponses.keySet()).as("responses for %s %s", method, path)
					.containsAll(baselineResponses.keySet());
			});
		});

		var currentSchemas = map(map(current.get("components")).get("schemas"));
		map(map(baseline.get("components")).get("schemas")).forEach((name, baselineSchemaValue) -> {
			assertThat(currentSchemas).as("schema %s", name).containsKey(name);
			var currentSchema = map(currentSchemas.get(name));
			var baselineSchema = map(baselineSchemaValue);
			assertThat(list(currentSchema.get("required"))).as("required properties of %s", name)
				.containsAll(list(baselineSchema.get("required")));
			var currentProperties = map(currentSchema.get("properties"));
			map(baselineSchema.get("properties")).forEach((property, baselinePropertyValue) -> {
				assertThat(currentProperties).as("property %s.%s", name, property).containsKey(property);
				var currentProperty = map(currentProperties.get(property));
				map(baselinePropertyValue).forEach((constraint, value) -> assertThat(currentProperty.get(constraint))
					.as("%s.%s %s", name, property, constraint)
					.isEqualTo(value));
			});
		});
	}

	private Map<String, Object> load(String resource) throws IOException {
		try (InputStream input = getClass().getResourceAsStream(resource)) {
			assertThat(input).as("contract resource %s", resource).isNotNull();
			return map(new Yaml().load(input));
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> map(Object value) {
		return value == null ? Map.of() : (Map<String, Object>) value;
	}

	@SuppressWarnings("unchecked")
	private static List<String> list(Object value) {
		return value == null ? List.of() : (List<String>) value;
	}

}
