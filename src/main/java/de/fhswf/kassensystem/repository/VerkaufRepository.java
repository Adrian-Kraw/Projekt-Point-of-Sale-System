package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Verkauf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository für den Datenbankzugriff auf {@link Verkauf}-Entitäten.
 *
 * <p>
 *     Stellt neben den Standard-CRUD-Operationen aus {@link JpaRepository} zeitraumbasierte Abfragen
 *     für den Tagesabschluss und die Umsatzübersicht bereit.
 * </p>
 *
 * @author Paula Martin, Adrian Krawietz
 */
@Repository
public interface VerkaufRepository extends JpaRepository<Verkauf, Long> {

    /**
     * Gibt alle abgeschlossenen Verkäufe innerhalb eines Zeitraums zurück, inklusive
     * ihrer Positionen und zugehörigen Artikeldaten.
     *
     * <p>
     *     Die Positionen, Artikel und Kategorien werden per {@code JOINFETCH} in einer einzigen
     *     Datenbankabfrage geladen, um {@code LazyInitializationException} in Vaadin-Views außerhalb
     *     der JPA-Session zu vermeiden.
     * </p>
     *
     * <p>
     *     {@code DISTINCT} verhindert doppelte {@link Verkauf}-Einträge, die durch den Join über die
     *     Positionsliste entstehen würden.
     * </p>
     *
     * <p>
     *     Stornierte Verkäufe ({@code Status.STORNIERT}) werden bewusst ausgeschlossen, da sie nicht in Umsatzauswertungen
     *     einfließen sollen.
     * </p>
     *
     * @param start Beginn des Zeitraums (inklusiv)
     * @param end Ende des Zeitraums (inklusiv)
     * @return Liste aller abgeschlossener Verkäufe im angegebenen Zeitraum mit vollständig geladenen Positionen, oder
     *         eine leere Liste, wenn keine Verkäufe gefunden wurden.
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

    /**
     * Gibt den Verkauf mit der angegebenen ID zurück.
     *
     * @param verkaufId Der Datenbankidentifikator des gesuchten Verkaufs
     * @return der gefundene Verkauf oder {@code null}, wenn kein Verkauf mit der angegebenen ID existiert.
     */
    Verkauf findVerkaufById(Long verkaufId);
}
