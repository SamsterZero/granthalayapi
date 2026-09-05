package dev.samster.granthalay;

import java.time.Instant;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.core.EventPublicationRepository;
import org.springframework.modulith.events.core.PublicationTargetIdentifier;
import org.springframework.modulith.events.core.TargetEventPublication;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DatabaseIT {

	@Autowired
	Flyway flyway;

	@Autowired
	SessionRepository<? extends Session> sessions;

	@Autowired
	EventPublicationRepository publications;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void migrationsAreAppliedAndDoNotRepeat() {
		flyway.validate();
		assertThat(flyway.info().applied()).hasSize(3);

		assertThat(flyway.info().pending()).isEmpty();
		assertThat(flyway.migrate().migrationsExecuted).isZero();
	}

	@Test
	void sessionsRoundTripAndDeletionCascadesToAttributes() {
		verifySessionPersistence(sessions);
	}

	private <S extends Session> void verifySessionPersistence(SessionRepository<S> repository) {
		var session = repository.createSession();
		session.setAttribute("test", "round-trip");
		repository.save(session);
		var primaryId = jdbc.queryForObject("select primary_id from spring_session where session_id = ?", String.class,
				session.getId());
		try {
			assertThat(repository.findById(session.getId()).<String>getAttribute("test")).isEqualTo("round-trip");
		}
		finally {
			repository.deleteById(session.getId());
		}
		assertThat(repository.findById(session.getId())).isNull();
		assertThat(jdbc.queryForObject("select count(*) from spring_session_attributes where session_primary_id = ?",
				Long.class, primaryId))
			.isZero();
	}

	@Test
	@Transactional
	void eventPublicationsPersistAndComplete() {
		var event = new TestEvent("round-trip");
		var target = PublicationTargetIdentifier.of("test-listener");
		var publication = publications.create(TargetEventPublication.of(event, target));
		assertThat(publications.findIncompletePublications()).singleElement()
			.satisfies(stored -> assertThat(stored.getEvent()).isEqualTo(event));
		publications.markCompleted(publication.getIdentifier(), Instant.now());
		assertThat(publications.findIncompletePublications()).isEmpty();
		assertThat(jdbc.queryForObject("select count(*) from event_publication where completion_date is not null",
				Long.class))
			.isEqualTo(1);
	}

	public record TestEvent(String value) {
	}

}
