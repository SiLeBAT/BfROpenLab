DROP VIEW IF EXISTS "SeiView";
DROP VIEW IF EXISTS "PeiView";
DROP VIEW IF EXISTS "MicrobialDataView";
DROP VIEW IF EXISTS "VersuchsbedingungenEinfach";

CREATE VIEW "VersuchsbedingungenEinfach" AS

SELECT

    "Versuchsbedingungen"."ID",
    "Versuchsbedingungen"."Referenz",
    "Versuchsbedingungen"."Agens",
    "Versuchsbedingungen"."AgensDetail",
    "Versuchsbedingungen"."Matrix",
    "Versuchsbedingungen"."MatrixDetail",
    "C"."Wert" AS "Temperatur",
    "P"."Wert" AS "pH",
    "A"."Wert" AS "aw",
    "O"."Wert" AS "CO2",
    "D"."Wert" AS "Druck",
    "L"."Wert" AS "Luftfeuchtigkeit",
    "Versuchsbedingungen"."Sonstiges",
    "Versuchsbedingungen"."Kommentar"

FROM

"Versuchsbedingungen"

LEFT JOIN "DoubleKennzahlenEinfach" AS "C"
ON "Versuchsbedingungen"."Temperatur"="C"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "P"
ON "Versuchsbedingungen"."pH"="P"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "A"
ON "Versuchsbedingungen"."aw"="A"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "O"
ON "Versuchsbedingungen"."CO2"="O"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "D"
ON "Versuchsbedingungen"."Druck"="D"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "L"
ON "Versuchsbedingungen"."Luftfeuchtigkeit"="L"."ID";

GRANT SELECT ON TABLE "VersuchsbedingungenEinfach" TO "PUBLIC";