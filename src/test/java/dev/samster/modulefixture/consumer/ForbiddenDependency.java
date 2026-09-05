package dev.samster.modulefixture.consumer;

import dev.samster.modulefixture.provider.Provider;

public record ForbiddenDependency(Provider provider) {
}
