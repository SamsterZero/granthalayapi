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
				var returnType = Stream.of(method.getGenericReturnType());
				var parameterTypes = Arrays.stream(method.getGenericParameterTypes());
				var boundaryTypes = Stream.concat(returnType, parameterTypes);
				var description = "HTTP types used by %s#%s".formatted(controller.getName(), method.getName());
				assertThat(boundaryTypes).as(description).noneMatch(HttpBoundaryTests::containsEntity);
			}
		}
	}

	private static boolean containsEntity(Type type) {
		if (type instanceof Class<?> candidate) {
			if (candidate.isAnnotationPresent(Entity.class)) {
				return true;
			}
			return candidate.isArray() && containsEntity(candidate.componentType());
		}
		if (type instanceof ParameterizedType parameterized) {
			if (containsEntity(parameterized.getRawType())) {
				return true;
			}
			return Arrays.stream(parameterized.getActualTypeArguments()).anyMatch(HttpBoundaryTests::containsEntity);
		}
		if (type instanceof GenericArrayType array) {
			return containsEntity(array.getGenericComponentType());
		}
		if (type instanceof WildcardType wildcard) {
			var lowerBounds = Arrays.stream(wildcard.getLowerBounds());
			var upperBounds = Arrays.stream(wildcard.getUpperBounds());
			return Stream.concat(lowerBounds, upperBounds).anyMatch(HttpBoundaryTests::containsEntity);
		}
		return false;
	}

}
