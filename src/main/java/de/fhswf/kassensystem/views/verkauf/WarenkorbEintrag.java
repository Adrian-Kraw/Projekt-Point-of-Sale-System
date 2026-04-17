package de.fhswf.kassensystem.views.verkauf;

import de.fhswf.kassensystem.model.Artikel;

/**
 * Einfaches Datenobjekt für einen Warenkorb-Eintrag.
 *
 * <p>Hält den Artikel und die aktuell gewünschte Menge.
 * Die Menge wird direkt von {@link WarenkorbPositionFactory} und
 * {@link VerkaufView} manipuliert (kein Setter nötig).
 *
 * @author Adrian
 */
class WarenkorbEintrag {

    final Artikel artikel;
    int menge;

    /**
     * Erstellt einen neuen Warenkorb-Eintrag.
     *
     * @param artikel der Artikel
     * @param menge   die anfängliche Menge (typischerweise 1)
     */
    WarenkorbEintrag(Artikel artikel, int menge) {
        this.artikel = artikel;
        this.menge   = menge;
    }
}
