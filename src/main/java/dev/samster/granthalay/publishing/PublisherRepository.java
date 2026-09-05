package dev.samster.granthalay.publishing;

import org.springframework.data.jpa.repository.JpaRepository;

interface PublisherRepository extends JpaRepository<PublisherEntity, String> {

}
