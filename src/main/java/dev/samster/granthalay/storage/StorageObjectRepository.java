package dev.samster.granthalay.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface StorageObjectRepository extends JpaRepository<StorageObjectEntity, String> {

	List<StorageObjectEntity> findByEditionIdOrderByVersionDesc(String editionId);

	Optional<StorageObjectEntity> findFirstByEditionIdOrderByVersionDesc(String editionId);

	Optional<StorageObjectEntity> findByEditionIdAndVersion(String editionId, int version);

	Optional<StorageObjectEntity> findByStorageKey(String storageKey);

}
