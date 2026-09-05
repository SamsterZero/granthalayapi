package dev.samster.granthalay.catalog;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_titles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TitleEntity {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@Column(name = "slug", nullable = false, unique = true)
	private String slug;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "subtitle")
	private String subtitle;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "language", nullable = false, length = 10)
	private String language;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "title", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("displayOrder ASC")
	private List<TitleContributorEntity> titleContributors = new ArrayList<>();

	@OneToMany(mappedBy = "title", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EditionEntity> editions = new ArrayList<>();

	TitleEntity(String id, String slug, String title, String subtitle, String description, String language,
			Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.slug = slug;
		this.title = title;
		this.subtitle = subtitle;
		this.description = description;
		this.language = language;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
