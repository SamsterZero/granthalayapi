package dev.samster.granthalay.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EpubValidatorTests {

	private EpubValidator validator;

	@BeforeEach
	void setUp() {
		validator = new EpubValidator();
	}

	@Test
	void rejectsEmptyBytes() {
		EpubValidationResult result = validator.validate(new byte[0]);
		assertThat(result.valid()).isFalse();
		assertThat(result.errorMessage()).contains("cannot be empty");
	}

	@Test
	void rejectsNonZipBytes() {
		byte[] invalidBytes = "HELLO WORLD NOT ZIP".getBytes(StandardCharsets.UTF_8);
		EpubValidationResult result = validator.validate(invalidBytes);
		assertThat(result.valid()).isFalse();
		assertThat(result.errorMessage()).contains("not a valid ZIP archive");
	}

	@Test
	void rejectsZipWithoutMimetype() throws IOException {
		byte[] zipBytes = createZipArchive(entry("META-INF/container.xml", containerXml("EPUB/content.opf")),
				entry("EPUB/content.opf", opfContent()));

		EpubValidationResult result = validator.validate(zipBytes);
		assertThat(result.valid()).isFalse();
		assertThat(result.errorMessage()).contains("missing mimetype entry");
	}

	@Test
	void rejectsZipWithInvalidMimetypeContent() throws IOException {
		byte[] zipBytes = createZipArchive(entry("mimetype", "text/plain"),
				entry("META-INF/container.xml", containerXml("EPUB/content.opf")),
				entry("EPUB/content.opf", opfContent()));

		EpubValidationResult result = validator.validate(zipBytes);
		assertThat(result.valid()).isFalse();
		assertThat(result.errorMessage()).contains("invalid mimetype entry content");
	}

	@Test
	void rejectsZipSlipTraversal() throws IOException {
		byte[] zipBytes = createZipArchive(entry("mimetype", "application/epub+zip"), entry("../evil.txt", "hacked"),
				entry("META-INF/container.xml", containerXml("EPUB/content.opf")),
				entry("EPUB/content.opf", opfContent()));

		EpubValidationResult result = validator.validate(zipBytes);
		assertThat(result.valid()).isFalse();
		assertThat(result.errorMessage()).contains("Malicious path traversal entry");
	}

	@Test
	void rejectsMissingContainerXml() throws IOException {
		byte[] zipBytes = createZipArchive(entry("mimetype", "application/epub+zip"),
				entry("EPUB/content.opf", opfContent()));

		EpubValidationResult result = validator.validate(zipBytes);
		assertThat(result.valid()).isFalse();
		assertThat(result.errorMessage()).contains("missing META-INF/container.xml");
	}

	@Test
	void rejectsMissingRootfileInArchive() throws IOException {
		byte[] zipBytes = createZipArchive(entry("mimetype", "application/epub+zip"),
				entry("META-INF/container.xml", containerXml("EPUB/content.opf")));

		EpubValidationResult result = validator.validate(zipBytes);
		assertThat(result.valid()).isFalse();
		assertThat(result.errorMessage()).contains("rootfile 'EPUB/content.opf' not found in archive");
	}

	@Test
	void validatesValidEpubArchive() throws IOException {
		byte[] validZipBytes = createZipArchive(entry("mimetype", "application/epub+zip"),
				entry("META-INF/container.xml", containerXml("EPUB/content.opf")),
				entry("EPUB/content.opf", opfContent()));

		EpubValidationResult result = validator.validate(validZipBytes);
		assertThat(result.valid()).isTrue();
		assertThat(result.errorMessage()).isNull();
		assertThat(result.rootfilePath()).isEqualTo("EPUB/content.opf");
		assertThat(result.sha256Hash()).hasSize(64);
		assertThat(result.sizeBytes()).isEqualTo(validZipBytes.length);
	}

	private record Entry(String name, String content) {
	}

	private static Entry entry(String name, String content) {
		return new Entry(name, content);
	}

	private static byte[] createZipArchive(Entry... entries) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			for (Entry e : entries) {
				ZipEntry ze = new ZipEntry(e.name());
				zos.putNextEntry(ze);
				zos.write(e.content().getBytes(StandardCharsets.UTF_8));
				zos.closeEntry();
			}
		}
		return baos.toByteArray();
	}

	private static String containerXml(String rootfilePath) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
					<rootfiles>
						<rootfile full-path="%s" media-type="application/oebps-package+xml"/>
					</rootfiles>
				</container>
				""".formatted(rootfilePath);
	}

	private static String opfContent() {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="pub-id">
					<metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
						<dc:identifier id="pub-id">urn:uuid:12345</dc:identifier>
						<dc:title>Test EPUB</dc:title>
					</metadata>
					<manifest></manifest>
					<spine></spine>
				</package>
				""";
	}

}
