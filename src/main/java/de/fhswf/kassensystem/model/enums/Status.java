package de.fhswf.kassensystem.model.enums;

/**
 * Definiert den möglichen Status eines Verkaufs.
 *
 * <p>
 *     Der Status wird beim Anlegen eines neuen Verkaufs standardmäßig auf "OFFEN" gesetzt und im Laufe des
 *     Kassiervorgangs fortgeschrieben. Er ist relevant für die Berechnung von Tagesabschlüssen und Umsatzberichten.
 * </p>
 *
 * @author Paula Martin
 */
public enum Status {
    OFFEN,
    ABGESCHLOSSEN,
    STORNIERT
}
