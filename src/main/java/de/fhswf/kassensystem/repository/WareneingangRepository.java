package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Wareneingang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WareneingangRepository extends JpaRepository<Wareneingang, Long> {

}
