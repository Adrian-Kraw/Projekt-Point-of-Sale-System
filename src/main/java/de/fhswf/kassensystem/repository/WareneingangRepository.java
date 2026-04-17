package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.model.enums.WareneingangStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository für den Datenbankzugriff auf {@link Wareneingang}-Entitäten.
 *
 * <p>
 *     Stellt neben den Standard-CRUD-Operationen aus {@link JpaRepository} eine statusbasierte
 *     Abfrage bereit, die zum Beispiel für die Verwaltung ausstehender Wareneingänge durch den Manager genutzt wird.
 * </p>
 *
 * @author Paula Martin
 */
@Repository
public interface WareneingangRepository extends JpaRepository<Wareneingang, Long> {

    /**
     * Gibt alle Wareneingänge mit dem angegebenen Status zurück.
     *
     * @param status der gesuchte Bearbeitungsstatus
     * @return Liste aller Wareneingänge mit dem angegebenen Status, oder eine leere Liste, wenn keine
     *         Einträge gefunden wurden.
     * @see WareneingangStatus
     */
    List<Wareneingang> findByStatus(WareneingangStatus status);
}
