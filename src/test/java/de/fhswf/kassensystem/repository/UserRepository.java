package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository für die Benutzer in dem System.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // ...
}
