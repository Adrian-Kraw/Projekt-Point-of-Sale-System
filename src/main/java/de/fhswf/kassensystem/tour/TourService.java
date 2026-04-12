package de.fhswf.kassensystem.tour;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Zentrale Verwaltung aller Onboarding-Touren.
 *
 * Sonderaktionen per TourStep.action():
 *   "navigate:dashboard"  → navigiert zum Dashboard
 *   "navigate:kassieren"  → navigiert zur Kassieren-View
 *   "navigate:lager"      → navigiert zur Lager-View
 *   "demo-verkauf"        → legt Beispielartikel in den Warenkorb
 *   "open-zahlungsdialog" → öffnet den ZahlungsDialog
 *   "open-quittungsdialog"→ öffnet den QuittungsDialog
 */
@Service
public class TourService {

    private static final Map<String, List<TourStep>> TOURS = Map.of(

            // ── Kassierer: 15 Schritte ────────────────────────────────────────────
            "kassierer", List.of(

                    // 1 – Willkommen → immer zum Dashboard navigieren
                    new TourStep(null,
                            "Willkommen im Kassensystem!",
                            "Diese Einführung führt dich Schritt für Schritt durch alle wichtigen Funktionen. Klicke auf Weiter um zu beginnen.",
                            "center", "navigate:dashboard"),

                    // 2 – Kassieren-Nav → direkt navigieren (kein manuelles Klicken)
                    new TourStep("[tour-id='kassieren-nav']",
                            "Zur Kassenansicht",
                            "Hier gelangst du zur Kassenansicht um Verkäufe durchzuführen. Wir wechseln jetzt dorthin.",
                            "right", "navigate:kassieren"),

                    // 3 – Kategorie-Chips
                    new TourStep("[tour-id='kategorie-chips']",
                            "Kategorien filtern",
                            "Mit diesen Chips kannst du das Sortiment filtern – z.B. nur Kuchen oder Getränke.",
                            "bottom"),

                    // 4 – Artikel-Suche
                    new TourStep("[tour-id='artikel-suche']",
                            "Artikel suchen",
                            "Tippe hier einen Artikelnamen ein um gezielt zu suchen. Die Anzeige filtert sofort während du tippst.",
                            "bottom"),

                    // 5 – Artikel-Grid (kein Scroll)
                    new TourStep("[tour-id='artikel-grid']",
                            "Artikel in den Warenkorb",
                            "Klicke auf eine Artikelkarte um sie in den Warenkorb zu legen. Der Bestand wird dabei live aktualisiert.",
                            "center"),

                    // 6 – Warenkorb mit Beispielverkauf
                    new TourStep("[tour-id='warenkorb-spalte']",
                            "Der Warenkorb",
                            "Rechts siehst du den Warenkorb. Mit + und – passt du die Menge an, mit dem Mülleimer-Icon entfernst du Artikel wieder.",
                            "left", "demo-verkauf"),

                    // 7 – Rabatt + Bezahlen zusammen (zusammenfassung-Container)
                    new TourStep("[tour-id='zusammenfassung']",
                            "Rabatt & Bezahlen",
                            "Hier gibst du optional einen Rabatt in Prozent ein. Wenn alles stimmt, klicke auf 'Bezahlen' um die Zahlung abzuschließen.",
                            "top"),

                    // 8 – ZahlungsDialog öffnen und erklären (Demo, keine Abbuchung)
                    // position "dialog-left" → Tooltip erscheint links neben dem Dialog
                    new TourStep(null,
                            "Zahlungsart wählen",
                            "Nach dem Kassieren öffnet sich dieses Fenster. Hier wählst du ob der Kunde bar oder per Karte zahlt. Dies ist nur eine Demo – es wird nichts gebucht.",
                            "dialog-left", "open-zahlungsdialog"),

                    // 9 – QuittungsDialog erklären (Demo, kein Druck)
                    // position "dialog-left" → Tooltip erscheint links neben dem Dialog
                    new TourStep(null,
                            "Quittung drucken?",
                            "Nach der Zahlung kannst du dem Kunden eine Quittung als PDF erstellen. Dies ist nur eine Demo – es wird nichts gedruckt.",
                            "dialog-left", "open-quittungsdialog"),

                    // 10 – Lager-Nav → direkt navigieren
                    new TourStep("[tour-id='lager-nav']",
                            "Zur Lagerverwaltung",
                            "Im Lager behältst du den Bestand aller Artikel im Blick. Wir wechseln jetzt dorthin.",
                            "right", "navigate:lager"),

                    // 11 – Beide Statistik-Kacheln zusammen
                    new TourStep("[tour-id='statistik-karten']",
                            "Lagerübersicht",
                            "Hier siehst du auf einen Blick wie viele Artikel unter dem Minimalbestand sind (links) und wie viele Artikel insgesamt im System erfasst sind (rechts).",
                            "bottom"),

                    // 12 – Bestandstabelle: highlight ganzer Container, Tooltip mittig
                    new TourStep("[tour-id='bestand-tabelle-kopf']",
                            "Bestandsübersicht",
                            "In der Tabelle siehst du alle Artikel mit aktuellem Bestand, Minimalgrenze und Ampelstatus. Über das Suchfeld kannst du gezielt nach Artikeln filtern.",
                            "bottom"),

                    // 13 – Ampelstatus: Spotlight auf Status-Spalte, Tooltip mittig
                    new TourStep("[tour-id='status-spalte']",
                            "Ampelstatus",
                            "Die farbigen Punkte zeigen den Bestandsstatus:\n🟢 Grün – Bestand ok\n🟠 Orange – Bestand knapp\n🔴 Rot – unter Minimalbestand",
                            "center"),

                    // 14 – Logo → zum Dashboard navigieren, Tooltip bleibt auf Logo
                    new TourStep("[tour-id='logo-row']",
                            "Zurück zum Dashboard",
                            "Klicke jederzeit oben links auf 'Canapé Café' um zum Dashboard zurückzukehren. Wir gehen jetzt dorthin.",
                            "right", "navigate:dashboard"),

                    // 15 – UserCard → Spotlight auf UserCard, Tooltip zentriert
                    new TourStep("[tour-id='user-card']",
                            "Dein Account – Ende der Einführung",
                            "Links unten findest du deinen Account. Klicke darauf um dich am Ende deiner Schicht auszuloggen.\n\nViel Erfolg! 🎉",
                            "right")
            ),

            // ── Manager: 6 Schritte ───────────────────────────────────────────────
            "manager", List.of(

                    new TourStep(null,
                            "Willkommen im Kassensystem!",
                            "Als Manager hast du Zugriff auf alle Bereiche. Diese Einführung zeigt dir die wichtigsten Funktionen.",
                            "center", "navigate:dashboard"),

                    new TourStep("[tour-id='lager-nav']",
                            "Lagerverwaltung",
                            "Hier behältst du den Bestand aller Artikel im Blick und kannst Wareneingänge buchen.",
                            "right"),

                    new TourStep("[tour-id='artikel-nav']",
                            "Artikelverwaltung",
                            "Hier pflegst du das Sortiment: Artikel anlegen, Preise anpassen, Kategorien und Bilder verwalten.",
                            "right"),

                    new TourStep("[tour-id='berichte-nav']",
                            "Berichte & Auswertungen",
                            "Umsatzübersichten, Top-Seller, Zahlungsarten und Tagesabschluss – alles auch als PDF exportierbar.",
                            "right"),

                    new TourStep("[tour-id='benutzer-nav']",
                            "Benutzerverwaltung",
                            "Hier legst du neue Kassierer und Manager an, vergibst Passwörter und verwaltest Zugriffsrechte.",
                            "right"),

                    new TourStep("[tour-id='user-card']",
                            "Dein Account – Ende der Einführung",
                            "Links unten findest du deinen Account. Klicke darauf um dich am Ende auszuloggen.\n\nViel Erfolg! 🎉",
                            "right")
            )
    );

    public List<TourStep> getSteps(String tourId) {
        return TOURS.getOrDefault(tourId, List.of());
    }
}