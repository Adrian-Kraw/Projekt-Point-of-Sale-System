package de.fhswf.kassensystem;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Spring Boot Anwendung für das Canapé Café Kassensystem.
 *
 * <p>{@code @SpringBootApplication} kombiniert drei Annotations:
 * <ul>
 *   <li>{@code @Configuration} – diese Klasse kann Spring Beans definieren</li>
 *   <li>{@code @EnableAutoConfiguration} – Spring konfiguriert sich automatisch
 *       anhand der vorhandenen Dependencies (z.B. Vaadin, JPA, Security)</li>
 *   <li>{@code @ComponentScan} – Spring sucht alle {@code @Component}, {@code @Service},
 *       {@code @Repository} etc. im Package {@code de.fhswf.kassensystem}</li>
 * </ul>
 *
 * <p>{@code AppShellConfigurator} ist ein Vaadin-Interface, das diese Klasse als
 * Konfigurationsquelle für den HTML-Shell markiert – das äußerste HTML-Gerüst
 * der Anwendung. Nur eine Klasse pro Projekt darf dieses Interface implementieren.
 *
 * <p>{@code @Theme("kassensystem")} weist Vaadin an, das Theme "kassensystem" zu laden.
 * Vaadin sucht dafür automatisch die Datei:
 * {@code src/main/frontend/themes/kassensystem/styles.css}
 *
 * @author Adrian Krawietz & Paula Martin
 */
@Push
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