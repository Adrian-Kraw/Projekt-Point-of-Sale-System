package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Wareneingang;
import de.fhswf.kassensystem.model.enums.WareneingangStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WareneingangRepository extends JpaRepository<Wareneingang, Long> {

    /**
     *
     * @param status
     * @return
     */
    List<Wareneingang> findByStatus(WareneingangStatus status);
}
