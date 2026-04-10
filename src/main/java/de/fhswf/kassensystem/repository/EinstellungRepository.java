package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Einstellung;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EinstellungRepository extends JpaRepository<Einstellung, String> {
}