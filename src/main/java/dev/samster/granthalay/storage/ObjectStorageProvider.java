package dev.samster.granthalay.storage;

import java.io.InputStream;

public interface ObjectStorageProvider {

	void store(String key, InputStream content, long sizeBytes, String contentType);

	InputStream read(String key);

	void delete(String key);

	boolean exists(String key);

}
