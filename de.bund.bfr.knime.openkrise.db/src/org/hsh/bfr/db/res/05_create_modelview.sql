DROP VIEW IF EXISTS "ModelView";

CREATE VIEW "ModelView" AS

SELECT

"Formel",
"P"."Parametername",
"D"."Parametername" AS "Dependent",
"I"."Parametername" AS "Independent",
"Modellkatalog"."Name",
"Modellkatalog"."ID" AS "Modell",
"minValue",
"maxValue",
"minIndep",
"maxIndep",
"Literatur",
"ReferenzText",
"Level",
"Klasse"

FROM "Modellkatalog"

LEFT JOIN(
    SELECT
        "Modell",
        GROUP_CONCAT( "Literatur"."ID" )AS "Literatur",
        GROUP_CONCAT( CONCAT( "Erstautor", '_', "Jahr" ) )AS "ReferenzText"
    FROM "Modell_Referenz"
    JOIN "Literatur"
    ON "Modell_Referenz"."Literatur"="Literatur"."ID"
    GROUP BY "Modell"
)AS "LitView"
ON "Modellkatalog"."ID"="LitView"."Modell"

LEFT JOIN(
    SELECT "Modell", "Parametername"
    FROM "ModellkatalogParameter"
    WHERE "Parametertyp"=3 )AS "D"
ON "Modellkatalog"."ID"="D"."Modell"

LEFT JOIN(
    SELECT
        "Modell",
        ARRAY_AGG( "Parametername" )AS "Parametername",
        ARRAY_AGG( "min" )AS "minIndep",
        ARRAY_AGG( "max" )AS "maxIndep"
    FROM "ModellkatalogParameter"
    WHERE "Parametertyp"=1
    GROUP BY "Modell" )AS "I"
ON "Modellkatalog"."ID"="I"."Modell"

LEFT JOIN(
    SELECT
        "Modell",
        ARRAY_AGG( "Parametername" )AS "Parametername",
        ARRAY_AGG( "min" )AS "minValue",
        ARRAY_AGG( "max" )AS "maxValue"
    FROM "ModellkatalogParameter"
    WHERE "Parametertyp"=2
    GROUP BY "Modell" )AS "P"
ON "Modellkatalog"."ID"="P"."Modell";

GRANT SELECT ON TABLE "ModelView" TO "PUBLIC";