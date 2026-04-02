package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Verkauf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository für Verkäufe
 */
@Repository
public interface VerkaufRepository extends JpaRepository<Verkauf, Long> {

    /**
     * Gibt alle Verkäufe an einem bestimmten Datum als Liste zurück.
     * @param date Datum
     * @return Liste der Verkäufe
     */
    List<Verkauf> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
