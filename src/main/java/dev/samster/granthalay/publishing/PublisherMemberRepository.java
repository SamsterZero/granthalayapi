package dev.samster.granthalay.publishing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface PublisherMemberRepository extends JpaRepository<PublisherMemberEntity, String> {

	List<PublisherMemberEntity> findByUserId(String userId);

	List<PublisherMemberEntity> findByPublisherId(String publisherId);

	Optional<PublisherMemberEntity> findByPublisherIdAndUserId(String publisherId, String userId);

	boolean existsByPublisherIdAndUserId(String publisherId, String userId);

}
