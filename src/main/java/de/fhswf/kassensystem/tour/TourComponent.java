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

public class TourComponent extends Div {

    public static class TourFinishedEvent extends ComponentEvent<TourComponent> {
        public TourFinishedEvent(TourComponent source) { super(source, false); }
    }

    private final List<TourStep>   steps;
    private final Consumer<String> actionHandler;
    private int      currentIndex    = 0;
    private Runnable pendingCallback = null;

    private final Div       tooltipDiv = new Div();
    private final H3        titleEl    = new H3();
    private final Paragraph descEl     = new Paragraph();
    private final Div       dotsEl     = new Div();
    private final Button    backBtn    = new Button("Zurück");
    private final Button    nextBtn    = new Button("Weiter");
    private final Button    actionBtn  = new Button();

    public TourComponent(List<TourStep> steps, Consumer<String> actionHandler) {
        this.steps         = steps;
        this.actionHandler = actionHandler;
        getStyle().set("position", "fixed").set("width", "0").set("height", "0")
                .set("overflow", "hidden").set("opacity", "0").set("pointer-events", "none");
        buildTooltip();
        buildActionBtn();
    }

    private void buildTooltip() {
        tooltipDiv.getStyle()
                .set("position", "fixed").set("z-index", "2147483647")
                .set("background", "#ffffff").set("border-radius", "16px")
                .set("padding", "20px 22px 16px")
                .set("box-shadow", "0 12px 40px rgba(0,0,0,0.22)")
                .set("min-width", "300px").set("max-width", "400px")
                .set("box-sizing", "border-box")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("top", "50%").set("left", "50%")
                .set("transform", "translate(-50%, -50%)");

        titleEl.getStyle().set("margin", "0 0 8px 0").set("font-size", "1rem")
                .set("font-weight", "700").set("color", "#1a1a1a")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        descEl.getStyle().set("margin", "0 0 14px 0").set("font-size", "0.875rem")
                .set("color", "#555").set("line-height", "1.65")
                .set("font-family", "'Plus Jakarta Sans', sans-serif");

        dotsEl.getStyle().set("display", "flex").set("gap", "5px")
                .set("align-items", "center").set("flex", "1");

        backBtn.getStyle().set("background", "none").set("border", "1.5px solid #ccc")
                .set("border-radius", "8px").set("padding", "6px 14px")
                .set("font-size", "0.875rem").set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("color", "#553722").set("cursor", "pointer").set("white-space", "nowrap");
        backBtn.addClickListener(e -> onBack());

        nextBtn.getStyle().set("background", "#553722").set("color", "#fff")
                .set("border", "none").set("border-radius", "8px").set("padding", "6px 16px")
                .set("font-size", "0.875rem").set("font-weight", "700")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("cursor", "pointer").set("white-space", "nowrap");
        nextBtn.addClickListener(e -> onNext());

        HorizontalLayout nav = new HorizontalLayout(dotsEl, backBtn, nextBtn);
        nav.setAlignItems(FlexComponent.Alignment.CENTER);
        nav.setPadding(false); nav.setSpacing(false);
        nav.getStyle().set("gap", "8px");

        VerticalLayout content = new VerticalLayout(titleEl, descEl, nav);
        content.setPadding(false); content.setSpacing(false);
        content.getStyle().set("gap", "0");
        tooltipDiv.add(content);
    }

    private void buildActionBtn() {
        actionBtn.setId("__tour_action_btn");
        actionBtn.getStyle().set("position", "absolute").set("opacity", "0")
                .set("width", "0").set("height", "0");
        actionBtn.addClickListener(e -> {
            TourStep step = steps.get(currentIndex);
            if (step.hasAction() && !step.action().startsWith("navigate:"))
                actionHandler.accept(step.action());
        });
        add(actionBtn);
    }

    private void onNext() {
        closeOpenDialogs();
        if (currentIndex < steps.size() - 1) { currentIndex++; executeAndShow(currentIndex); }
        else finish();
    }

    private void onBack() {
        closeOpenDialogs();
        if (currentIndex > 0) { currentIndex--; executeAndShow(currentIndex); }
    }

    public void start() {
        currentIndex    = 0;
        pendingCallback = null;
        injectSpotlight();
        UI.getCurrent().add(tooltipDiv);
        executeAndShow(0);
    }

    public Registration addTourFinishedListener(ComponentEventListener<TourFinishedEvent> listener) {
        return addListener(TourFinishedEvent.class, listener);
    }

