package de.fhswf.kassensystem.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DashedLine;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.dto.ArtikelStatistikDTO;
import de.fhswf.kassensystem.model.dto.TagesabschlussDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Generiert einen PDF-Bericht mit Tagesabschluss und Artikelstatistik.
 * Verwendet iText 7.
 *
 * @author Adrian Krawietz
 */
@Service
public class PdfExportService {

    private static final DeviceRgb BRAUN       = new DeviceRgb(85, 55, 34);
    private static final DeviceRgb BRAUN_HELL  = new DeviceRgb(253, 220, 198);
    private static final DeviceRgb GRAU_HELL   = new DeviceRgb(245, 242, 255);
    private static final DeviceRgb WEISS       = new DeviceRgb(255, 255, 255);

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Erstellt einen vollständigen Tagesbericht als PDF-Bytes.
     *
     * @param tagesabschluss Tagesabschluss-Daten
     * @param artikelStatistik Artikelstatistik-Daten
     * @return PDF als Byte-Array
     */
    public byte[] exportiereTagebericht(TagesabschlussDTO tagesabschluss,
                                        List<ArtikelStatistikDTO> artikelStatistik) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf  = new PdfDocument(writer);
            Document doc     = new Document(pdf);

            doc.setMargins(40, 50, 40, 50);

            PdfFont fontBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal  = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            String datum = tagesabschluss.getDatum().format(DATUM_FORMAT);

            // ── Header ──────────────────────────────────────────────────────────
            Table header = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .useAllAvailableWidth();

            Cell titelZelle = new Cell()
                    .add(new Paragraph("Canapé Café")
                            .setFont(fontBold).setFontSize(20).setFontColor(BRAUN))
                    .add(new Paragraph("Tagesbericht – " + datum)
                            .setFont(fontNormal).setFontSize(11).setFontColor(BRAUN))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            Cell datumZelle = new Cell()
                    .add(new Paragraph("Erstellt am\n" +
                            LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " Uhr")
                            .setFont(fontNormal).setFontSize(9)
                            .setFontColor(ColorConstants.GRAY)
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            header.addCell(titelZelle);
            header.addCell(datumZelle);
            doc.add(header);

            // Trennlinie
            doc.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(8).setMarginBottom(16)
                    .setStrokeColor(BRAUN));

            // ── Abschnitt: Tagesabschluss ────────────────────────────────────────
            doc.add(abschnittTitel("Tagesabschluss", fontBold));

            // Kennzahlen-Tabelle
            Table kennzahlen = new Table(UnitValue.createPercentArray(new float[]{33, 33, 34}))
                    .useAllAvailableWidth()
                    .setMarginBottom(16);

            kennzahlen.addCell(kennzahlZelle("Gesamtumsatz",
                    formatBetrag(tagesabschluss.getGesamtumsatz()), fontBold, fontNormal));
            kennzahlen.addCell(kennzahlZelle("Transaktionen",
                    String.valueOf(tagesabschluss.getAnzahlTransaktionen()), fontBold, fontNormal));

