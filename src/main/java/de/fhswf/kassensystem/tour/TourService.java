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

                    // 12 – Nachbestellhinweise
                    new TourStep("[tour-id='nachbestell-block']",
                            "Nachbestellhinweise",
                            "Wenn ein Artikel unter den Minimalbestand fällt, erscheint er hier. So siehst du sofort was nachbestellt werden muss.",
                            "bottom", "demo-nachbestell"),

                    // 13 – Lieferungsbescheid
                    new TourStep("[tour-id='lieferung-block']",
                            "Lieferungsbescheid",
                            "Wurde eine Bestellung aufgegeben, erscheint sie hier. Du kannst bestätigen dass die Lieferung angekommen ist – erst dann wird der Bestand aktualisiert.",
                            "bottom", "demo-lieferung"),

                    // 14 – Bestandstabelle
                    new TourStep("[tour-id='bestand-tabelle-kopf']",
                            "Bestandsübersicht",
                            "In der Tabelle siehst du alle Artikel mit aktuellem Bestand, Minimalgrenze und Ampelstatus. Über das Suchfeld kannst du gezielt nach Artikeln filtern.",
                            "bottom"),

                    // 15 – Ampelstatus
                    new TourStep("[tour-id='status-spalte']",
                            "Ampelstatus",
                            "Die farbigen Punkte zeigen den Bestandsstatus:\n🟢 Grün – Bestand ok\n🟠 Orange – Bestand knapp\n🔴 Rot – unter Minimalbestand",
                            "center"),

                    // 16 – Logo → zum Dashboard navigieren
                    new TourStep("[tour-id='logo-row']",
                            "Zurück zum Dashboard",
                            "Klicke jederzeit oben links auf 'Canapé Café' um zum Dashboard zurückzukehren. Wir gehen jetzt dorthin.",
                            "right", "navigate:dashboard"),

                    // 17 – UserCard
                    new TourStep("[tour-id='user-card']",
                            "Dein Account – Ende der Einführung",
                            "Links unten findest du deinen Account. Klicke darauf um dich am Ende deiner Schicht auszuloggen.\n\nViel Erfolg! 🎉",
                            "right")
            ),

            // ── Manager: vollständige Tour ────────────────────────────────────────
            "manager", List.of(

                    // 1 – Willkommen
                    new TourStep(null,
                            "Hallo! 👋",
                            "Schön, dass du dabei bist. Diese Einführung zeigt dir alle Bereiche – Kassieren, Lager, Artikel, Berichte und Benutzerverwaltung.",
                            "center", "navigate:dashboard"),

                    // ── KASSIEREN ──────────────────────────────────────────────────

                    // 2 – Kassieren-Nav
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

                    // 5 – Artikel-Grid
                    new TourStep("[tour-id='artikel-grid']",
                            "Artikel in den Warenkorb",
                            "Klicke auf eine Artikelkarte um sie in den Warenkorb zu legen. Der Bestand wird dabei live aktualisiert.",
                            "center"),

                    // 6 – Warenkorb
                    new TourStep("[tour-id='warenkorb-spalte']",
                            "Der Warenkorb",
                            "Rechts siehst du den Warenkorb. Mit + und – passt du die Menge an, mit dem Mülleimer-Icon entfernst du Artikel wieder.",
                            "left", "demo-verkauf"),

                    // 7 – Rabatt & Bezahlen
                    new TourStep("[tour-id='zusammenfassung']",
                            "Rabatt & Bezahlen",
                            "Hier gibst du optional einen Rabatt in Prozent ein. Wenn alles stimmt, klicke auf 'Bezahlen' um die Zahlung abzuschließen.",
                            "top"),

                    // 8 – Zahlungsart
                    new TourStep(null,
                            "Zahlungsart wählen",
                            "Nach dem Kassieren öffnet sich dieses Fenster. Hier wählst du ob der Kunde bar oder per Karte zahlt. Dies ist nur eine Demo – es wird nichts gebucht.",
                            "dialog-left", "open-zahlungsdialog"),

                    // 9 – Quittung
                    new TourStep(null,
                            "Quittung drucken?",
                            "Nach der Zahlung kannst du dem Kunden eine Quittung als PDF erstellen. Dies ist nur eine Demo – es wird nichts gedruckt.",
                            "dialog-left", "open-quittungsdialog"),

                    // ── LAGER ──────────────────────────────────────────────────────

                    // 10 – Lager Navigation
                    new TourStep("[tour-id='lager-nav']",
                            "Lagerverwaltung",
                            "Hier behältst du den Überblick über alle Bestände. Wir schauen kurz rein.",
                            "right", "navigate:lager"),

                    // 3 – Statistik-Karten
                    new TourStep("[tour-id='statistik-karten']",
                            "Auf einen Blick",
                            "Links siehst du wie viele Artikel unter dem Minimalbestand liegen und nachbestellt werden sollten. Rechts die Gesamtzahl aller Artikel.",
                            "bottom"),

                    // 4 – Lager-Aktionen Karte
                    new TourStep("[tour-id='lager-aktionen-karte']",
                            "Wareneingang buchen",
                            "Kam eine Lieferung an? Hier buchst du den Eingang direkt – der Bestand wird sofort aktualisiert.",
                            "bottom"),

                    // 5 – Bestandseingang buchen Dialog
                    new TourStep(null,
                            "So funktioniert's",
                            "Artikel auswählen, Menge eingeben, speichern – fertig. Dies ist nur eine Demo, es wird nichts gespeichert.",
                            "dialog-left", "open-wareneingang-dialog"),

                    // 6 – Nachbestellhinweise
                    new TourStep("[tour-id='nachbestell-block']",
                            "Nachbestellhinweise",
                            "Fällt ein Artikel unter den Minimalbestand, erscheint er hier – solange keine offene Bestellung dafür existiert.",
                            "bottom", "demo-nachbestell"),

                    // 7 – Lieferungsbescheid
                    new TourStep("[tour-id='lieferung-block']",
                            "Lieferungsbescheid",
                            "Wurde eine Bestellung aufgegeben, erscheint sie hier. Erst nach Bestätigung wird der Bestand aktualisiert – Kassierer und Manager können bestätigen.",
                            "bottom", "demo-lieferung"),

                    // 8 – Bestandsübersicht Tabelle
                    new TourStep("[tour-id='bestand-tabelle-kopf']",
                            "Bestandsübersicht",
                            "Alle Artikel mit aktuellem Bestand, Minimalgrenze und Ampelstatus. Oben rechts kannst du nach einem Artikel suchen.",
                            "bottom"),

                    // 9 – Ampelstatus
                    new TourStep("[tour-id='status-spalte']",
                            "Ampelstatus",
                            "Die Punkte zeigen den Bestandsstatus:\n🟢 Grün – alles ok\n🟠 Orange – wird knapp\n🔴 Rot – unter Minimalbestand",
                            "center"),

                    // ── ARTIKEL ────────────────────────────────────────────────────

                    // 8 – Artikel Navigation
                    new TourStep("[tour-id='artikel-nav']",
                            "Artikelverwaltung",
                            "Hier pflegst du das Sortiment – Artikel anlegen, Preise anpassen, Artikel deaktivieren. Wir schauen kurz rein.",
                            "right", "navigate:artikel"),

                    // 9 – Artikel Suche
                    new TourStep("[tour-id='artikel-suchfeld']",
                            "Artikel suchen",
                            "Tippe einen Namen ein – die Liste filtert sofort. Praktisch wenn du einen bestimmten Artikel schnell finden willst.",
                            "bottom"),

                    // 10 – Neuer Artikel Button
                    new TourStep("[tour-id='neuer-artikel-btn']",
                            "Neuen Artikel anlegen",
                            "Hier legst du neue Artikel an: Name, Kategorie, Preis, Mehrwertsteuer, Bestand und optional ein Bild.",
                            "left"),

                    // 11 – Neuer Artikel Dialog
                    new TourStep(null,
                            "Artikel anlegen",
                            "Felder ausfüllen, speichern – der Artikel ist sofort im Kassensystem verfügbar. Dies ist nur eine Demo.",
                            "dialog-left", "open-neuer-artikel-dialog"),

                    // 12 – Artikel Tabelle
                    new TourStep("[tour-id='artikel-tabelle']",
                            "Alle Artikel auf einen Blick",
                            "Hier siehst du ID, Name, Kategorie, Preis, Mehrwertsteuer, Bestand, Minimalgrenze und ob der Artikel gerade aktiv ist.",
                            "center"),

                    // 13 – Aktionen Spalte
                    new TourStep("[tour-id='artikel-aktionen-header']",
                            "Artikel bearbeiten & verwalten",
                            "Jede Zeile hat zwei Aktionen:\n✏️ Stift – Artikel bearbeiten\n🚫 Auge – Artikel aus dem Verkauf nehmen\nInaktive Artikel erscheinen nicht mehr im Kassensystem.",
                            "left"),

                    // ── BERICHTE ───────────────────────────────────────────────────

                    // 14 – Berichte Navigation
                    new TourStep("[tour-id='berichte-nav']",
                            "Berichte & Auswertungen",
                            "Hier siehst du alle Umsatz- und Verkaufsdaten. Wir schauen kurz rein.",
                            "right", "navigate:berichte"),

                    // 15 – PDF Export
                    new TourStep("[tour-id='pdf-export-btn']",
                            "PDF-Export",
                            "Exportiert den aktuellen Tagesbericht als PDF – praktisch für die Ablage oder den Steuerberater.",
                            "left"),

                    // 16 – Tagesabschluss Tab
                    new TourStep("[tour-id='berichte-tab-tages']",
                            "Tagesabschluss",
                            "Gesamtumsatz, Transaktionen und der Ø Bon-Wert für den gewählten Tag. Datum oben links wechseln um andere Tage anzuschauen.",
                            "bottom"),

                    // 17 – Inhalt
                    new TourStep("[tour-id='berichte-inhalt']",
                            "Zahlungsarten & Top-Seller",
                            "Darunter siehst du die Aufteilung zwischen Bar- und Kartenzahlungen sowie den Top-Seller des Tages.",
                            "top"),

                    // 18 – Umsatzübersicht Tab
                    new TourStep("[tour-id='berichte-tab-umsatz']",
                            "Umsatzübersicht",
                            "Der Umsatzverlauf nach Stunde – so siehst du wann der Laden am meisten läuft. Plus eine Liste aller verkauften Produkte.",
                            "bottom", "navigate-tab-umsatz"),

                    // 19 – Artikelstatistik Tab
                    new TourStep("[tour-id='berichte-tab-artikel']",
                            "Artikelstatistik",
                            "Das Verkaufsranking der letzten 30 Tage – auf einen Blick welche Artikel gut laufen und welche nicht.",
                            "bottom", "navigate-tab-artikel"),

                    // ── BENUTZER ───────────────────────────────────────────────────

                    // 20 – Benutzer Navigation
                    new TourStep("[tour-id='benutzer-nav']",
                            "Benutzerverwaltung",
                            "Hier verwaltest du alle Mitarbeiter-Accounts. Wir schauen kurz rein.",
                            "right", "navigate:benutzer"),

                    // 21 – Neuer Benutzer Button
                    new TourStep("[tour-id='neuer-benutzer-btn']",
                            "Neuen Mitarbeiter anlegen",
                            "Benutzername, Name, Rolle (Kassierer oder Manager) und Startpasswort vergeben – fertig.",
                            "left"),

                    // 22 – Neuer Benutzer Dialog
                    new TourStep(null,
                            "Neuen Mitarbeiter anlegen",
                            "Einfach ausfüllen und speichern – der Mitarbeiter kann sich sofort einloggen. Dies ist nur eine Demo.",
                            "dialog-left", "open-neuer-benutzer-dialog"),

                    // 23 – Benutzer Tabelle
                    new TourStep("[tour-id='benutzer-tabelle']",
                            "Alle Mitarbeiter",
                            "Hier siehst du alle Accounts mit Name, Rolle und ob sie gerade aktiv sind.",
                            "top"),

                    // 24 – Bearbeiten Button
                    new TourStep("[tour-id='benutzer-bearbeiten-btn']",
                            "Daten ändern",
                            "Mit dem Stift-Button kannst du Name und Rolle eines Mitarbeiters anpassen.",
                            "left"),

                    // 25 – Passwort Button
                    new TourStep("[tour-id='benutzer-passwort-btn']",
                            "Passwort zurücksetzen",
                            "Passwort vergessen? Mit diesem Button setzt du es schnell zurück und vergibst ein neues.",
                            "left"),

                    // 26 – Sperren Button
                    new TourStep("[tour-id='benutzer-sperren-btn']",
                            "Mitarbeiter sperren",
                            "Urlaub, Kündigung, oder einfach temporär deaktivieren? Hier sperrst du den Account – ein weiterer Klick reaktiviert ihn wieder.",
                            "left"),

                    // ── ABSCHLUSS ──────────────────────────────────────────────────

                    // 27 – User Card
                    new TourStep("[tour-id='user-card']",
                            "Das war's! 🎉",
                            "Du kennst jetzt alle Bereiche. Links unten findest du deinen Account – dort kannst du dich auch ausloggen.\n\nViel Erfolg!",
                            "right")
            )
    );

    public List<TourStep> getSteps(String tourId) {
        return TOURS.getOrDefault(tourId, List.of());
    }
}