    private void executeAndShow(int index) {
        TourStep step = steps.get(index);

        if (!step.hasAction()) { navigateIfNeeded(step, () -> showStep(index)); return; }

        String action = step.action();
        if (action.startsWith("navigate:")) {
            String route = action.substring("navigate:".length());
            if (route.equals("dashboard")) route = "";
            String cur = UI.getCurrent().getActiveViewLocation().getPath();
            if (cur.equals(route)) showStep(index);
            else { hideSpotlight(); UI.getCurrent().navigate(route); scheduleNav(() -> showStep(index)); }
            return;
        }

        navigateIfNeeded(step, () -> {
            showStep(index);
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => document.getElementById('__tour_action_btn')?.click(), 400);");
        });
    }

    private void navigateIfNeeded(TourStep step, Runnable after) {
        String route = routeForSelector(step.targetSelector());
        if (route == null) { after.run(); return; }
        String cur = UI.getCurrent().getActiveViewLocation().getPath();
        if (cur.equals(route)) after.run();
        else { hideSpotlight(); UI.getCurrent().navigate(route); scheduleNav(after); }
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
                "setTimeout(() => document.getElementById('__tour_nav_ready_btn')?.click(), 650);");
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

    private void showStep(int index) {
        TourStep step  = steps.get(index);
        int      total = steps.size();

        titleEl.setText(step.title());
        descEl.getElement().setProperty("innerHTML", step.description().replace("\n", "<br>"));

        dotsEl.removeAll();
        for (int i = 0; i < total; i++) {
            Span dot = new Span();
            dot.getStyle()
                    .set("width",  i == index ? "10px" : "7px")
                    .set("height", i == index ? "10px" : "7px")
                    .set("border-radius", "50%")
                    .set("background", i == index ? "#553722" : "#ddd")
                    .set("display", "inline-block").set("flex-shrink", "0");
            dotsEl.add(dot);
        }

        nextBtn.setText(index == total - 1 ? "Fertig" : "Weiter");
        backBtn.setVisible(index != 0);

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
                        el.style.transform = 'none';
                        el.style.opacity   = '0';
                        setTimeout(() => {
                            const dlg = document.querySelector('vaadin-dialog-overlay');
                            const dr = dlg ? dlg.getBoundingClientRect() : null;
                            el.style.transform = 'none';
                            const tipW = el.getBoundingClientRect().width || 320;
                            el.style.top  = (dr ? Math.max(16, dr.top + 20) : 200) + 'px';
                            el.style.left = (dr ? Math.max(16, dr.left - tipW - 24) : 200) + 'px';
                            el.style.opacity = '1';
                        }, 300);
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
                    el.style.top = '50%'; el.style.left = '50%';
                    return;
                }

                const r0 = target.getBoundingClientRect();
                if (r0.top < 0 || r0.bottom > window.innerHeight)
                    target.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

                setTimeout(() => {
                    const rect   = target.getBoundingClientRect();
                    const PAD    = 10, TIP_W = 400, MARGIN = 16;
                    const vw = window.innerWidth, vh = window.innerHeight;

                    let spotTop = rect.top, spotHeight = rect.height;
                    if (selector.includes('status-spalte')) {
                        const table = document.querySelector("[tour-id='bestand-tabelle']");
                        if (table) spotHeight = table.getBoundingClientRect().bottom - rect.top;
                    }

                    spotlight.style.display = 'block';
                    spotlight.style.top    = (spotTop   - PAD) + 'px';
                    spotlight.style.left   = (rect.left - PAD) + 'px';
                    spotlight.style.width  = (rect.width  + PAD*2) + 'px';
                    spotlight.style.height = (spotHeight  + PAD*2) + 'px';

                    const tipH = Math.max(el.getBoundingClientRect().height, 160);
                    let top, left;
                    if      (tipCentered)    { el.style.transform='translate(-50%,-50%)'; el.style.top='50%'; el.style.left='50%'; return; }
                    else if (pos==='bottom') { top=rect.bottom+MARGIN;             left=rect.left+rect.width/2-TIP_W/2; }
                    else if (pos==='top')    { top=rect.top-tipH-MARGIN;           left=rect.left+rect.width/2-TIP_W/2; }
                    else if (pos==='right')  { top=rect.top+rect.height/2-tipH/2; left=rect.right+MARGIN; }
                    else if (pos==='left')   { top=rect.top+rect.height/2-tipH/2; left=rect.left-TIP_W-MARGIN; }
                    else                     { top=vh/2-tipH/2;                    left=vw/2-TIP_W/2; }

                    el.style.transform = 'none';
                    el.style.top  = Math.max(MARGIN, Math.min(top,  vh-tipH-MARGIN)) + 'px';
                    el.style.left = Math.max(MARGIN, Math.min(left, vw-TIP_W-MARGIN)) + 'px';

                    target.classList.add('tour-target-highlight');
                    setTimeout(() => target.classList.remove('tour-target-highlight'), 1400);
                }, 150);
            })(this.$server ? this : this.getRootNode().host || this);
            """, selector, step.position(), tipCentered);
    }

    private void closeOpenDialogs() {
        getElement().executeJs("""
            document.querySelectorAll('vaadin-dialog-overlay').forEach(d => {
                d.style.pointerEvents = '';
                if (typeof d.close === 'function') d.close();
                else d.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', bubbles:true}));
            });
        """);
    }

    private void injectSpotlight() {
        getElement().executeJs("""
            document.getElementById('__tour_spotlight')?.remove();
            document.getElementById('__tour_styles')?.remove();

            const style = document.createElement('style');
            style.id = '__tour_styles';
            style.textContent = `
              #__tour_spotlight {
                position:fixed; display:none; background:transparent;
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
                "const s=document.getElementById('__tour_spotlight'); if(s) s.style.display='none';");
    }

    private void finish() {
        pendingCallback = null;
        UI.getCurrent().getPage().reload();
    }
}