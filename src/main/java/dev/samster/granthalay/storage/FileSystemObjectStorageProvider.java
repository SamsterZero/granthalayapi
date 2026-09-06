package dev.samster.granthalay.storage;

import java.io.BufferedInputStream;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileSystemObjectStorageProvider implements ObjectStorageProvider {

	private final Path rootLocation;

	public FileSystemObjectStorageProvider(
			@Value("${granthalay.storage.location:./target/storage}") String storageLocation) {
		this.rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
	}

	@Override
	public void store(String key, InputStream content, long sizeBytes, String contentType) {
		try {
			Path destination = resolveAndValidateKey(key);
			Path parent = destination.getParent();
			if (parent != null && !Files.exists(parent)) {
				Files.createDirectories(parent);
			}
			Files.copy(content, destination, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to store object with key: " + key, e);
		}
	}

	@Override
	public InputStream read(String key) {
		try {
			Path target = resolveAndValidateKey(key);
			if (!Files.exists(target)) {
				throw new IllegalArgumentException("Object not found for key: " + key);
			}
			return new BufferedInputStream(Files.newInputStream(target));
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to read object with key: " + key, e);
		}
	}

	@Override
	public void delete(String key) {
		try {
			Path target = resolveAndValidateKey(key);
			Files.deleteIfExists(target);
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to delete object with key: " + key, e);
		}
	}

	@Override
	public boolean exists(String key) {
		try {
			Path target = resolveAndValidateKey(key);
			return Files.exists(target);
		}
		catch (Exception e) {
			return false;
		}
	}

	private Path resolveAndValidateKey(String key) {
		Path resolved = rootLocation.resolve(key).normalize();
		if (!resolved.startsWith(rootLocation)) {
			throw new IllegalArgumentException("Cannot store file outside current storage directory: " + key);
		}
		return resolved;
	}

}
