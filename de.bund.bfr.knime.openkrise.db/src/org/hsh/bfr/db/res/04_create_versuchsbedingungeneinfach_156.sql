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

    CASE
        WHEN "P"."Wert" IS NULL
        THEN "Psubst"."Wert"
        ELSE "P"."Wert"
    END "pH",

    CASE
        WHEN "A"."Wert" IS NULL
        THEN "Asubst"."Wert"
        ELSE "A"."Wert"
    END AS "aw",

    "O"."Wert" AS "CO2",
    "D"."Wert" AS "Druck",
    "L"."Wert" AS "Luftfeuchtigkeit",
    "Versuchsbedingungen"."Sonstiges",
    "Versuchsbedingungen"."Kommentar",
    "Versuchsbedingungen"."Guetescore",
    "Versuchsbedingungen"."Geprueft"

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
ON "Versuchsbedingungen"."Luftfeuchtigkeit"="L"."ID"

LEFT JOIN "Matrices"
ON "Versuchsbedingungen"."Matrix"="Matrices"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "Psubst"
ON "Matrices"."pH"="Psubst"."ID"

LEFT JOIN "DoubleKennzahlenEinfach" AS "Asubst"
ON "Matrices"."aw"="Asubst"."ID";

GRANT SELECT ON TABLE "VersuchsbedingungenEinfach" TO "PUBLIC";