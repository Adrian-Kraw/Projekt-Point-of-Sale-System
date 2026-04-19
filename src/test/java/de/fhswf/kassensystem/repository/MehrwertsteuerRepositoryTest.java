package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Mehrwertsteuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("MehrwertsteuerRepository Tests")
class MehrwertsteuerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MehrwertsteuerRepository mehrwertsteuerRepository;

    @BeforeEach
    void setUp() {
        Mehrwertsteuer mwst = new Mehrwertsteuer();
        mwst.setBezeichnung("Regelsteuersatz");
        mwst.setSatz(new BigDecimal("19.00"));
        entityManager.persist(mwst);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findBySatz")
    class FindBySatz {

        @Test
        @DisplayName("Gibt Mehrwertsteuersatz zurück wenn Satz existiert")
        void findBySatz_erfolg() {
            Optional<Mehrwertsteuer> result = mehrwertsteuerRepository
                    .findBySatz(new BigDecimal("19.00"));

            assertThat(result).isPresent();
            assertThat(result.get().getBezeichnung()).isEqualTo("Regelsteuersatz");
        }

        @Test
        @DisplayName("Gibt leeres Optional zurück wenn Satz nicht existiert")
        void findBySatz_nichtGefunden() {
            Optional<Mehrwertsteuer> result = mehrwertsteuerRepository
                    .findBySatz(new BigDecimal("7.00"));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Vergleicht numerisch — 19.00 und 19.0 sind gleich")
        void findBySatz_numerischerVergleich() {
            Optional<Mehrwertsteuer> result = mehrwertsteuerRepository
                    .findBySatz(new BigDecimal("19.0"));

            assertThat(result).isPresent();
        }
    }
}