            BigDecimal bonWert = tagesabschluss.getAnzahlTransaktionen() > 0
                    ? tagesabschluss.getGesamtumsatz().divide(
                    BigDecimal.valueOf(tagesabschluss.getAnzahlTransaktionen()),
                    2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            kennzahlen.addCell(kennzahlZelle("Ø Bon-Wert",
                    formatBetrag(bonWert), fontBold, fontNormal));

            doc.add(kennzahlen);

            // Zahlungsarten-Tabelle
            doc.add(new Paragraph("Zahlungsarten")
                    .setFont(fontBold).setFontSize(11).setFontColor(BRAUN)
                    .setMarginBottom(6));

            Table zahlungen = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            // Header
            zahlungen.addHeaderCell(tabellenHeader("Zahlungsart", fontBold));
            zahlungen.addHeaderCell(tabellenHeader("Betrag", fontBold));
            zahlungen.addHeaderCell(tabellenHeader("Anteil", fontBold));

            BigDecimal gesamt = tagesabschluss.getGesamtumsatz();
            if (gesamt == null) gesamt = BigDecimal.ZERO;

            BigDecimal bar   = tagesabschluss.getUmsatzBar()   != null ? tagesabschluss.getUmsatzBar()   : BigDecimal.ZERO;
            BigDecimal karte = tagesabschluss.getUmsatzKarte() != null ? tagesabschluss.getUmsatzKarte() : BigDecimal.ZERO;

            String anteilBar   = gesamt.compareTo(BigDecimal.ZERO) > 0
                    ? bar.multiply(BigDecimal.valueOf(100)).divide(gesamt, 1, RoundingMode.HALF_UP) + " %"
                    : "0 %";
            String anteilKarte = gesamt.compareTo(BigDecimal.ZERO) > 0
                    ? karte.multiply(BigDecimal.valueOf(100)).divide(gesamt, 1, RoundingMode.HALF_UP) + " %"
                    : "0 %";

            zahlungen.addCell(tabellenZelle("Barzahlung",    fontNormal, false));
            zahlungen.addCell(tabellenZelle(formatBetrag(bar),   fontNormal, false));
            zahlungen.addCell(tabellenZelle(anteilBar,       fontNormal, false));

            zahlungen.addCell(tabellenZelle("Kartenzahlung", fontNormal, true));
            zahlungen.addCell(tabellenZelle(formatBetrag(karte),  fontNormal, true));
            zahlungen.addCell(tabellenZelle(anteilKarte,     fontNormal, true));

            doc.add(zahlungen);

            // ── Abschnitt: Artikelstatistik ──────────────────────────────────────
            doc.add(abschnittTitel("Artikelstatistik – Verkaufsranking (letzte 30 Tage)", fontBold));

            if (artikelStatistik == null || artikelStatistik.isEmpty()) {
                doc.add(new Paragraph("Keine Verkaufsdaten vorhanden.")
                        .setFont(fontNormal).setFontSize(10)
                        .setFontColor(ColorConstants.GRAY));
            } else {
                Table ranking = new Table(UnitValue.createPercentArray(new float[]{8, 42, 20, 30}))
                        .useAllAvailableWidth();

                ranking.addHeaderCell(tabellenHeader("#",          fontBold));
                ranking.addHeaderCell(tabellenHeader("Artikel",    fontBold));
                ranking.addHeaderCell(tabellenHeader("Verkauft",   fontBold));
                ranking.addHeaderCell(tabellenHeader("Umsatz",     fontBold));

                int rang = 1;
                for (ArtikelStatistikDTO dto : artikelStatistik) {
                    boolean zebra = rang % 2 == 0;
                    ranking.addCell(tabellenZelle(String.valueOf(rang), fontNormal, zebra));
                    ranking.addCell(tabellenZelle(dto.getArtikel().getName(), fontNormal, zebra));
                    ranking.addCell(tabellenZelle(dto.getAnzahlVerkauft() + " Stk.", fontNormal, zebra));
                    ranking.addCell(tabellenZelle(formatBetrag(dto.getGesamtumsatz()), fontNormal, zebra));
                    rang++;
                }
                doc.add(ranking);
            }

            // ── Footer ───────────────────────────────────────────────────────────
            doc.add(new LineSeparator(new SolidLine(0.5f))
                    .setMarginTop(20).setMarginBottom(8)
                    .setStrokeColor(ColorConstants.GRAY));

            doc.add(new Paragraph("Canapé Café · Automatisch generierter Bericht · " + datum)
                    .setFont(fontNormal).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new KassensystemException("PDF-Export fehlgeschlagen. Bitte versuche es erneut.");
        }
    }


    /**
     * Generiert einen Kassenbon als PDF-Bytes.
     * Enthält alle Verkaufspositionen mit Menge und Einzelpreis,
     * optionalen Rabatt, MwSt-Aufschlüsselung (7% / 19%) und Gesamtsumme.
     *
     * @param positionen    Verkaufspositionen aus dem abgeschlossenen Warenkorb
     * @param rabattProzent Rabatt in Prozent, oder {@code null} / 0 für keinen Rabatt
     * @return PDF als Byte-Array, bereit zum Download
     */
    public byte[] exportiereKassenbon(
            List<Verkaufsposition> positionen,
            BigDecimal rabattProzent) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf  = new PdfDocument(writer);
            Document doc     = new Document(pdf);
            doc.setMargins(30, 40, 30, 40);

            PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Header
            doc.add(new Paragraph("Canapé Café")
                    .setFont(fontBold).setFontSize(16).setFontColor(BRAUN)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " Uhr")
                    .setFont(fontNormal).setFontSize(9).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(8));

            doc.add(new LineSeparator(new DashedLine())
                    .setMarginBottom(8));

            // Positionen
            BigDecimal zwischensumme = BigDecimal.ZERO;
            BigDecimal mwst7  = BigDecimal.ZERO;
            BigDecimal mwst19 = BigDecimal.ZERO;

            for (Verkaufsposition pos : positionen) {
                BigDecimal posGesamt = pos.getEinzelpreis()
                        .multiply(BigDecimal.valueOf(pos.getMenge()));
                zwischensumme = zwischensumme.add(posGesamt);

                // MwSt aufschlüsseln
                if (pos.getArtikel().getMehrwertsteuer() != null) {
                    BigDecimal satz = pos.getArtikel().getMehrwertsteuer().getSatz();
                    BigDecimal nettoAnteil = posGesamt.divide(
                            BigDecimal.ONE.add(satz.divide(BigDecimal.valueOf(100))),
                            2, RoundingMode.HALF_UP);
                    BigDecimal steuerBetrag = posGesamt.subtract(nettoAnteil);
                    if (satz.compareTo(BigDecimal.valueOf(7)) == 0) {
                        mwst7 = mwst7.add(steuerBetrag);
                    } else {
                        mwst19 = mwst19.add(steuerBetrag);
                    }
                }

                Table posTable = new Table(UnitValue.createPercentArray(new float[]{60, 15, 25}))
                        .useAllAvailableWidth().setMarginBottom(2);
                posTable.addCell(new Cell().add(new Paragraph(pos.getArtikel().getName())
                                .setFont(fontNormal).setFontSize(9))
                        .setBorder(Border.NO_BORDER).setPadding(1));
                posTable.addCell(new Cell().add(new Paragraph(pos.getMenge() + "x")
                                .setFont(fontNormal).setFontSize(9).setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER).setPadding(1));
                posTable.addCell(new Cell().add(new Paragraph(formatBetrag(posGesamt))
                                .setFont(fontBold).setFontSize(9).setTextAlignment(TextAlignment.RIGHT))
                        .setBorder(Border.NO_BORDER).setPadding(1));
                doc.add(posTable);
            }

            doc.add(new LineSeparator(new DashedLine())
                    .setMarginTop(6).setMarginBottom(6));

            // Zwischensumme & Rabatt
            BigDecimal rabattBetrag = BigDecimal.ZERO;
            if (rabattProzent != null && rabattProzent.compareTo(BigDecimal.ZERO) > 0) {
                rabattBetrag = zwischensumme.multiply(rabattProzent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                addBonZeile(doc, "Zwischensumme", formatBetrag(zwischensumme), fontNormal, false);
                addBonZeile(doc, "Rabatt (" + rabattProzent.toPlainString() + "%)",
                        "- " + formatBetrag(rabattBetrag), fontNormal, false);
            }

            BigDecimal gesamtsumme = zwischensumme.subtract(rabattBetrag);

            // MwSt
            if (mwst7.compareTo(BigDecimal.ZERO) > 0) {
                addBonZeile(doc, "MwSt 7%", formatBetrag(mwst7), fontNormal, false);
            }
            if (mwst19.compareTo(BigDecimal.ZERO) > 0) {
                addBonZeile(doc, "MwSt 19%", formatBetrag(mwst19), fontNormal, false);
            }

            doc.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(4).setMarginBottom(4).setStrokeColor(BRAUN));

            // Gesamt
            addBonZeile(doc, "GESAMT", formatBetrag(gesamtsumme), fontBold, true);

            doc.add(new LineSeparator(new DashedLine())
                    .setMarginTop(8).setMarginBottom(8));

            doc.add(new Paragraph("Vielen Dank für Ihren Besuch!")
                    .setFont(fontNormal).setFontSize(9).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new KassensystemException("Kassenbon-Export fehlgeschlagen. Bitte versuche es erneut.");
        }
    }

    /**
     * Erstellt eine farbig hinterlegte Abschnittsüberschrift.
     *
     * @param text Überschriftentext
     * @param font fetter Font
     */
    private Paragraph abschnittTitel(String text, PdfFont font) {
        return new Paragraph(text)
                .setFont(font).setFontSize(13).setFontColor(BRAUN)
                .setBackgroundColor(GRAU_HELL)
                .setPadding(8).setMarginBottom(10);
    }

    /**
     * Erstellt eine Kennzahlen-Kachel mit großem Zahlenwert oben und kleinem Label unten.
     *
     * @param label      Beschriftung (z.B. "Gesamtumsatz")
     * @param wert       anzuzeigender Wert (z.B. "142,50 €")
     * @param fontBold   fetter Font für den Wert
     * @param fontNormal normaler Font für das Label
     */
    private Cell kennzahlZelle(String label, String wert, PdfFont fontBold, PdfFont fontNormal) {
        return new Cell()
                .add(new Paragraph(wert).setFont(fontBold).setFontSize(18).setFontColor(BRAUN))
                .add(new Paragraph(label).setFont(fontNormal).setFontSize(9).setFontColor(ColorConstants.GRAY))
                .setBackgroundColor(GRAU_HELL)
                .setBorder(Border.NO_BORDER)
                .setPadding(12).setMargin(3);
    }

    /**
     * Erstellt eine Tabellen-Kopfzelle mit braunem Hintergrund und weißem Text.
     *
     * @param text Spaltenbezeichnung
     * @param font fetter Font
     */
    private Cell tabellenHeader(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(WEISS))
                .setBackgroundColor(BRAUN)
                .setBorderBottom(new SolidBorder(BRAUN, 1))
                .setPadding(8);
    }

    /**
     * Erstellt eine Tabellen-Datenzelle mit optionalem Zebra-Hintergrund.
     *
     * @param text  Zelleninhalt
     * @param font  normaler Font
     * @param zebra {@code true} für hellgrauen Hintergrund (Zebra-Streifen)
     */
    private Cell tabellenZelle(String text, PdfFont font, boolean zebra) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setBackgroundColor(zebra ? GRAU_HELL : WEISS)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(
                        new DeviceRgb(230, 230, 230), 0.5f))
                .setPadding(7);
    }

    /**
     * Fügt dem Kassenbon eine zweispaltige Zeile (Label links, Wert rechts) hinzu.
     *
     * @param doc   das Dokument dem die Zeile hinzugefügt wird
     * @param label Beschriftung (z.B. "MwSt 7%")
     * @param wert  formatierter Betrag (z.B. "0,49 €")
     * @param font  zu verwendender Font
     * @param gross {@code true} für große, braun-farbige Darstellung (GESAMT-Zeile)
     */
    private void addBonZeile(Document doc, String label, String wert,
                             PdfFont font, boolean gross) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth().setMarginBottom(2);
        t.addCell(new Cell().add(new Paragraph(label).setFont(font)
                        .setFontSize(gross ? 11 : 9).setFontColor(gross ? BRAUN : ColorConstants.BLACK))
                .setBorder(Border.NO_BORDER).setPadding(1));
        t.addCell(new Cell().add(new Paragraph(wert).setFont(font)
                        .setFontSize(gross ? 11 : 9).setFontColor(gross ? BRAUN : ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(1));
        doc.add(t);
    }

    /**
     * Formatiert einen Betrag als deutschen Währungsstring (z.B. {@code "1.234,56 €"}).
     *
     * @param betrag der zu formatierende Betrag, oder {@code null}
     * @return formatierter String, {@code "0,00 €"} bei {@code null}
     */
    private String formatBetrag(BigDecimal betrag) {
        if (betrag == null) return "0,00 €";
        return String.format(Locale.GERMANY, "%,.2f €", betrag);
    }

}