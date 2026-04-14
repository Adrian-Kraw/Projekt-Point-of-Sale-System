package de.fhswf.kassensystem.views.berichte;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhswf.kassensystem.model.dto.ArtikelStatistikDTO;
import de.fhswf.kassensystem.model.dto.TagesabschlussDTO;
import de.fhswf.kassensystem.service.BerichteService;
import de.fhswf.kassensystem.service.EinstellungService;
import de.fhswf.kassensystem.service.PdfExportService;
import de.fhswf.kassensystem.views.MainLayout;
import de.fhswf.kassensystem.views.SecuredView;
import de.fhswf.kassensystem.model.enums.Rolle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Berichte-View.
 * FIX: Bon-Zielwert wird jetzt in der DB gespeichert (EinstellungService)
 *      und überlebt Seitenreloads und Neustarts.
 */
@Route(value = "berichte", layout = MainLayout.class)
public class BerichteView extends SecuredView {

    private final BerichteService    berichteService;
    private final PdfExportService   pdfExportService;
    private final EinstellungService einstellungService;

    private final Div       tabInhalt  = new Div();
    private       LocalDate aktivDatum = LocalDate.now();
    private       String    aktiverTab = "tagesabschluss";

    private Span tagesTab;
    private Span umsatzTab;
    private Span artikelTab;

    public BerichteView(BerichteService berichteService,
                        PdfExportService pdfExportService,
                        EinstellungService einstellungService) {
        super(Rolle.KASSIERER);
        this.berichteService    = berichteService;
        this.pdfExportService   = pdfExportService;
        this.einstellungService = einstellungService;

        applyStandardBackground();
        tabInhalt.setWidthFull();
        tabInhalt.getElement().setAttribute("tour-id", "berichte-inhalt");
        add(buildHeader(), buildTabNavigation(), buildDatumZeile(), tabInhalt);
        ladeTabInhalt();
    }

    /** Tour-Aktionen für den TourManager. */
    public void tourAktion(String action) {
        switch (action) {
            case "navigate-tab-umsatz"  -> wechsleTab("umsatz",  umsatzTab,  new Span[]{tagesTab, artikelTab});
            case "navigate-tab-artikel" -> wechsleTab("artikel", artikelTab, new Span[]{tagesTab, umsatzTab});
            default -> {}
        }
    }

    private void ladeTabInhalt() {
        tabInhalt.removeAll();
        switch (aktiverTab) {
            case "tagesabschluss" -> {
                // Zielwert frisch aus DB laden
                BigDecimal bonZielwert = einstellungService.getBonZielwert();
                TagesabschlussDTO dto  = berichteService.getTagesabschluss(aktivDatum);
                tabInhalt.add(new TagesabschlussPanel(dto, bonZielwert, istManager(),
                        wert -> {
                            // Zielwert in DB speichern, dann Tab neu laden
                            einstellungService.setBonZielwert(wert);
                            ladeTabInhalt();
                        }));
            }
            case "umsatz"  -> tabInhalt.add(new UmsatzuebersichtPanel(berichteService, aktivDatum));
            case "artikel" -> {
                List<ArtikelStatistikDTO> statistiken = berichteService.getArtikelStatistik(30);
                tabInhalt.add(new ArtikelstatistikPanel(statistiken));
            }
        }
    }

    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setPadding(false);
        header.getStyle().set("margin-bottom", "2rem");

        HorizontalLayout titelGruppe = new HorizontalLayout();
        titelGruppe.setAlignItems(FlexComponent.Alignment.CENTER);
        titelGruppe.setSpacing(false);
        titelGruppe.getStyle().set("gap", "1rem");

        Div iconBox = new Div();
        iconBox.getStyle().set("background", "#e2e0fc").set("border-radius", "1rem").set("padding", "0.75rem")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");
        Span icon = createIcon("bar_chart");
        icon.getStyle().set("color", "#553722").set("font-size", "1.75rem");
        iconBox.add(icon);

        H2 titel = new H2("Berichte & Auswertungen");
        titel.getStyle().set("margin", "0").set("font-size", "1.75rem").set("font-weight", "800")
                .set("color", "#1a1a2e").set("letter-spacing", "-0.025em")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");
        titelGruppe.add(iconBox, titel);

        Button exportBtn = new Button();
        exportBtn.getElement().setAttribute("tour-id", "pdf-export-btn");
        Span dlIcon = createIcon("download");
        Span dlText = new Span("Als PDF exportieren");
        dlText.getStyle().set("font-weight", "600").set("font-size", "0.8rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("letter-spacing", "0.05em").set("text-transform", "uppercase");
        exportBtn.getElement().appendChild(dlIcon.getElement());
        exportBtn.getElement().appendChild(dlText.getElement());
        exportBtn.getStyle()
                .set("background", "transparent").set("border", "2px solid rgba(85,55,34,0.2)")
                .set("border-radius", "9999px").set("padding", "0.625rem 1.5rem")
                .set("color", "#553722").set("cursor", "pointer")
                .set("display", "flex").set("align-items", "center").set("gap", "0.5rem");
        exportBtn.addClickListener(e -> exportiereAlsPdf());

        header.add(titelGruppe, exportBtn);
        return header;
    }

