package de.fhswf.kassensystem.model.enums;

/**
 * Repräsentiert den möglichen Status eines Wareneingangs.
 *
 * <p>
 *     Der Status wird beim Erfassen eines neuen Wareneingangs gesetzt und steuert, ob die zugehörige Bestandserhöhung
 *     wirksam ist.
 * </p>
 *
 * @author Paula Martin
 */
public enum WareneingangStatus {
    AUSSTEHEND,
    BESTAETIGT,
    STORNIERT
}
