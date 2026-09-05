package dev.samster.granthalay.catalog;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TitleContributorId implements Serializable {

	@Column(name = "title_id", nullable = false, length = 36)
	private String titleId;

	@Column(name = "contributor_id", nullable = false, length = 36)
	private String contributorId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 50)
	private ContributorRole role;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		TitleContributorId that = (TitleContributorId) o;
		return Objects.equals(titleId, that.titleId) && Objects.equals(contributorId, that.contributorId)
				&& role == that.role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(titleId, contributorId, role);
	}

}
