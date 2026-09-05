package dev.samster.granthalay.publishing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface SubmissionRepository extends JpaRepository<SubmissionEntity, String> {

	List<SubmissionEntity> findByPublisherId(String publisherId);

}
