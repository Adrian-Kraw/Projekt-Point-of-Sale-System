package de.fhswf.kassensystem;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Kassensystem-Anwendung.
 *
 * @SpringBootApplication kombiniert drei Annotations:
 *   - @Configuration: diese Klasse kann Spring Beans definieren
 *   - @EnableAutoConfiguration: Spring konfiguriert sich automatisch
 *     anhand der vorhandenen Dependencies (z.B. Vaadin, JPA, Security)
 *   - @ComponentScan: Spring sucht alle @Component, @Service,
 *     @Repository etc. im Package de.fhswf.kassensystem
 *
 * AppShellConfigurator ist ein Vaadin-Interface das diese Klasse als
 * Konfigurationsklasse für den HTML-Shell markiert – das äußerste
 * HTML-Gerüst der Anwendung. Nur eine Klasse pro Projekt darf
 * dieses Interface implementieren.
 *
 * @Theme("kassensystem") weist Vaadin an das Theme "kassensystem" zu laden.
 * Vaadin sucht dafür automatisch die Datei:
 * src/main/frontend/themes/kassensystem/styles.css
 */
@SpringBootApplication
@Theme("kassensystem")
public class KassensystemApplication implements AppShellConfigurator {

	/**
	 * Startet die Spring Boot Anwendung.
	 * Spring Boot startet einen eingebetteten Tomcat-Server
	 * und macht die Anwendung unter localhost:8080 erreichbar.
	 *
	 * @param args optionale Kommandozeilenargumente
	 */
	public static void main(String[] args) {
		SpringApplication.run(KassensystemApplication.class, args);
	}
}