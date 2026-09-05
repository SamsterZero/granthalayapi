package dev.samster.granthalay.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_title_contributors")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TitleContributorEntity {

	@EmbeddedId
	private TitleContributorId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("titleId")
	@JoinColumn(name = "title_id")
	private TitleEntity title;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("contributorId")
	@JoinColumn(name = "contributor_id")
	private ContributorEntity contributor;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	TitleContributorEntity(TitleEntity title, ContributorEntity contributor, ContributorRole role, int displayOrder) {
		this.id = new TitleContributorId(title.getId(), contributor.getId(), role);
		this.title = title;
		this.contributor = contributor;
		this.displayOrder = displayOrder;
	}

}
