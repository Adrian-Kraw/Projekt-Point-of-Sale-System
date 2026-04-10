package de.fhswf.kassensystem.views.verkauf;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;
import java.util.function.Consumer;

/**
 * Horizontale Chip-Gruppe für die Kategorie-Filterung in der Kassier-View.
 */
class KategorieChipGroup extends HorizontalLayout {

    private String aktiveKategorie = "Alle";

    KategorieChipGroup(List<String> kategorien, Consumer<String> onSelect) {
        setSpacing(false);
        getStyle().set("gap", "0.75rem").set("margin-bottom", "2rem").set("flex-wrap", "wrap");

        add(buildChip("Alle", true, onSelect));
        kategorien.stream().sorted().forEach(kat -> add(buildChip(kat, false, onSelect)));
    }

    private Button buildChip(String label, boolean aktiv, Consumer<String> onSelect) {
        Button chip = new Button(label);
        stilisiere(chip, aktiv);

        chip.addClickListener(e -> {
            aktiveKategorie = label;
            getChildren().forEach(c -> {
                if (c instanceof Button b) stilisiere(b, b.getText().equals(label));
            });
            onSelect.accept(label);
        });
        return chip;
    }

    private void stilisiere(Button chip, boolean aktiv) {
        chip.getStyle()
                .set("border-radius", "9999px").set("padding", "0.625rem 1.5rem")
                .set("font-size", "0.875rem").set("font-weight", aktiv ? "700" : "500")
                .set("border", "none").set("cursor", "pointer")
                .set("font-family", "'Plus Jakarta Sans', sans-serif")
                .set("background", aktiv ? "#553722" : "#e8e5ff")
                .set("color", aktiv ? "white" : "#50453e")
                .set("box-shadow", aktiv ? "0 4px 15px rgba(85,55,34,0.2)" : "none");
    }

    String getAktiveKategorie() {
        return aktiveKategorie;
    }
}
