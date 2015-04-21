DROP VIEW IF EXISTS "SonstigesEinfach";

CREATE VIEW "SonstigesEinfach" AS

SELECT

    "Versuchsbedingungen_Sonstiges"."Versuchsbedingungen" AS "Versuchsbedingung",
    ARRAY_AGG("SonstigeParameter"."ID") AS "SonstigesID",
    ARRAY_AGG("SonstigeParameter"."Parameter") AS "Parameter",
    ARRAY_AGG("SonstigeParameter"."Beschreibung") AS "Beschreibung",
    ARRAY_AGG("Einheiten"."Einheit") AS "Einheit",
    ARRAY_AGG("DoubleKennzahlenEinfach"."Wert") AS "Wert"

FROM "Versuchsbedingungen_Sonstiges"

LEFT JOIN "Einheiten"
ON "Versuchsbedingungen_Sonstiges"."Einheit"="Einheiten"."ID"

JOIN "SonstigeParameter"
ON "Versuchsbedingungen_Sonstiges"."SonstigeParameter"="SonstigeParameter"."ID"

LEFT JOIN "DoubleKennzahlenEinfach"
ON "Versuchsbedingungen_Sonstiges"."Wert"="DoubleKennzahlenEinfach"."ID"

GROUP BY "Versuchsbedingungen";

GRANT SELECT ON TABLE "SonstigesEinfach" TO "PUBLIC";