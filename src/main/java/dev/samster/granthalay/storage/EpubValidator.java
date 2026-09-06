package dev.samster.granthalay.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
public class EpubValidator {

	public static final long MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024L; // 100 MB

	public static final long MAX_UNCOMPRESSED_SIZE_BYTES = 500 * 1024 * 1024L; // 500 MB

	public static final double MAX_COMPRESSION_RATIO = 100.0;

	public EpubValidationResult validate(byte[] epubBytes) {
		if (epubBytes == null || epubBytes.length == 0) {
			return EpubValidationResult.failure("EPUB payload cannot be empty");
		}

		if (epubBytes.length > MAX_FILE_SIZE_BYTES) {
			return EpubValidationResult.failure("EPUB file size exceeds maximum limit of 100MB");
		}

		// 1. Magic Bytes Check (PK\x03\x04)
		if (epubBytes.length < 4 || epubBytes[0] != 0x50 || epubBytes[1] != 0x4B || epubBytes[2] != 0x03
				|| epubBytes[3] != 0x04) {
			return EpubValidationResult.failure("File is not a valid ZIP archive");
		}

		// 2. SHA-256 Digest Calculation
		String sha256Hash;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(epubBytes);
			sha256Hash = HexFormat.of().formatHex(hashBytes);
		}
		catch (Exception e) {
			return EpubValidationResult.failure("Failed to compute file digest");
		}

		// 3. Scan ZIP Entries for Safety & EPUB Metadata
		Set<String> entryNames = new HashSet<>();
		byte[] mimetypeBytes = null;
		byte[] containerXmlBytes = null;
		long totalUncompressedSize = 0L;

		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(epubBytes))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();

				// Zip Slip Check
				if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
					return EpubValidationResult.failure("Malicious path traversal entry detected in EPUB archive");
				}

				entryNames.add(name);

				// Read entry content
				ByteArrayOutputStream entryStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[8192];
				int len;
				long entryUncompressed = 0L;

				while ((len = zis.read(buffer)) != -1) {
					entryStream.write(buffer, 0, len);
					entryUncompressed += len;
					totalUncompressedSize += len;

					if (totalUncompressedSize > MAX_UNCOMPRESSED_SIZE_BYTES) {
						return EpubValidationResult
							.failure("EPUB archive total uncompressed size exceeds security threshold");
					}
				}

				// Compression Ratio Check
				if (epubBytes.length > 0) {
					double ratio = (double) totalUncompressedSize / (double) epubBytes.length;
					if (ratio > MAX_COMPRESSION_RATIO && totalUncompressedSize > 10 * 1024 * 1024L) {
						return EpubValidationResult
							.failure("EPUB archive compression ratio exceeds security threshold");
					}
				}

				if ("mimetype".equalsIgnoreCase(name)) {
					mimetypeBytes = entryStream.toByteArray();
				}
				else if ("META-INF/container.xml".equalsIgnoreCase(name)) {
					containerXmlBytes = entryStream.toByteArray();
				}

				zis.closeEntry();
			}
		}
		catch (Exception e) {
			return EpubValidationResult.failure("Corrupt or unreadable ZIP archive: " + e.getMessage());
		}

		// 4. Mimetype Check
		if (mimetypeBytes == null) {
			return EpubValidationResult.failure("Invalid EPUB package: missing mimetype entry");
		}

		String mimetypeText = new String(mimetypeBytes).trim();
		if (!mimetypeText.startsWith("application/epub+zip")) {
			return EpubValidationResult.failure("Invalid EPUB package: invalid mimetype entry content");
		}

		// 5. META-INF/container.xml Check
		if (containerXmlBytes == null) {
			return EpubValidationResult.failure("Invalid EPUB package: missing META-INF/container.xml");
		}

		String rootfilePath = parseRootfilePath(containerXmlBytes);
		if (rootfilePath == null || rootfilePath.isBlank()) {
			return EpubValidationResult.failure("Invalid EPUB package: missing rootfile element in container.xml");
		}

		if (!entryNames.contains(rootfilePath)) {
			return EpubValidationResult
				.failure("Invalid EPUB package: rootfile '" + rootfilePath + "' not found in archive");
		}

		return EpubValidationResult.success(epubBytes.length, sha256Hash, rootfilePath);
	}

	private String parseRootfilePath(byte[] containerXmlBytes) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setExpandEntityReferences(false);
			factory.setNamespaceAware(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(containerXmlBytes));

			NodeList rootfiles = doc.getElementsByTagName("rootfile");
			for (int i = 0; i < rootfiles.getLength(); i++) {
				Element element = (Element) rootfiles.item(i);
				String mediaType = element.getAttribute("media-type");
				if ("application/oebps-package+xml".equals(mediaType) || mediaType.isBlank()) {
					String fullPath = element.getAttribute("full-path");
					if (fullPath != null && !fullPath.isBlank()) {
						return fullPath;
					}
				}
			}
		}
		catch (Exception e) {
			return null;
		}
		return null;
	}

}
