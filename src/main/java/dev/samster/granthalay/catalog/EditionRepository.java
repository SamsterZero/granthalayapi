package dev.samster.granthalay.catalog;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface EditionRepository extends JpaRepository<EditionEntity, String> {

	Optional<EditionEntity> findByIsbn(String isbn);

	boolean existsByIsbn(String isbn);

}
