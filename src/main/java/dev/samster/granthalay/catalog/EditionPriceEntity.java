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
@Table(name = "catalog_edition_prices")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class EditionPriceEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "edition_id", nullable = false)
	private EditionEntity edition;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "amount_in_cents", nullable = false)
	private long amountInCents;

	@Column(name = "territory", nullable = false, length = 10)
	private String territory;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	EditionPriceEntity(String id, EditionEntity edition, String currency, long amountInCents, String territory,
			Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.edition = edition;
		this.currency = currency;
		this.amountInCents = amountInCents;
		this.territory = territory;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
