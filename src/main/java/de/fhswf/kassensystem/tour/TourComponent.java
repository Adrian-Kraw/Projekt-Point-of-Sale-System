package de.fhswf.kassensystem.tour;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

import java.util.List;
import java.util.function.Consumer;

/**
 * Tour-Komponente – vollständig in Vaadin, kein Dialog.
 * Tooltip = Vaadin Div mit position:fixed direkt an UI attached.
 * Buttons = echte Vaadin Buttons mit direkten Server-Listenern.
 * Spotlight/Overlay = JS (kein anderer Weg).
 */
public class TourComponent extends Div {

    public static class TourFinishedEvent extends ComponentEvent<TourComponent> {
        public TourFinishedEvent(TourComponent source) { super(source, false); }
    }

    private final List<TourStep>   steps;
    private final Consumer<String> actionHandler;
    private int      currentIndex    = 0;
    private Runnable pendingCallback = null;

    // ── Tooltip-Komponenten (echtes Vaadin) ───────────────────────────────────
    private final Div       tooltipDiv = new Div();
    private final H3        titleEl    = new H3();
    private final Paragraph descEl     = new Paragraph();
    private final Div       dotsEl     = new Div();
    private final Button    backBtn    = new Button("Zurück");
    private final Button    nextBtn    = new Button("Weiter");

    // Unsichtbarer Action-Button für server-seitige Aktionen
    private final Button actionBtn = new Button();

    public TourComponent(List<TourStep> steps, Consumer<String> actionHandler) {
        this.steps         = steps;
        this.actionHandler = actionHandler;

        // TourComponent-Wrapper unsichtbar
        getStyle().set("position", "fixed")
                .set("width", "0").set("height", "0")
                .set("overflow", "hidden").set("opacity", "0")
                .set("pointer-events", "none");

        buildTooltip();
        buildActionBtn();
    }

    // ── Tooltip aufbauen ──────────────────────────────────────────────────────

    private void buildTooltip() {
        // Tooltip-Container: fixed, höchster z-index, eigenes Styling
        tooltipDiv.getStyle()
                .set("position", "fixed")
                .set("z-index", "2147483647")
                .set("background", "#ffffff")
                .set("border-radius", "16px")
                .set("padding", "20px 22px 16px")
                .set("box-shadow", "0 12px 40px rgba(0,0,0,0.22)")
                .set("min-width", "300px")
                .set("max-width", "400px")
                .set("box-sizing", "border-box")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("top", "50%")
                .set("left", "50%")
                .set("transform", "translate(-50%, -50%)");

        // Titel
        titleEl.getStyle()
                .set("margin", "0 0 8px 0")
                .set("font-size", "1rem")
                .set("font-weight", "700")
                .set("color", "#1a1a1a")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // Beschreibung
        descEl.getStyle()
                .set("margin", "0 0 14px 0")
                .set("font-size", "0.875rem")
                .set("color", "#555")
                .set("line-height", "1.65")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        // Dots
        dotsEl.getStyle()
                .set("display", "flex").set("gap", "5px")
                .set("align-items", "center").set("flex", "1");

        // Zurück-Button
        backBtn.getStyle()
                .set("background", "none")
                .set("border", "1.5px solid #ccc")
                .set("border-radius", "8px")
                .set("padding", "6px 14px")
                .set("font-size", "0.875rem")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("color", "#553722")
                .set("cursor", "pointer")
                .set("white-space", "nowrap");
        backBtn.addClickListener(e -> onBack());

        // Weiter-Button
        nextBtn.getStyle()
                .set("background", "#553722")
                .set("color", "#fff")
                .set("border", "none")
                .set("border-radius", "8px")
                .set("padding", "6px 16px")
                .set("font-size", "0.875rem")
                .set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("cursor", "pointer")
                .set("white-space", "nowrap");
        nextBtn.addClickListener(e -> onNext());

        // Nav-Zeile
        HorizontalLayout nav = new HorizontalLayout(dotsEl, backBtn, nextBtn);
        nav.setAlignItems(FlexComponent.Alignment.CENTER);
        nav.setPadding(false);
        nav.setSpacing(false);
        nav.getStyle().set("gap", "8px");

        VerticalLayout content = new VerticalLayout(titleEl, descEl, nav);
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("gap", "0");

        tooltipDiv.add(content);
    }

    private void buildActionBtn() {
        actionBtn.setId("__tour_action_btn");
        actionBtn.getStyle()
                .set("position", "absolute").set("opacity", "0")
                .set("width", "0").set("height", "0");
        actionBtn.addClickListener(e -> {
            TourStep step = steps.get(currentIndex);
            if (step.hasAction() && !step.action().startsWith("navigate:")) {
                actionHandler.accept(step.action());
            }
        });
        add(actionBtn);
    }

    // ── Button-Handler (100% serverseitig) ────────────────────────────────────

    private void onNext() {
        closeOpenDialogs();
        if (currentIndex < steps.size() - 1) {
            currentIndex++;
            executeAndShow(currentIndex);
        } else {
            finish();
        }
    }

