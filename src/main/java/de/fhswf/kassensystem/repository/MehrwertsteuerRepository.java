package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Mehrwertsteuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repository für die Mehrwertsteuersätze
 */
@Repository
public interface MehrwertsteuerRepository extends JpaRepository<Mehrwertsteuer, Long> {
    Optional<Mehrwertsteuer> findBySatz(BigDecimal satz);
}
