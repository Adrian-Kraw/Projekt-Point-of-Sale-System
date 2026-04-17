package de.fhswf.kassensystem.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Willkommens-Ansicht des Kassensystems (Dashboard).
 *
 * <p>Wird nach dem Login als Startseite angezeigt und ist über das Logo
 * sowie den Benutzernamen in der Sidebar jederzeit erreichbar.
 * Nicht eingeloggte Benutzer werden in {@code beforeEnter} zur Login-Seite weitergeleitet.
 *
 * <p>Aufbau der View:
 * <ul>
 *   <li>Zwei dekorative, animierte Blur-Blobs im Hintergrund (rein visuell,
 *       {@code pointer-events: none})</li>
 *   <li>Zentrierter Begrüßungstext mit dem Café-Namen als zweizeilige H1-Überschrift</li>
 * </ul>
 *
 * <p>Zugriff: Rollen {@code KASSIERER} und {@code MANAGER}.
 *
 * @author Adrian
 */
@RolesAllowed({"KASSIERER", "MANAGER"})
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends Div implements BeforeEnterObserver {

    public DashboardView() {
        setSizeFull();
        getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("min-height", "100%")
                .set("padding", "2rem")
                .set("box-sizing", "border-box")
                .set("background", "linear-gradient(135deg, #fcf8ff 0%, #fdf0e8 100%)")
                .set("position", "relative")
                .set("overflow", "hidden");

        add(
                buildBlobBottom(),
                buildBlobTop(),
                buildContent()
        );
    }

    /**
     * Erstellt den dekorativen Blur-Blob unten rechts.
     *
     * Die Blobs sind rein visuell und nicht interaktiv.
     * pointer-events: none verhindert dass sie Klicks abfangen.
     */
    private Div buildBlobBottom() {
        Div blob = new Div();
        blob.getStyle()
                .set("position", "fixed")
                .set("bottom", "3rem")
                .set("right", "3rem")
                .set("width", "16rem")
                .set("height", "16rem")
                .set("background", "rgba(255,220,198,0.4)")
                .set("border-radius", "9999px")
                .set("filter", "blur(100px)")
                .set("pointer-events", "none")
                .set("z-index", "0");
        return blob;
    }

    /**
     * Erstellt den dekorativen Blur-Blob oben rechts.
     */
    private Div buildBlobTop() {
        Div blob = new Div();
        blob.getStyle()
                .set("position", "fixed")
                .set("top", "6rem")
                .set("right", "6rem")
                .set("width", "12rem")
                .set("height", "12rem")
                .set("background", "rgba(200,232,243,0.25)")
                .set("border-radius", "9999px")
                .set("filter", "blur(80px)")
                .set("pointer-events", "none")
                .set("z-index", "0");
        return blob;
    }

    /**
     * Erstellt den zentralen Content-Bereich mit Überschrift und Untertitel.
     *
     * Die Überschrift besteht aus zwei Zeilen:
     * - "Willkommen bei" (fett, dunkelbraun)
     * - "Canapé Café" (leicht, kursiv, etwas heller)
     *
     * Beide Spans sind in einem gemeinsamen H1 verschachtelt.
     * display:block auf dem ersten Span erzeugt den Zeilenumbruch
     * ohne ein br-Tag zu benötigen.
     */
    private VerticalLayout buildContent() {
        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("text-align", "center")
                .set("max-width", "42rem")
                .set("z-index", "10")
                .set("position", "relative")
                .set("gap", "0");

        content.add(
                buildHeadline()
        );
        return content;
    }

    /**
     * Erstellt die zweizeilige Hauptüberschrift.
     *
     * H1 ist semantisch korrekt für die wichtigste Überschrift der Seite.
     * Die zwei Spans innerhalb des H1 werden durch display:block
     * auf separaten Zeilen dargestellt.
     *
     * clamp(2.5rem, 6vw, 4.5rem) skaliert die Schriftgröße
     * responsiv zwischen Mindest- und Maximalwert.
     */
    private H1 buildHeadline() {
        H1 headline = new H1();
        headline.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("line-height", "1.1")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Span willkommen = new Span("Willkommen bei");
        willkommen.getStyle()
                .set("display", "block")
                .set("font-size", "clamp(2.5rem, 6vw, 4.5rem)")
                .set("font-weight", "800")
                .set("color", "#553722")
                .set("letter-spacing", "-0.025em");

        Span cafeName = new Span("Canapé Café");
        cafeName.getStyle()
                .set("display", "block")
                .set("font-size", "clamp(2.5rem, 6vw, 4.5rem)")
                .set("font-weight", "300")
                .set("font-style", "italic")
                .set("color", "#50453e")
                .set("letter-spacing", "-0.025em");

        headline.add(willkommen, cafeName);
        return headline;
    }

    /**
     * Prüft vor dem Rendern der Seite ob der Benutzer eingeloggt ist.
     *
     * <p>Ist kein gültiger Benutzer im {@code SecurityContext} vorhanden,
     * entweder weil keine Authentifizierung existiert oder es sich um einen
     * anonymen Benutzer handelt – wird direkt zur Login-Seite weitergeleitet.
     *
     * @param event enthält Informationen zur aktuellen Navigation
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            event.rerouteTo("login");
        }
    }

}