DROP VIEW IF EXISTS "EstModelSecView";

CREATE VIEW "EstModelSecView" AS

SELECT
    "Formel" AS "Formel2",
    "Dependent" AS "Dependent2",
    "Independent" AS "Independent2",
    "Parametername" AS "Parametername2",
    "Wert" AS "Wert2",
    "Name" AS "Name2",
    "Modellkatalog"."ID" AS "Modell2",
    "GeschaetzteModelle"."ID" AS "GeschaetztesModell2",
    "RMS" AS "RMS2",
    "Rsquared" AS "Rsquared2",
    "AIC" AS "AIC2",
    "BIC" AS "BIC2",
    "min" AS "min2",
    "max" AS "max2",
    "minIndep" AS "minIndep2",
    "maxIndep" AS "maxIndep2",
    "LitMID" AS "LitMID2",
    "LitM" AS "LitM2",
    "LitEmID" AS "LitEmID2",
    "LitEm" AS "LitEm2",
    "Versuchsbedingung" AS "Versuchsbedingung2",
    "StandardError" AS "StandardError2",
    "VarParMap" AS "VarParMap2"

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

WHERE "Level"=2;

GRANT SELECT ON TABLE "EstModelSecView" TO "PUBLIC";