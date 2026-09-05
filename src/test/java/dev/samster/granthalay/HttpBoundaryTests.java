package dev.samster.granthalay;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.stream.Stream;

import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

class HttpBoundaryTests {

	@Test
	void restControllersNeverExposePersistenceEntities() throws ClassNotFoundException {
		var scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
		for (var candidate : scanner.findCandidateComponents("dev.samster.granthalay")) {
			var controller = Class.forName(candidate.getBeanClassName());
			for (var method : controller.getDeclaredMethods()) {
				var boundaryTypes = Stream.concat(Stream.of(method.getGenericReturnType()),
						Arrays.stream(method.getGenericParameterTypes()));
				assertThat(boundaryTypes).as("HTTP types used by %s#%s", controller.getName(), method.getName())
					.noneMatch(HttpBoundaryTests::containsEntity);
			}
		}
	}

	private static boolean containsEntity(Type type) {
		if (type instanceof Class<?> candidate) {
			return candidate.isAnnotationPresent(Entity.class)
					|| candidate.isArray() && containsEntity(candidate.componentType());
		}
		if (type instanceof ParameterizedType parameterized) {
			return containsEntity(parameterized.getRawType())
					|| Arrays.stream(parameterized.getActualTypeArguments()).anyMatch(HttpBoundaryTests::containsEntity);
		}
		if (type instanceof GenericArrayType array) {
			return containsEntity(array.getGenericComponentType());
		}
		if (type instanceof WildcardType wildcard) {
			return Stream.concat(Arrays.stream(wildcard.getLowerBounds()), Arrays.stream(wildcard.getUpperBounds()))
				.anyMatch(HttpBoundaryTests::containsEntity);
		}
		return false;
	}

}
