package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhswf.kassensystem.model.Artikel;
import de.fhswf.kassensystem.model.Verkauf;
import de.fhswf.kassensystem.model.Verkaufsposition;
import de.fhswf.kassensystem.model.dto.TagesabschlussDTO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel für die Top-3-Seller des aktuellen Tages im Tagesabschluss.
 *
 * <p>Aggregiert alle Verkaufspositionen des Tages nach Artikel,
 * sortiert absteigend nach Menge und zeigt die drei meistverkauften Artikel.
 *
 * @author Adrian
 */
class TopSellerPanel extends VerticalLayout {

    /**
     * Erstellt das Panel und berechnet die Top-Seller aus den Tagesverkäufen.
     *
     * @param dto Tagesabschlussdaten mit allen Verkäufen und Positionen
     */
    TopSellerPanel(TagesabschlussDTO dto) {
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("flex", "0 0 320px").set("max-width", "320px").set("background", "white")
                .set("border-radius", "1.25rem").set("border", "2px solid #553722")
                .set("padding", "2rem").set("gap", "1.5rem");

        add(buildKopf(), buildListe(dto));
    }

    /**
     * Erstellt den Kartenkopf mit Titel, Untertitel und Stern-Icon-Box.
     */
    private HorizontalLayout buildKopf() {
        H3 titel = new H3("Top Seller Heute");
        titel.getStyle().set("margin", "0").set("font-size", "1.1rem").set("font-weight", "900")
                .set("color", "#553722").set("font-family", "'Plus Jakarta Sans', sans-serif");
        Paragraph sub = new Paragraph("Basierend auf der Anzahl der verkauften Artikel");
        sub.getStyle().set("margin", "0").set("font-size", "0.7rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        Div sternBox = new Div();
        sternBox.getStyle().set("width", "3rem").set("height", "3rem").set("border-radius", "9999px")
                .set("background", "#f5f2ff").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center");
        Span stern = new Span("star");
        stern.addClassName("material-symbols-outlined");
        stern.getStyle().set("line-height", "1");
        sternBox.add(stern);

        VerticalLayout titelBlock = new VerticalLayout();
        titelBlock.setPadding(false);
        titelBlock.setSpacing(false);
        titelBlock.add(titel, sub);

        HorizontalLayout kopf = new HorizontalLayout();
        kopf.setWidthFull();
        kopf.setAlignItems(FlexComponent.Alignment.START);
        kopf.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        kopf.setPadding(false);
        kopf.add(titelBlock, sternBox);
        return kopf;
    }

    /**
     * Aggregiert die Verkaufsmengen je Artikel und rendert die Top-3-Liste.
     *
     * @param dto Tagesabschlussdaten
     * @return Layout mit bis zu drei Einträgen
     */
    private VerticalLayout buildListe(TagesabschlussDTO dto) {
        VerticalLayout liste = new VerticalLayout();
        liste.setWidthFull();
        liste.setPadding(false);
        liste.setSpacing(false);
        liste.getStyle().set("gap", "0.75rem");

        Map<Artikel, Integer> mengen = new LinkedHashMap<>();
        List<Verkauf> verkaeufe = dto.getVerkaeufe() != null ? dto.getVerkaeufe() : List.of();
        for (Verkauf v : verkaeufe) {
            if (v.getPositionen() == null) continue;
            for (Verkaufsposition pos : v.getPositionen())
                mengen.merge(pos.getArtikel(), pos.getMenge(), Integer::sum);
        }

        mengen.entrySet().stream()
                .sorted(Map.Entry.<Artikel, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> liste.add(buildEintrag(
                        e.getKey().getName(), e.getKey().getKategorie().getName(), e.getValue() + "x")));

        if (mengen.isEmpty()) liste.add(BerichteUtils.leerSpan("Keine Verkäufe an diesem Tag."));
        return liste;
    }

    /**
     * Erstellt einen einzelnen Top-Seller-Eintrag mit Avatar, Name/Kategorie und Anzahl.
     *
     * @param name      Artikelname
     * @param kategorie Kategoriename
     * @param anzahl    verkaufte Menge als Text (z.B. "8x")
     */
    private HorizontalLayout buildEintrag(String name, String kategorie, String anzahl) {
        Div avatar = new Div();
        avatar.getStyle().set("width", "2.5rem").set("height", "2.5rem")
                .set("border-radius", "9999px").set("background", "#efecff").set("flex-shrink", "0");

        Span n = new Span(name);
        n.getStyle().set("font-weight", "700").set("color", "#553722").set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        Span k = new Span(kategorie);
        k.getStyle().set("font-size", "0.7rem").set("color", "#82746d")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.add(n, k);

        HorizontalLayout links = new HorizontalLayout();
        links.setAlignItems(FlexComponent.Alignment.CENTER);
        links.setSpacing(false);
        links.getStyle().set("gap", "1rem");
        links.add(avatar, info);

        Span a = new Span(anzahl);
        a.getStyle().set("font-weight", "900").set("color", "#553722").set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        HorizontalLayout eintrag = new HorizontalLayout();
        eintrag.setWidthFull();
        eintrag.setAlignItems(FlexComponent.Alignment.CENTER);
        eintrag.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        eintrag.setPadding(false);
        eintrag.getStyle().set("padding", "0.25rem 0");
        eintrag.add(links, a);
        return eintrag;
    }
}
