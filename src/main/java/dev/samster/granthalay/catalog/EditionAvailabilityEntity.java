package dev.samster.granthalay.catalog;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "catalog_edition_availability")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class EditionAvailabilityEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "edition_id", nullable = false)
	private EditionEntity edition;

	@Column(name = "territory", nullable = false, length = 10)
	private String territory;

	@Column(name = "available_from")
	private Instant availableFrom;

	@Column(name = "available_until")
	private Instant availableUntil;

	@Column(name = "is_available", nullable = false)
	private boolean isAvailable;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	EditionAvailabilityEntity(String id, EditionEntity edition, String territory, Instant availableFrom,
			Instant availableUntil, boolean isAvailable, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.edition = edition;
		this.territory = territory;
		this.availableFrom = availableFrom;
		this.availableUntil = availableUntil;
		this.isAvailable = isAvailable;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
