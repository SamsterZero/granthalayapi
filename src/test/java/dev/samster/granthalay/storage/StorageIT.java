package dev.samster.granthalay.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dev.samster.granthalay.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class StorageIT {

	@Autowired
	IngestEpubUseCase ingestUseCase;

	@Autowired
	ObjectStorageProvider storageProvider;

	@Autowired
	StorageObjectRepository repository;

	private byte[] validEpubBytes;

	@BeforeEach
	void setUp() throws IOException {
		validEpubBytes = createValidEpub("Test Book");
	}

	@Test
	void ingestsValidEpubAndIncrementsVersions() throws Exception {
		String editionId = "edition-123";

		// 1. Ingest Version 1
		StorageObjectResponse v1 = ingestUseCase.ingestEpub(editionId, "sample-v1.epub", validEpubBytes);
		assertThat(v1.editionId()).isEqualTo(editionId);
		assertThat(v1.version()).isEqualTo(1);
		assertThat(v1.filename()).isEqualTo("sample-v1.epub");
		assertThat(v1.contentType()).isEqualTo("application/epub+zip");
		assertThat(v1.status()).isEqualTo("STORED");
		assertThat(v1.sha256Hash()).hasSize(64);
		assertThat(storageProvider.exists(v1.storageKey())).isTrue();

		// 2. Ingest Version 2 (Replacement)
		byte[] updatedEpubBytes = createValidEpub("Test Book Updated Edition");
		StorageObjectResponse v2 = ingestUseCase.ingestEpub(editionId, "sample-v2.epub", updatedEpubBytes);
		assertThat(v2.editionId()).isEqualTo(editionId);
		assertThat(v2.version()).isEqualTo(2);
		assertThat(v2.filename()).isEqualTo("sample-v2.epub");
		assertThat(storageProvider.exists(v2.storageKey())).isTrue();

		// 3. Fetch Latest Stored Object Metadata
		StorageObjectResponse latest = ingestUseCase.getStoredObject(editionId);
		assertThat(latest.version()).isEqualTo(2);
		assertThat(latest.id()).isEqualTo(v2.id());
	}

	@Test
	void rejectsInvalidEpubPayloads() {
		String editionId = "edition-invalid";
		byte[] corruptBytes = "INVALID EPUB BYTES".getBytes(StandardCharsets.UTF_8);

		assertThatThrownBy(() -> ingestUseCase.ingestEpub(editionId, "corrupt.epub", corruptBytes))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("EPUB validation failed: File is not a valid ZIP archive");
	}

	private static byte[] createValidEpub(String title) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			zos.putNextEntry(new ZipEntry("mimetype"));
			zos.write("application/epub+zip".getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();

			zos.putNextEntry(new ZipEntry("META-INF/container.xml"));
			String container = """
					<?xml version="1.0" encoding="UTF-8"?>
					<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
						<rootfiles>
							<rootfile full-path="EPUB/content.opf" media-type="application/oebps-package+xml"/>
						</rootfiles>
					</container>
					""";
			zos.write(container.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();

			zos.putNextEntry(new ZipEntry("EPUB/content.opf"));
			String opf = """
					<?xml version="1.0" encoding="UTF-8"?>
					<package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="pub-id">
						<metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
							<dc:identifier id="pub-id">urn:uuid:12345</dc:identifier>
							<dc:title>%s</dc:title>
						</metadata>
						<manifest></manifest>
						<spine></spine>
					</package>
					""".formatted(title);
			zos.write(opf.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}
		return baos.toByteArray();
	}

}
