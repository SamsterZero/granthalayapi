package dev.samster.granthalay.publishing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import dev.samster.granthalay.TestcontainersConfiguration;
import dev.samster.granthalay.catalog.EditionFormat;
import dev.samster.granthalay.catalog.EditionStatus;
import dev.samster.granthalay.catalog.ManageCatalogUseCase;
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
class PublishingIT {

	@Autowired
	PublisherReleaseUseCase releaseUseCase;

	@Autowired
	PublisherOnboardingUseCase onboardingUseCase;

	@Autowired
	ManageCatalogUseCase manageCatalogUseCase;

	@Autowired
	PublisherRepository publisherRepository;

	@Autowired
	SubmissionRepository submissionRepository;

	@Autowired
	PublishingAuditEventRepository auditEventRepository;

	private PublisherEntity publisher;

	private String editionId;

	@BeforeEach
	void setUp() {
		publisher = releaseUseCase.createPublisher("Penguin Random House", "contact@penguin.example");

		var titleId = manageCatalogUseCase.createTitle("1984-george-orwell-" + System.currentTimeMillis(), "1984",
				"A Novel", "Dystopian social science fiction novel", "en");

		editionId = manageCatalogUseCase.addEdition(titleId, "9780451524935", EditionFormat.EPUB, 1, publisher.getId(),
				LocalDate.of(1949, 6, 8), EditionStatus.DRAFT);
	}

	@Test
	void executesFullSubmissionLifecycleSubmitApprovePublishWithdraw() {
		// 1. Submit Edition
		SubmitEditionRequest submitReq = new SubmitEditionRequest(publisher.getId(), editionId, "1984",
				"9780451524935");
		SubmissionResponse sub = releaseUseCase.submitEdition(submitReq, "publisher-admin");
		assertThat(sub.status()).isEqualTo("SUBMITTED");

		// 2. Review & Approve
		ReviewSubmissionRequest reviewReq = new ReviewSubmissionRequest(true, null, "reviewer-1");
		sub = releaseUseCase.reviewSubmission(sub.id(), reviewReq);
		assertThat(sub.status()).isEqualTo("APPROVED");

		// 3. Publish Release
		PublishReleaseRequest publishReq = new PublishReleaseRequest(null, "publisher-admin");
		sub = releaseUseCase.publishRelease(sub.id(), publishReq);
		assertThat(sub.status()).isEqualTo("PUBLISHED");

		// 4. Update Territory Availability
		UpdateAvailabilityRequest availReq = new UpdateAvailabilityRequest("GLOBAL", Instant.now(), null, true,
				"publisher-admin");
		releaseUseCase.updateTerritoryAvailability(editionId, availReq);

		// 5. Withdraw Release
		WithdrawReleaseRequest withdrawReq = new WithdrawReleaseRequest("Rights expired", "publisher-admin");
		sub = releaseUseCase.withdrawRelease(sub.id(), withdrawReq);
		assertThat(sub.status()).isEqualTo("WITHDRAWN");

		// 6. Audit Trail Verification
		List<PublishingAuditEventResponse> history = releaseUseCase.getSubmissionHistory(sub.id());
		assertThat(history).hasSize(4);
		assertThat(history.get(0).action()).isEqualTo("SUBMIT");
		assertThat(history.get(1).action()).isEqualTo("APPROVE");
		assertThat(history.get(2).action()).isEqualTo("PUBLISH");
		assertThat(history.get(3).action()).isEqualTo("WITHDRAW");
	}

	@Test
	void rejectsInvalidStateTransitions() {
		SubmitEditionRequest submitReq = new SubmitEditionRequest(publisher.getId(), editionId, "1984",
				"9780451524935");
		SubmissionResponse sub = releaseUseCase.submitEdition(submitReq, "publisher-admin");

		// Cannot publish directly from SUBMITTED state without approval
		PublishReleaseRequest publishReq = new PublishReleaseRequest(null, "publisher-admin");
		assertThatThrownBy(() -> releaseUseCase.publishRelease(sub.id(), publishReq))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Cannot publish submission in status: SUBMITTED");
	}

	@Test
	void onboardPublisherAndManageTeamMembers() {
		String ownerUserId = "user-owner-1";

		// 1. Onboard Publisher Profile
		OnboardPublisherRequest onboardReq = new OnboardPublisherRequest("HarperCollins", "contact@harper.example",
				"payout_ref_harper_100");
		PublisherResponse publisherRes = onboardingUseCase.onboardPublisher(onboardReq, ownerUserId);

		assertThat(publisherRes.name()).isEqualTo("HarperCollins");
		assertThat(publisherRes.contactEmail()).isEqualTo("contact@harper.example");
		assertThat(publisherRes.payoutReference()).isEqualTo("payout_ref_harper_100");
		assertThat(publisherRes.status()).isEqualTo("APPROVED");

		// 2. Fetch User's Associated Publishers
		List<PublisherResponse> myPublishers = onboardingUseCase.getPublishersForUser(ownerUserId);
		assertThat(myPublishers).extracting(PublisherResponse::id).contains(publisherRes.id());

		// 3. Add Team Member
		AddPublisherMemberRequest memberReq = new AddPublisherMemberRequest("user-editor-2", "EDITOR");
		PublisherMemberResponse memberRes = onboardingUseCase.addPublisherMember(publisherRes.id(), memberReq,
				ownerUserId);
		assertThat(memberRes.userId()).isEqualTo("user-editor-2");
		assertThat(memberRes.role()).isEqualTo("EDITOR");

		// 4. Update Publisher Profile
		UpdatePublisherProfileRequest updateReq = new UpdatePublisherProfileRequest("HarperCollins Global",
				"updated@harper.example", "payout_ref_harper_200");
		PublisherResponse updated = onboardingUseCase.updatePublisherProfile(publisherRes.id(), updateReq, ownerUserId);
		assertThat(updated.name()).isEqualTo("HarperCollins Global");
		assertThat(updated.contactEmail()).isEqualTo("updated@harper.example");
		assertThat(updated.payoutReference()).isEqualTo("payout_ref_harper_200");

		// 5. Tenant Isolation / Authorization Enforcement
		String unauthorizedUserId = "unauthorized-user-999";
		assertThatThrownBy(() -> onboardingUseCase.getPublisherProfile(publisherRes.id(), unauthorizedUserId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("unauthorized-user-999 is not authorized");
	}

}
