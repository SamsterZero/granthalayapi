package dev.samster.granthalay.publishing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface PublishingAuditEventRepository extends JpaRepository<PublishingAuditEventEntity, String> {

	List<PublishingAuditEventEntity> findBySubmissionIdOrderByCreatedAtAsc(String submissionId);

}
