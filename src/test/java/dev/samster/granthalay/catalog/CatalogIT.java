package dev.samster.granthalay.catalog;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;

import dev.samster.granthalay.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogIT {

	@LocalServerPort
	int port;

	@Autowired
	ManageCatalogUseCase manageCatalogUseCase;

	@Autowired
	TitleRepository titleRepository;

	@Autowired
	ContributorRepository contributorRepository;

	@Autowired
	EditionRepository editionRepository;

	private HttpClient client;

	private String titleSlug;

	private String editionId;

	@BeforeEach
	void setUp() {
		editionRepository.deleteAll();
		titleRepository.deleteAll();
		contributorRepository.deleteAll();

		client = HttpClient.newHttpClient();
		titleSlug = "the-god-of-small-things-" + System.currentTimeMillis();

		var contributorId = manageCatalogUseCase.createContributor("Arundhati Roy",
				"Indian author and political activist.");
		var titleId = manageCatalogUseCase.createTitle(titleSlug, "The God of Small Things", "A Novel",
				"A story about the childhood experiences of fraternal twins...", "en");

		manageCatalogUseCase.addContributorToTitle(titleId, contributorId, ContributorRole.AUTHOR, 1);

		editionId = manageCatalogUseCase.addEdition(titleId, "9780679457312", EditionFormat.EPUB, 1, null,
				LocalDate.of(1997, 4, 4), EditionStatus.PUBLISHED);

		manageCatalogUseCase.setPrice(editionId, "USD", 1499, "GLOBAL");
		manageCatalogUseCase.setPrice(editionId, "INR", 49900, "IN");

		manageCatalogUseCase.setAvailability(editionId, "GLOBAL", Instant.now().minusSeconds(86400), null, true);
	}

	@Test
	void listsCatalogTitlesWithPaginationAndFiltering() throws Exception {
		var req = HttpRequest
			.newBuilder(URI
				.create("http://localhost:" + port + "/api/v1/catalog/titles?search=Small&language=en&page=0&size=10"))
			.GET()
			.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());

		assertThat(res.statusCode()).isEqualTo(200);
		assertThat(res.headers().firstValue("Content-Type").orElse("")).contains("application/json");
		assertThat(res.body()).contains(titleSlug);
		assertThat(res.body()).contains("The God of Small Things");
		assertThat(res.body()).contains("Arundhati Roy");
	}

	@Test
	void fetchesCatalogTitleDetailBySlug() throws Exception {
		var req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/catalog/titles/" + titleSlug))
			.GET()
			.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());

		assertThat(res.statusCode()).isEqualTo(200);
		assertThat(res.headers().firstValue("Content-Type").orElse("")).contains("application/json");
		assertThat(res.body()).contains(titleSlug);
		assertThat(res.body()).contains("The God of Small Things");
		assertThat(res.body()).contains("Arundhati Roy");
		assertThat(res.body()).contains("AUTHOR");
		assertThat(res.body()).contains("EPUB");
		assertThat(res.body()).contains("USD");
	}

	@Test
	void fetchesCatalogEditionById() throws Exception {
		var req = HttpRequest
			.newBuilder(URI.create("http://localhost:" + port + "/api/v1/catalog/editions/" + editionId))
			.GET()
			.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());

		assertThat(res.statusCode()).isEqualTo(200);
		assertThat(res.headers().firstValue("Content-Type").orElse("")).contains("application/json");
		assertThat(res.body()).contains(editionId);
		assertThat(res.body()).contains("PUBLISHED");
		assertThat(res.body()).contains("GLOBAL");
	}

	@Test
	void returnsProblemDetailsWhenTitleNotFound() throws Exception {
		var req = HttpRequest
			.newBuilder(URI.create("http://localhost:" + port + "/api/v1/catalog/titles/non-existent-slug-xyz"))
			.GET()
			.build();
		var res = client.send(req, HttpResponse.BodyHandlers.ofString());

		assertThat(res.statusCode()).isEqualTo(404);
		assertThat(res.headers().firstValue("Content-Type").orElse("")).contains("application/problem+json");
		assertThat(res.body()).contains("Not Found");
		assertThat(res.body()).contains("non-existent-slug-xyz");
	}

}
