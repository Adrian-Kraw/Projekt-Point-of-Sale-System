package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Kategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KategorieRepository extends JpaRepository<Kategorie, Long> {
    Optional<Kategorie> findByName(String name);
}
