package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Mehrwertsteuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repsoitory für den Datenbankzugriff auf {@link Mehrwertsteuer}-Entitäten.
 *
 * <p>
 *     Stellt neben den Standard-CRUD-Operationen aus {@link JpaRepository} eine Suche nach dem prozentualen Steuersatz
 *     bereit.
 * </p>
 *
 * @author Paula Martin, Adrian Krawietz
 */
@Repository
public interface MehrwertsteuerRepository extends JpaRepository<Mehrwertsteuer, Long> {

    /**
     * Sucht einen Mehrwertsteuersatz anhand seines exakten Prozentwertes.
     *
     * @param satz der gesuchte Steuersatz als Dezimalzahl
     * @return ein {@code Optional} mit dem gefundenen Mehrwertsteuersatz, oder {@code Optional.empty()} wenn kein
     *         Eintrag mit dem angegebenen Satz existiert.
     */
    Optional<Mehrwertsteuer> findBySatz(BigDecimal satz);
}
