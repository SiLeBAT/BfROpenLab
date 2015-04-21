DROP VIEW IF EXISTS "SonstigesEinfach";

CREATE VIEW "SonstigesEinfach" AS

SELECT

    "Versuchsbedingungen_Sonstiges"."Versuchsbedingungen" AS "Versuchsbedingung",
    "SonstigeParameter"."ID" AS "SonstigesID",
    "SonstigeParameter"."Parameter",
    "SonstigeParameter"."Beschreibung",
    "Einheiten"."Einheit",
    "DoubleKennzahlenEinfach"."Wert"

FROM "Versuchsbedingungen_Sonstiges"

LEFT JOIN "Einheiten"
ON "Versuchsbedingungen_Sonstiges"."Einheit"="Einheiten"."ID"

JOIN "SonstigeParameter"
ON "Versuchsbedingungen_Sonstiges"."SonstigeParameter"="SonstigeParameter"."ID"

LEFT JOIN "DoubleKennzahlenEinfach"
ON "Versuchsbedingungen_Sonstiges"."Wert"="DoubleKennzahlenEinfach"."ID";

GRANT SELECT ON TABLE "SonstigesEinfach" TO "PUBLIC";