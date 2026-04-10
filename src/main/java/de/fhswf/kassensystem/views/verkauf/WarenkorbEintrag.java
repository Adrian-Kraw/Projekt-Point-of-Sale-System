package de.fhswf.kassensystem.views.verkauf;

import de.fhswf.kassensystem.model.Artikel;

/**
 * Hält Artikel und Menge für einen Warenkorb-Eintrag.
 */
class WarenkorbEintrag {

    final Artikel artikel;
    int menge;

    WarenkorbEintrag(Artikel artikel, int menge) {
        this.artikel = artikel;
        this.menge   = menge;
    }
}
