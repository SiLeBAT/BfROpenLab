DROP VIEW IF EXISTS "MesswerteEinfach";

CREATE VIEW "MesswerteEinfach" AS

SELECT

    "ID",
    "Versuchsbedingungen" AS "Versuchsbedingung",
	"T"."Wert" AS "Zeit",
	"ZE"."display in GUI as" AS "ZeitEinheit",
	"K"."Wert" AS "Konzentration",
	"KE"."display in GUI as" AS "KonzentrationsEinheit",
    "C"."Wert" AS "Temperatur",
    "P"."Wert" AS "pH",
    "A"."Wert" AS "aw",
    "Q"."Wert" AS "Druck",
    "R"."Wert" AS "CO2",
    "S"."Wert" AS "Luftfeuchtigkeit",
    "Messwerte"."Sonstiges",
    "Messwerte"."Kommentar",
    "Messwerte"."Delta"

FROM "Messwerte"

JOIN "DoubleKennzahlenEinfach" AS "T"
ON "Messwerte"."Zeit"="T"."ID"

JOIN "DoubleKennzahlenEinfach" AS "K"
ON "Messwerte"."Konzentration"="K"."ID"

JOIN "Einheiten" AS "ZE"
ON "Messwerte"."ZeitEinheit"="ZE"."ID"

JOIN "Einheiten" AS "KE"
ON "Messwerte"."Konz_Einheit"="KE"."ID"

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

GRANT SELECT ON TABLE "MesswerteEinfach" TO "PUBLIC";