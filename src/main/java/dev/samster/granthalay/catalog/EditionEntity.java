package dev.samster.granthalay.catalog;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_editions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class EditionEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "title_id", nullable = false)
	private TitleEntity title;

	@Column(name = "isbn", unique = true, length = 20)
	private String isbn;

	@Enumerated(EnumType.STRING)
	@Column(name = "format", nullable = false, length = 50)
	private EditionFormat format;

	@Column(name = "edition_number", nullable = false)
	private int editionNumber;

	@Column(name = "publisher_id", length = 36)
	private String publisherId;

	@Column(name = "published_date")
	private LocalDate publishedDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private EditionStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "edition", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EditionPriceEntity> prices = new ArrayList<>();

	@OneToMany(mappedBy = "edition", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EditionAvailabilityEntity> availability = new ArrayList<>();

	EditionEntity(String id, TitleEntity title, String isbn, EditionFormat format, int editionNumber,
			String publisherId, LocalDate publishedDate, EditionStatus status, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.title = title;
		this.isbn = isbn;
		this.format = format;
		this.editionNumber = editionNumber;
		this.publisherId = publisherId;
		this.publishedDate = publishedDate;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
