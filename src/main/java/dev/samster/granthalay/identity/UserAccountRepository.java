package dev.samster.granthalay.identity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UserAccountRepository extends JpaRepository<UserAccount, String> {

	Optional<UserAccount> findByEmailIgnoreCase(String email);

	Optional<UserAccount> findByVerificationToken(String verificationToken);

	boolean existsByEmailIgnoreCase(String email);

}
