DROP VIEW IF EXISTS "SeiView";
DROP VIEW IF EXISTS "PeiView";
DROP VIEW IF EXISTS "MicrobialDataView";
DROP VIEW IF EXISTS "VersuchsbedingungenEinfach";
DROP VIEW IF EXISTS "MesswerteEinfach";

CREATE VIEW "MesswerteEinfach" AS

SELECT

    "ID",
    "Versuchsbedingungen" AS "Versuchsbedingung",

    CASE
        WHEN "Messwerte"."ZeitEinheit" LIKE 'Stunde'
        THEN "T"."Wert"
        WHEN "Messwerte"."ZeitEinheit" LIKE 'Minute'
        THEN "T"."Wert"/60
        WHEN "Messwerte"."ZeitEinheit" LIKE 'Sekunde'
        THEN "T"."Wert"/3600
        WHEN "Messwerte"."ZeitEinheit" LIKE 'Tag'
        THEN "T"."Wert"*24
        WHEN "Messwerte"."ZeitEinheit" LIKE 'Woche'
        THEN "T"."Wert"*168
        WHEN "Messwerte"."ZeitEinheit" LIKE 'Monat'
        THEN "T"."Wert"*730.5
        WHEN "Messwerte"."ZeitEinheit" LIKE 'Jahr'
        THEN "T"."Wert"*8766
        ELSE NULL
    END AS "Zeit",

    CASE
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", 'log(10)?.*( pro |/)25(g|m[lL])' )
        THEN "K"."Wert"-LOG10( 25 )
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", 'log(10)?.*( pro |/)(kg|[lL])' )
        THEN "K"."Wert"-3
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", 'log(10)?.*( pro |/)100(g|m[lL])' )
        THEN "K"."Wert"-2
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", 'log(10)?.*( pro |/)0\.1(g|m[lL])' )
        THEN "K"."Wert"+1
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", 'log(10)?.*( pro |/)(g|m[lL])' )
        THEN "K"."Wert"    
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", '.*( pro |/)25(g|m[lL])' )
        THEN LOG10( "K"."Wert"/25 )
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", '.*( pro |/)(kg|[lL])' )
        THEN LOG10( "K"."Wert" )-3
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", '.*( pro |/)100(g|m[lL])' )
        THEN LOG10( "K"."Wert" )-2
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", '.*( pro |/)0\.1(g|m[lL])' )
        THEN LOG10( "K"."Wert" )+1
        WHEN REGEXP_MATCHES( "Einheiten"."Einheit", '.*( pro |/)(g|m[lL])' )
        THEN CASE
            WHEN "K"."Wert" <= 1
            THEN 0
            ELSE LOG10( "C"."Wert" )
        END
        ELSE NULL
    END AS "Konzentration",

    "C"."Wert" AS "Temperatur",
    "P"."Wert" AS "pH",
    "A"."Wert" AS "aw",
    "Q"."Wert" AS "Druck",
    "R"."Wert" AS "CO2",
    "S"."Wert" AS "Luftfeuchtigkeit",
    "Messwerte"."Sonstiges",
    "Messwerte"."Kommentar"

FROM "Messwerte"

JOIN "DoubleKennzahlenEinfach" AS "T"
ON "Messwerte"."Zeit"="T"."ID"

JOIN "DoubleKennzahlenEinfach" AS "K"
ON "Messwerte"."Konzentration"="K"."ID"

JOIN "Einheiten"
ON "Messwerte"."Konz_Einheit"="Einheiten"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "C"
ON "Messwerte"."Temperatur"="C"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "P"
ON "Messwerte"."pH"="P"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "A"
ON "Messwerte"."aw"="A"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "Q"
ON "Messwerte"."Druck"="Q"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "R"
ON "Messwerte"."CO2"="R"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "S"
ON "Messwerte"."Luftfeuchtigkeit"="S"."ID"

WHERE "Delta" IS NULL OR NOT "Delta";

GRANT SELECT ON TABLE "MesswerteEinfach" TO "PUBLIC";