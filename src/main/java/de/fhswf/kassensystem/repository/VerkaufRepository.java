package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Verkauf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository für Verkäufe
 */
@Repository
public interface VerkaufRepository extends JpaRepository<Verkauf, Long> {

    /**
     * Lädt alle Verkäufe in einem Zeitraum MIT Positionen (eager via JOIN FETCH).
     * Verhindert LazyInitializationException in Views außerhalb der DB-Session.
     */
    @Query("SELECT DISTINCT v FROM Verkauf v " +
            "LEFT JOIN FETCH v.positionen p " +
            "LEFT JOIN FETCH p.artikel a " +
            "LEFT JOIN FETCH a.kategorie " +
            "WHERE v.timestamp BETWEEN :start AND :end " +
            "AND v.status = de.fhswf.kassensystem.model.enums.Status.ABGESCHLOSSEN")
    List<Verkauf> findByTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    Verkauf getVerkaufById(Long verkaufId);
}
