package dev.samster.granthalay.catalog;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_contributors")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ContributorEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "bio", columnDefinition = "TEXT")
	private String bio;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	ContributorEntity(String id, String name, String bio, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.name = name;
		this.bio = bio;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
