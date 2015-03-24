DROP VIEW IF EXISTS "EstModelPrimView";

CREATE VIEW "EstModelPrimView" AS

SELECT
    "Formel",
    "Dependent",
    "Independent",
    "Parametername",
    "Wert",
    "Name",
    "Modellkatalog"."ID" AS "Modell",
    "GeschaetzteModelle"."ID" AS "GeschaetztesModell",
    "RMS",
    "Rsquared",
    "AIC",
    "BIC",
    "min",
    "max",
    "minIndep",
    "maxIndep",
    "LitMID",
    "LitM",
    "LitEmID",
    "LitEm",
    "Versuchsbedingung",
    "StandardError",
    "VarParMap"


FROM "GeschaetzteModelle"

LEFT JOIN "Modellkatalog"
ON "Modellkatalog"."ID"="GeschaetzteModelle"."Modell"

LEFT JOIN(

    SELECT
        "GeschaetztesModell",
        GROUP_CONCAT(
            CASE
                WHEN "VarParMap" IS NOT NULL
                THEN "VarParMap"
                ELSE "Parametername"
            END
        )AS "Dependent"

    FROM "GeschaetzteModelle"

    JOIN "Modellkatalog"
    ON "Modellkatalog"."ID"="GeschaetzteModelle"."Modell"

    JOIN "ModellkatalogParameter"
    ON "ModellkatalogParameter"."Modell"="Modellkatalog"."ID"

    LEFT JOIN "VarParMaps"
    ON "VarParMaps"."VarPar"="ModellkatalogParameter"."ID" AND "VarParMaps"."GeschaetztesModell"="GeschaetzteModelle"."ID"

    WHERE "Parametertyp"=3
    GROUP BY "GeschaetztesModell"

)"DepVarView"
ON "GeschaetzteModelle"."ID"="DepVarView"."GeschaetztesModell"

LEFT JOIN(

    SELECT
        "GeschaetztesModell",
        ARRAY_AGG(
            CASE
                WHEN "VarParMap" IS NOT NULL
                THEN "VarParMap"
                ELSE "Parametername"
            END
        )AS "Independent",
        ARRAY_AGG( "Gueltig_von" )AS "minIndep",
        ARRAY_AGG( "Gueltig_bis" )AS "maxIndep"

    FROM "GeschaetzteModelle"

    JOIN "GueltigkeitsBereiche"
    ON "GeschaetzteModelle"."ID"="GueltigkeitsBereiche"."GeschaetztesModell"

    JOIN "ModellkatalogParameter"
    ON "GueltigkeitsBereiche"."Parameter"="ModellkatalogParameter"."ID"

    LEFT JOIN "VarParMaps"
    ON "VarParMaps"."VarPar"="ModellkatalogParameter"."ID" AND "VarParMaps"."GeschaetztesModell"="GeschaetzteModelle"."ID"

    WHERE "Parametertyp"=1
    GROUP BY "GeschaetztesModell"

)"IndepVarView"
ON "GeschaetzteModelle"."ID"="IndepVarView"."GeschaetztesModell"

LEFT JOIN(

    SELECT
        "GeschaetztesModell",
        ARRAY_AGG(
            CASE
                WHEN "VarParMap" IS NOT NULL
                THEN "VarParMap"
                ELSE "Parametername"
            END
        )AS "Parametername",
        ARRAY_AGG( "Wert" )AS "Wert",
        ARRAY_AGG( "min" )AS "min",
        ARRAY_AGG( "max" )AS "max",
        ARRAY_AGG( "StandardError" )AS "StandardError"

    FROM "GeschaetzteModelle"

    JOIN "GeschaetzteParameter"
    ON "GeschaetzteModelle"."ID"="GeschaetzteParameter"."GeschaetztesModell"

    JOIN "ModellkatalogParameter"
    ON "GeschaetzteParameter"."Parameter"="ModellkatalogParameter"."ID"

    LEFT JOIN "VarParMaps"
    ON "VarParMaps"."VarPar"="ModellkatalogParameter"."ID" AND "VarParMaps"."GeschaetztesModell"="GeschaetzteModelle"."ID"

    WHERE "Parametertyp"=2
    GROUP BY "GeschaetztesModell"

)"ParamView"
ON "GeschaetzteModelle"."ID"="ParamView"."GeschaetztesModell"

LEFT JOIN (

    SELECT
        "Modell",
        GROUP_CONCAT( CONCAT( "Erstautor", '_', "Jahr" ) )AS "LitM",
        GROUP_CONCAT( "Literatur"."ID" )AS "LitMID"
    FROM "Modell_Referenz"
    JOIN "Literatur"
    ON "Modell_Referenz"."Literatur"="Literatur"."ID"

    GROUP BY "Modell"

)"LitMView"
ON "Modellkatalog"."ID"="LitMView"."Modell"

LEFT JOIN (

    SELECT
        "GeschaetztesModell",
        GROUP_CONCAT( CONCAT( "Erstautor", '_', "Jahr" ) )AS "LitEm",
        GROUP_CONCAT( "Literatur"."ID" )AS "LitEmID"

    FROM "GeschaetztesModell_Referenz"

    JOIN "Literatur"
    ON "GeschaetztesModell_Referenz"."Literatur"="Literatur"."ID"

    GROUP BY "GeschaetztesModell"
)"LitEmView"
ON "GeschaetzteModelle"."ID"="LitEmView"."GeschaetztesModell"

LEFT JOIN(

    SELECT

        "VarParMaps"."GeschaetztesModell",
        GROUP_CONCAT(
            CONCAT(
                "VarParMaps"."VarParMap",
                '=',
                "ModellkatalogParameter"."Parametername" ) )AS "VarParMap"

    FROM "VarParMaps"

    JOIN "ModellkatalogParameter"
    ON "ModellkatalogParameter"."ID"="VarParMaps"."VarPar" 

    GROUP BY "VarParMaps"."GeschaetztesModell"

)"VarParMapView"
ON "VarParMapView"."GeschaetztesModell"="GeschaetzteModelle"."ID"

WHERE "Level"=1;

GRANT SELECT ON TABLE "EstModelPrimView" TO "PUBLIC";