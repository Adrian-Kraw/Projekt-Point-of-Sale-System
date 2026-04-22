package de.fhswf.kassensystem.repository;

import de.fhswf.kassensystem.model.Kategorie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("KategorieRepository Tests")
class KategorieRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KategorieRepository kategorieRepository;

    @BeforeEach
    void setUp() {
        Kategorie kategorie = new Kategorie();
        kategorie.setName("Getränke");
        entityManager.persist(kategorie);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("Gibt Kategorie zurück wenn Name existiert")
        void findByName_erfolg() {
            Optional<Kategorie> result = kategorieRepository.findByName("Getränke");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Getränke");
        }

        @Test
        @DisplayName("Gibt leeres Optional zurück wenn Name nicht existiert")
        void findByName_nichtGefunden() {
            Optional<Kategorie> result = kategorieRepository.findByName("Snacks");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Suche ist case-sensitive")
        void findByName_caseSensitive() {
            Optional<Kategorie> result = kategorieRepository.findByName("getränke");

            assertThat(result).isEmpty();
        }
    }
}