    private HorizontalLayout buildTabNavigation() {
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.setSpacing(false);
        tabs.getStyle().set("gap", "3rem").set("border-bottom", "1px solid rgba(130,116,109,0.15)")
                .set("margin-bottom", "2rem");

        tagesTab   = buildTab("Tagesabschluss",   true);
        umsatzTab  = buildTab("Umsatzübersicht",  false);
        artikelTab = buildTab("Artikelstatistik", false);

        tagesTab.getElement().setAttribute("tour-id", "berichte-tab-tages");
        umsatzTab.getElement().setAttribute("tour-id", "berichte-tab-umsatz");
        artikelTab.getElement().setAttribute("tour-id", "berichte-tab-artikel");

        tagesTab.addClickListener(e   -> wechsleTab("tagesabschluss", tagesTab,   new Span[]{umsatzTab, artikelTab}));
        umsatzTab.addClickListener(e  -> wechsleTab("umsatz",         umsatzTab,  new Span[]{tagesTab, artikelTab}));
        artikelTab.addClickListener(e -> wechsleTab("artikel",        artikelTab, new Span[]{tagesTab, umsatzTab}));

        tabs.add(tagesTab, umsatzTab, artikelTab);
        return tabs;
    }

    private void wechsleTab(String tab, Span aktiv, Span[] inaktive) {
        aktiverTab = tab;
        aktiv.getStyle().set("color", "#553722").set("border-bottom", "3px solid #6f4e37");
        for (Span s : inaktive)
            s.getStyle().set("color", "rgba(85,55,34,0.4)").set("border-bottom", "3px solid transparent");
        ladeTabInhalt();
    }

    private Span buildTab(String label, boolean aktiv) {
        Span tab = new Span(label);
        tab.getStyle()
                .set("font-size", "0.8rem").set("font-weight", "600").set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em").set("padding-bottom", "1rem").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("color", aktiv ? "#553722" : "rgba(85,55,34,0.4)")
                .set("border-bottom", aktiv ? "3px solid #6f4e37" : "3px solid transparent")
                .set("transition", "all 0.2s");
        return tab;
    }

    private HorizontalLayout buildDatumZeile() {
        HorizontalLayout zeile = new HorizontalLayout();
        zeile.setWidthFull();
        zeile.setAlignItems(FlexComponent.Alignment.CENTER);
        zeile.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        zeile.setPadding(false);
        zeile.getStyle().set("margin-bottom", "2rem");

        DatePicker datePicker = new DatePicker(aktivDatum);
        datePicker.setLocale(java.util.Locale.GERMAN);
        datePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) { aktivDatum = e.getValue(); ladeTabInhalt(); }
        });

        Span echtzeit = new Span("Daten werden in Echtzeit aktualisiert");
        echtzeit.getStyle().set("font-size", "0.8rem").set("font-style", "italic")
                .set("color", "#82746d").set("font-family", "'Plus Jakarta Sans', sans-serif");

        zeile.add(datePicker, echtzeit);
        return zeile;
    }

    private void exportiereAlsPdf() {
        try {
            TagesabschlussDTO tagesabschluss = berichteService.getTagesabschluss(aktivDatum);
            List<ArtikelStatistikDTO> statistik = berichteService.getArtikelStatistik(30);
            byte[] pdfBytes = pdfExportService.exportiereTagebericht(tagesabschluss, statistik);
            String dateiname = "Tagesbericht_" +
                    aktivDatum.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String base64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            UI.getCurrent().getPage().executeJs(
                    "const bytes=atob($0);const arr=new Uint8Array(bytes.length);" +
                            "for(let i=0;i<bytes.length;i++)arr[i]=bytes.charCodeAt(i);" +
                            "const blob=new Blob([arr],{type:'application/pdf'});" +
                            "const url=URL.createObjectURL(blob);const a=document.createElement('a');" +
                            "a.href=url;a.download=$1;document.body.appendChild(a);a.click();" +
                            "document.body.removeChild(a);URL.revokeObjectURL(url);",
                    base64, dateiname);
            com.vaadin.flow.component.notification.Notification.show(
                    "PDF wird heruntergeladen...", 3000,
                    com.vaadin.flow.component.notification.Notification.Position.BOTTOM_START);
        } catch (Exception ex) {
            com.vaadin.flow.component.notification.Notification.show(
                    "Fehler beim PDF-Export: " + ex.getMessage(), 4000,
                    com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        }
    }
}