    private void onBack() {
        closeOpenDialogs();
        if (currentIndex > 0) {
            currentIndex--;
            executeAndShow(currentIndex);
        }
    }

    // ── Öffentliche API ───────────────────────────────────────────────────────

    public void start() {
        currentIndex    = 0;
        pendingCallback = null;
        injectSpotlight();
        // Tooltip an UI hängen (direkt, nicht als Kind von TourComponent)
        UI.getCurrent().add(tooltipDiv);
        executeAndShow(0);
    }

    public Registration addTourFinishedListener(ComponentEventListener<TourFinishedEvent> listener) {
        return addListener(TourFinishedEvent.class, listener);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void executeAndShow(int index) {
        TourStep step = steps.get(index);

        if (!step.hasAction()) {
            navigateIfNeeded(step, () -> showStep(index));
            return;
        }

        String action = step.action();
        if (action.startsWith("navigate:")) {
            String route = action.substring("navigate:".length());
            if (route.equals("dashboard")) route = "";
            String cur = UI.getCurrent().getActiveViewLocation().getPath();
            if (cur.equals(route)) {
                showStep(index);
            } else {
                hideSpotlight();
                UI.getCurrent().navigate(route);
                scheduleNav(() -> showStep(index));
            }
            return;
        }

        // Demo-Aktionen: Step anzeigen, dann Aktion ausführen
        navigateIfNeeded(step, () -> {
            showStep(index);
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => document.getElementById('__tour_action_btn')?.click(), 400);"
            );
        });
    }

    private void navigateIfNeeded(TourStep step, Runnable after) {
        String route = routeForSelector(step.targetSelector());
        if (route == null) { after.run(); return; }
        String cur = UI.getCurrent().getActiveViewLocation().getPath();
        if (cur.equals(route)) {
            after.run();
        } else {
            hideSpotlight();
            UI.getCurrent().navigate(route);
            scheduleNav(after);
        }
    }

    private void scheduleNav(Runnable cb) {
        pendingCallback = cb;
        Button navBtn = new Button();
        navBtn.setId("__tour_nav_ready_btn");
        navBtn.getStyle().set("position", "absolute").set("opacity", "0")
                .set("width", "0").set("height", "0");
        navBtn.addClickListener(e -> {
            remove(navBtn);
            if (pendingCallback != null) { Runnable r = pendingCallback; pendingCallback = null; r.run(); }
        });
        add(navBtn);
        UI.getCurrent().getPage().executeJs(
                "setTimeout(() => document.getElementById('__tour_nav_ready_btn')?.click(), 650);"
        );
    }

    private String routeForSelector(String sel) {
        if (sel == null) return null;
        if (sel.contains("kategorie-chips") || sel.contains("artikel-suche")
                || sel.contains("artikel-grid")   || sel.contains("warenkorb-spalte")
                || sel.contains("zusammenfassung") || sel.contains("rabatt-zeile")
                || sel.contains("bezahlen-btn"))   return "kassieren";
        if (sel.contains("statistik-karten") || sel.contains("artikel-minimum")
                || sel.contains("gesamtartikel")   || sel.contains("bestand-tabelle")
                || sel.contains("status-spalte"))  return "lager";
        return null;
    }

    // ── Step anzeigen ─────────────────────────────────────────────────────────

    private void showStep(int index) {
        TourStep step  = steps.get(index);
        int      total = steps.size();
        boolean  last  = index == total - 1;
        boolean  first = index == 0;

        // Titel & Beschreibung
        titleEl.setText(step.title());
        descEl.getElement().setProperty("innerHTML",
                step.description().replace("\n", "<br>"));

        // Dots aktualisieren
        dotsEl.removeAll();
        for (int i = 0; i < total; i++) {
            Span dot = new Span();
            dot.getStyle()
                    .set("width",  i == index ? "10px" : "7px")
                    .set("height", i == index ? "10px" : "7px")
                    .set("border-radius", "50%")
                    .set("background", i == index ? "#553722" : "#ddd")
                    .set("display", "inline-block")
                    .set("flex-shrink", "0");
            dotsEl.add(dot);
        }

        nextBtn.setText(last ? "Fertig" : "Weiter");
        backBtn.setVisible(!first);

        // Spotlight + Tooltip-Position via JS
        String  selector    = step.hasTarget() ? step.targetSelector() : "";
        boolean tipCentered = step.tooltipCentered();

        tooltipDiv.getElement().executeJs("""
            (function(el) {
                const selector    = $0;
                const pos         = $1;
                const tipCentered = $2;
                const spotlight   = document.getElementById('__tour_spotlight');
                if (!spotlight) return;

                const hasSelector = selector && selector.trim() !== '';

                if (!hasSelector) {
                    spotlight.style.display = 'none';
                    if (pos === 'dialog-left') {
                        // Nach Dialog-Öffnen positionieren (wird von executeAndShow übernommen)
                        el.style.transform = 'none';
                        el.style.top  = '50%';
                        el.style.left = '16px';
                        // Korrektur nach Dialog-Render
                        setTimeout(() => {
                            const dlg = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))[0];
                            if (dlg) {
                                const dr   = dlg.getBoundingClientRect();
                                const tipH = el.getBoundingClientRect().height;
                                el.style.top  = Math.max(16, dr.top + dr.height/2 - tipH/2) + 'px';
                                el.style.left = Math.max(16, dr.left - 420) + 'px';
                            }
                        }, 500);
                    } else {
                        el.style.transform = 'translate(-50%, -50%)';
                        el.style.top  = '50%';
                        el.style.left = '50%';
                    }
                    return;
                }

                const target = document.querySelector(selector);
                if (!target) {
                    spotlight.style.display = 'none';
                    el.style.transform = 'translate(-50%, -50%)';
                    el.style.top  = '50%';
                    el.style.left = '50%';
                    return;
                }

                const r0 = target.getBoundingClientRect();
                if (r0.top < 0 || r0.bottom > window.innerHeight)
                    target.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

                setTimeout(() => {
                    const rect  = target.getBoundingClientRect();
                    const PAD   = 10;
                    const TIP_W = 400;
                    const MARGIN = 16;
                    const vw = window.innerWidth, vh = window.innerHeight;

                    let spotTop = rect.top, spotHeight = rect.height;
                    if (selector.includes('status-spalte')) {
                        const table = document.querySelector("[tour-id='bestand-tabelle']");
                        if (table) {
                            const tr = table.getBoundingClientRect();
                            spotHeight = tr.bottom - rect.top;
                        }
                    }

                    spotlight.style.display = 'block';
                    spotlight.style.top    = (spotTop    - PAD) + 'px';
                    spotlight.style.left   = (rect.left  - PAD) + 'px';
                    spotlight.style.width  = (rect.width  + PAD*2) + 'px';
                    spotlight.style.height = (spotHeight  + PAD*2) + 'px';

                    el.style.transform = 'none';
                    const tipH = Math.max(el.getBoundingClientRect().height, 160);
                    let top, left;
                    if      (tipCentered)      { el.style.transform='translate(-50%,-50%)'; el.style.top='50%'; el.style.left='50%'; return; }
                    else if (pos==='bottom')   { top=rect.bottom+MARGIN; left=rect.left+rect.width/2-TIP_W/2; }
                    else if (pos==='top')      { top=rect.top-tipH-MARGIN; left=rect.left+rect.width/2-TIP_W/2; }
                    else if (pos==='right')    { top=rect.top+rect.height/2-tipH/2; left=rect.right+MARGIN; }
                    else if (pos==='left')     { top=rect.top+rect.height/2-tipH/2; left=rect.left-TIP_W-MARGIN; }
                    else                       { top=vh/2-tipH/2; left=vw/2-TIP_W/2; }

                    el.style.top  = Math.max(MARGIN, Math.min(top,  vh-tipH-MARGIN)) + 'px';
                    el.style.left = Math.max(MARGIN, Math.min(left, vw-TIP_W-MARGIN)) + 'px';

                    target.classList.add('tour-target-highlight');
                    setTimeout(() => target.classList.remove('tour-target-highlight'), 1400);
                }, 150);
            })(this.$server ? this : this.getRootNode().host || this);
            """,
                selector, step.position(), tipCentered
        );
    }

    // ── Demo-Dialoge schließen ─────────────────────────────────────────────────

    private void closeOpenDialogs() {
        getElement().executeJs("""
            document.querySelectorAll('vaadin-dialog-overlay').forEach(d => {
                d.style.pointerEvents = '';
                if (typeof d.close === 'function') d.close();
                else d.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', bubbles:true}));
            });
        """);
    }

    // ── Spotlight injizieren ───────────────────────────────────────────────────

    private void injectSpotlight() {
        getElement().executeJs("""
            document.getElementById('__tour_spotlight')?.remove();
            document.getElementById('__tour_styles')?.remove();

            const style = document.createElement('style');
            style.id = '__tour_styles';
            style.textContent = `
              #__tour_spotlight {
                position:fixed; display:none;
                background:transparent;
                box-shadow:0 0 0 9999px rgba(0,0,0,0.20);
                border-radius:10px; z-index:200; pointer-events:none;
                transition:top .25s,left .25s,width .25s,height .25s;
              }
              @keyframes __tour_pulse {
                0%   { box-shadow:0 0 0 0   rgba(85,55,34,0.6); }
                70%  { box-shadow:0 0 0 14px rgba(85,55,34,0); }
                100% { box-shadow:0 0 0 0   rgba(85,55,34,0); }
              }
              .tour-target-highlight { animation:__tour_pulse 1.3s ease-out; border-radius:8px; }
            `;
            document.head.appendChild(style);

            const spotlight = document.createElement('div');
            spotlight.id = '__tour_spotlight';
            document.body.appendChild(spotlight);
        """);
    }

    private void hideSpotlight() {
        getElement().executeJs(
                "const s=document.getElementById('__tour_spotlight'); if(s) s.style.display='none';"
        );
    }

    // ── Finish ────────────────────────────────────────────────────────────────

    private void finish() {
        pendingCallback = null;
        // Seite neu laden → sauberer Ursprungszustand, kein Spotlight-Flash
        UI.getCurrent().getPage().reload();
    }
}