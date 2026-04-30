-- =============================================================
-- CANAPÉ CAFÉ – Wochensimulation (Montag bis Samstag) für den 20.04.2026 bis 25.04.2026
-- =============================================================
-- VORAUSSETZUNGEN:
--   Artikel, Kategorien, MwSt-Sätze und User müssen bereits
--   in der DB vorhanden sein (Stammdaten aus dem System).
-- AUSFÜHREN:
--   psql -U postgres -d kassensystem -f simulation.sql
-- =============================================================

BEGIN;

DO $$
    DECLARE
        u_kassierer    BIGINT;
        u_manager      BIGINT;

        a_ids          BIGINT[];
        a_preise       NUMERIC[];
        a_bestaende    INT[];
        artikel_count  INT;

        lieferanten    TEXT[] := ARRAY['Bäckerei Schulz', 'Kaffeerösterei Müller', 'Großhandel Frisch GmbH', 'Getränke Depot AG'];

        v_id           BIGINT;
        pos_count      INT;
        art_idx        INT;
        menge          INT;
        einzelpreis    NUMERIC;
        v_gesamtsumme  NUMERIC;
        rabatt         NUMERIC;
        zahlungsart    TEXT;
        v_ts           TIMESTAMP;
        tag_offset     INT;
        stunde         INT;
        minute_val     INT;
        i              INT;
        j              INT;
        verkauf_count  INT;
        neuer_bestand  INT;
        wi_menge       INT;

        wochentage        TEXT[] := ARRAY['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'];
        verkauefe_pro_tag INT[] := ARRAY[12, 10, 11, 13, 18, 22];

    BEGIN
        -- User laden
        SELECT id INTO u_kassierer
        FROM kassensystem_user WHERE benutzername = 'Stefan' AND aktiv = true LIMIT 1;
        IF u_kassierer IS NULL THEN RAISE EXCEPTION 'Benutzer "Stefan" nicht gefunden!'; END IF;

        SELECT id INTO u_manager
        FROM kassensystem_user WHERE benutzername = 'Tobias' AND aktiv = true LIMIT 1;
        IF u_manager IS NULL THEN RAISE EXCEPTION 'Benutzer "Tobias" nicht gefunden!'; END IF;

        RAISE NOTICE 'User: Stefan=%, Tobias=%', u_kassierer, u_manager;

        -- Aktive Artikel laden
        SELECT
            ARRAY_AGG(id ORDER BY id),
            ARRAY_AGG(preis ORDER BY id),
            ARRAY_AGG(bestand ORDER BY id)
        INTO a_ids, a_preise, a_bestaende
        FROM artikel WHERE aktiv = true;

        artikel_count := ARRAY_LENGTH(a_ids, 1);
        RAISE NOTICE 'Aktive Artikel gefunden: %', artikel_count;

        IF artikel_count IS NULL OR artikel_count = 0 THEN
            RAISE EXCEPTION 'Keine aktiven Artikel gefunden. Bitte erst Stammdaten anlegen.';
        END IF;


        -- 1. WARENEINGÄNGE
        ------------------------------------------------------------
        RAISE NOTICE 'Lege Wareneingänge an...';

        -- Montag 06:30: Hauptlieferung
        FOR i IN 1..LEAST(4, artikel_count) LOOP
                wi_menge := 20 + (RANDOM() * 30)::INT;
                INSERT INTO wareneingang (artikel_id, menge, kommentar, lieferant, datum, status, bestellt_von_id, bestellt_am)
                VALUES (
                           a_ids[i],
                           wi_menge,
                           'Wöchentliche Regellieferung',
                           lieferanten[1 + (i % 2)],
                           DATE '2026-04-20',
                           'BESTAETIGT',
                           u_manager,
                           TIMESTAMP '2026-04-20 06:30:00'
                       );
                UPDATE artikel SET bestand = bestand + wi_menge WHERE id = a_ids[i];
                a_bestaende[i] := a_bestaende[i] + wi_menge;
            END LOOP;

        -- Mittwoch 07:00: Getränkelieferung
        FOR i IN LEAST(5, artikel_count)..LEAST(8, artikel_count) LOOP
                wi_menge := 15 + (RANDOM() * 25)::INT;
                INSERT INTO wareneingang (artikel_id, menge, kommentar, lieferant, datum, status, bestellt_von_id, bestellt_am)
                VALUES (
                           a_ids[i],
                           wi_menge,
                           'Getränke-Nachlieferung Mitte der Woche',
                           lieferanten[4],
                           DATE '2026-04-22',
                           'BESTAETIGT',
                           u_manager,
                           TIMESTAMP '2026-04-22 07:00:00'
                       );
                UPDATE artikel SET bestand = bestand + wi_menge WHERE id = a_ids[i];
                a_bestaende[i] := a_bestaende[i] + wi_menge;
            END LOOP;

        -- Freitag 06:45: Grosslieferung für Wochenende
        FOR i IN 1..LEAST(6, artikel_count) LOOP
                wi_menge := 30 + (RANDOM() * 20)::INT;
                INSERT INTO wareneingang (artikel_id, menge, kommentar, lieferant, datum, status, bestellt_von_id, bestellt_am)
                VALUES (
                           a_ids[i],
                           wi_menge,
                           'Wochenend-Aufstockung',
                           lieferanten[3],
                           DATE '2026-04-24',
                           'BESTAETIGT',
                           u_manager,
                           TIMESTAMP '2026-04-24 06:45:00'
                       );
                UPDATE artikel SET bestand = bestand + wi_menge WHERE id = a_ids[i];
                a_bestaende[i] := a_bestaende[i] + wi_menge;
            END LOOP;

        -- Samstag: ausstehender Wareneingang
        INSERT INTO wareneingang (artikel_id, menge, kommentar, lieferant, datum, status, bestellt_von_id, bestellt_am)
        VALUES (
                   a_ids[1],
                   20,
                   'Notbestellung - Bestand zu niedrig',
                   lieferanten[1],
                   DATE '2026-04-25',
                   'AUSSTEHEND',
                   u_manager,
                   TIMESTAMP '2026-04-25 08:00:00'
               );


        -- 2. VERKÄUFE – Montag bis Samstag
        ------------------------------------------------------------

        RAISE NOTICE 'Lege Verkäufe an...';

        FOR tag_offset IN REVERSE 5..0 LOOP
                verkauf_count := verkauefe_pro_tag[6 - tag_offset];
                RAISE NOTICE 'Simuliere % Verkäufe für %', verkauf_count, wochentage[6 - tag_offset];

                FOR j IN 1..verkauf_count LOOP

                        CASE
                            WHEN j % 5 = 0 THEN stunde := 7 + (RANDOM() * 2)::INT;
                            WHEN j % 5 = 1 THEN stunde := 11 + (RANDOM() * 2)::INT;
                            WHEN j % 5 = 2 THEN stunde := 14 + (RANDOM() * 2)::INT;
                            WHEN j % 5 = 3 THEN stunde := 9 + (RANDOM() * 1)::INT;
                            ELSE                 stunde := 15 + (RANDOM() * 2)::INT;
                            END CASE;
                        minute_val := (RANDOM() * 59)::INT;

                        v_ts := (DATE '2026-04-25' - (tag_offset || ' days')::INTERVAL)
                            + (stunde || ' hours')::INTERVAL
                            + (minute_val || ' minutes')::INTERVAL;

                        IF tag_offset = 0 THEN
                            zahlungsart := CASE WHEN RANDOM() < 0.45 THEN 'BAR' ELSE 'KARTE' END;
                        ELSE
                            zahlungsart := CASE WHEN RANDOM() < 0.62 THEN 'BAR' ELSE 'KARTE' END;
                        END IF;

                        rabatt := CASE
                                      WHEN RANDOM() < 0.12 THEN 0.50
                                      WHEN RANDOM() < 0.05 THEN 1.00
                                      ELSE 0.00
                            END;

                        INSERT INTO verkauf (timestamp, gesamtsumme, zahlungsart, user_id, rabatt, status)
                        VALUES (
                                   v_ts,
                                   0,
                                   zahlungsart,
                                   CASE WHEN stunde < 13 THEN u_kassierer ELSE u_manager END,
                                   rabatt,
                                   'ABGESCHLOSSEN'
                               )
                        RETURNING id INTO v_id;

                        pos_count := 1 + (RANDOM() * 3)::INT;
                        v_gesamtsumme := 0;

                        FOR i IN 1..pos_count LOOP
                                art_idx := 1 + (RANDOM() * (artikel_count - 1))::INT;
                                einzelpreis := a_preise[art_idx];
                                menge := 1 + (RANDOM() * 2)::INT;

                                neuer_bestand := a_bestaende[art_idx] - menge;
                                IF neuer_bestand < 2 THEN
                                    menge := GREATEST(0, a_bestaende[art_idx] - 2);
                                    IF menge = 0 THEN CONTINUE; END IF;
                                    neuer_bestand := 2;
                                END IF;
                                a_bestaende[art_idx] := neuer_bestand;

                                INSERT INTO verkaufsposition (menge, einzelpreis, artikel_id, verkauf_id)
                                VALUES (menge, einzelpreis, a_ids[art_idx], v_id);

                                v_gesamtsumme := v_gesamtsumme + (einzelpreis * menge);
                            END LOOP;

                        v_gesamtsumme := GREATEST(0, v_gesamtsumme - rabatt);
                        UPDATE verkauf SET gesamtsumme = v_gesamtsumme WHERE id = v_id;

                    END LOOP;
            END LOOP;

        -- Bestand synchronisieren
        FOR i IN 1..artikel_count LOOP
                UPDATE artikel SET bestand = a_bestaende[i] WHERE id = a_ids[i];
            END LOOP;


        -- 3. STORNIERUNGEN
        ------------------------------------------------------------

        RAISE NOTICE 'Lege Stornierungen an...';

        INSERT INTO verkauf (timestamp, gesamtsumme, zahlungsart, user_id, rabatt, status)
        SELECT
            TIMESTAMP '2026-04-20 10:15:00',
            a_preise[1],
            'BAR',
            u_kassierer,
            0.00,
            'STORNIERT'
        FROM artikel WHERE aktiv = true ORDER BY id LIMIT 1
        RETURNING id INTO v_id;

        INSERT INTO verkaufsposition (menge, einzelpreis, artikel_id, verkauf_id)
        SELECT 1, preis, id, v_id FROM artikel WHERE aktiv = true ORDER BY id LIMIT 1;

        INSERT INTO verkauf (timestamp, gesamtsumme, zahlungsart, user_id, rabatt, status)
        SELECT
            TIMESTAMP '2026-04-23 14:45:00',
            a_preise[LEAST(2, artikel_count)],
            'KARTE',
            u_manager,
            0.00,
            'STORNIERT'
        FROM artikel WHERE aktiv = true ORDER BY id LIMIT 1
        RETURNING id INTO v_id;

        INSERT INTO verkaufsposition (menge, einzelpreis, artikel_id, verkauf_id)
        SELECT 1, preis, id, v_id FROM artikel WHERE aktiv = true ORDER BY id OFFSET 1 LIMIT 1;

        RAISE NOTICE 'Simulation abgeschlossen.';
        RAISE NOTICE 'Wareneingänge: 3 bestätigt, 1 ausstehend';
        RAISE NOTICE 'Verkäufe: Mo=12, Di=10, Mi=11, Do=13, Fr=18, Sa=22, Stornos=2';

    END $$;

COMMIT;


-- ZUSAMMENFASSUNG
------------------------------------------------------------
SELECT
    'Verkäufe gesamt'      AS info,
    COUNT(*)::TEXT         AS wert
FROM verkauf
UNION ALL
SELECT 'davon ABGESCHLOSSEN', COUNT(*)::TEXT FROM verkauf WHERE status = 'ABGESCHLOSSEN'
UNION ALL
SELECT 'davon STORNIERT',     COUNT(*)::TEXT FROM verkauf WHERE status = 'STORNIERT'
UNION ALL
SELECT 'Wareneingänge',       COUNT(*)::TEXT FROM wareneingang
UNION ALL
SELECT 'davon BESTAETIGT',    COUNT(*)::TEXT FROM wareneingang WHERE status = 'BESTAETIGT'
UNION ALL
SELECT 'davon AUSSTEHEND',    COUNT(*)::TEXT FROM wareneingang WHERE status = 'AUSSTEHEND'
UNION ALL
SELECT 'Umsatz gesamt (€)',   ROUND(SUM(gesamtsumme), 2)::TEXT FROM verkauf WHERE status = 'ABGESCHLOSSEN';