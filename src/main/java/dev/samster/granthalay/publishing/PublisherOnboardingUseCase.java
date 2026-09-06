package dev.samster.granthalay.publishing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PublisherOnboardingUseCase {

	private final PublisherRepository publisherRepository;

	private final PublisherMemberRepository memberRepository;

	PublisherOnboardingUseCase(PublisherRepository publisherRepository, PublisherMemberRepository memberRepository) {
		this.publisherRepository = publisherRepository;
		this.memberRepository = memberRepository;
	}

	public PublisherResponse onboardPublisher(OnboardPublisherRequest request, String userId) {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("User ID is required for publisher onboarding");
		}

		Instant now = Instant.now();
		PublisherEntity publisher = new PublisherEntity(UUID.randomUUID().toString(), request.name(),
				PublisherStatus.APPROVED, request.contactEmail(), request.payoutReference(), now, now);
		PublisherEntity savedPublisher = publisherRepository.save(publisher);

		PublisherMemberEntity member = new PublisherMemberEntity(UUID.randomUUID().toString(), savedPublisher, userId,
				PublisherMemberRole.OWNER, now);
		memberRepository.save(member);

		return toPublisherResponse(savedPublisher);
	}

	@Transactional(readOnly = true)
	public List<PublisherResponse> getPublishersForUser(String userId) {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("User ID is required");
		}

		return memberRepository.findByUserId(userId)
			.stream()
			.map(PublisherMemberEntity::getPublisher)
			.map(this::toPublisherResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public PublisherResponse getPublisherProfile(String publisherId, String requestingUserId) {
		PublisherEntity publisher = getPublisherAndVerifyAccess(publisherId, requestingUserId);
		return toPublisherResponse(publisher);
	}

	public PublisherResponse updatePublisherProfile(String publisherId, UpdatePublisherProfileRequest request,
			String requestingUserId) {
		PublisherEntity publisher = getPublisherAndVerifyAccess(publisherId, requestingUserId);

		publisher.setName(request.name());
		publisher.setContactEmail(request.contactEmail());
		publisher.setPayoutReference(request.payoutReference());
		publisher.setUpdatedAt(Instant.now());

		PublisherEntity updated = publisherRepository.save(publisher);
		return toPublisherResponse(updated);
	}

	public PublisherMemberResponse addPublisherMember(String publisherId, AddPublisherMemberRequest request,
			String requestingUserId) {
		PublisherEntity publisher = getPublisherAndVerifyAccess(publisherId, requestingUserId);

		if (memberRepository.existsByPublisherIdAndUserId(publisherId, request.userId())) {
			throw new IllegalStateException("User " + request.userId() + " is already a member of this publisher");
		}

		PublisherMemberRole role;
		try {
			role = PublisherMemberRole.valueOf(request.role().toUpperCase());
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Invalid member role: " + request.role());
		}

		Instant now = Instant.now();
		PublisherMemberEntity member = new PublisherMemberEntity(UUID.randomUUID().toString(), publisher,
				request.userId(), role, now);
		PublisherMemberEntity saved = memberRepository.save(member);

		return new PublisherMemberResponse(saved.getId(), saved.getPublisher().getId(), saved.getUserId(),
				saved.getRole().name(), saved.getCreatedAt());
	}

	private PublisherEntity getPublisherAndVerifyAccess(String publisherId, String userId) {
		PublisherEntity publisher = publisherRepository.findById(publisherId)
			.orElseThrow(() -> new IllegalArgumentException("Publisher not found: " + publisherId));

		boolean isMember = memberRepository.existsByPublisherIdAndUserId(publisherId, userId);
		if (!isMember) {
			throw new IllegalArgumentException(
					"User " + userId + " is not authorized to access publisher: " + publisherId);
		}

		return publisher;
	}

	private PublisherResponse toPublisherResponse(PublisherEntity publisher) {
		return new PublisherResponse(publisher.getId(), publisher.getName(), publisher.getStatus().name(),
				publisher.getContactEmail(), publisher.getPayoutReference(), publisher.getCreatedAt(),
				publisher.getUpdatedAt());
	}

}
