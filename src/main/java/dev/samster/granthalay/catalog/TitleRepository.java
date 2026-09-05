package dev.samster.granthalay.catalog;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TitleRepository extends JpaRepository<TitleEntity, String> {

	Optional<TitleEntity> findBySlug(String slug);

	boolean existsBySlug(String slug);

	@Query("""
			SELECT DISTINCT t FROM TitleEntity t
			LEFT JOIN t.titleContributors tc
			LEFT JOIN tc.contributor c
			WHERE (:search IS NULL OR :search = '' OR
				LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
				LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
			AND (:language IS NULL OR :language = '' OR t.language = :language)
			""")
	Page<TitleEntity> findCatalogTitles(@Param("search") String search, @Param("language") String language,
			Pageable pageable);

}
