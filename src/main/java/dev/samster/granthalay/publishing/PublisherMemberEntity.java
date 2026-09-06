package dev.samster.granthalay.publishing;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "publishing_publisher_members")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PublisherMemberEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "publisher_id", nullable = false)
	private PublisherEntity publisher;

	@Column(name = "user_id", nullable = false, length = 36)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 50)
	private PublisherMemberRole role;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	PublisherMemberEntity(String id, PublisherEntity publisher, String userId, PublisherMemberRole role,
			Instant createdAt) {
		this.id = id;
		this.publisher = publisher;
		this.userId = userId;
		this.role = role;
		this.createdAt = createdAt;
	}

}
