DROP VIEW IF EXISTS "SelectModelView";
DROP VIEW IF EXISTS "EstSeiView";
DROP VIEW IF EXISTS "EstPeiView";
DROP VIEW IF EXISTS "EstModelSecView";
DROP VIEW IF EXISTS "EstModelPrimView";
DROP VIEW IF EXISTS "MicrobialDataView";

DROP VIEW IF EXISTS "VersuchsbedingungenEinfach";
DROP VIEW IF EXISTS "MesswerteEinfach";

DROP VIEW IF EXISTS "DoubleKennzahlenEinfach";
DROP VIEW IF EXISTS "SonstigesEinfach";







CREATE VIEW "DoubleKennzahlenEinfach" AS

SELECT

    "ID",

    CASE
        WHEN "Wert" IS NULL
        THEN CASE
            WHEN "Minimum" IS NULL
            THEN "Maximum"
            ELSE CASE
                WHEN "Maximum" IS NULL
                THEN "Minimum"
                ELSE ( "Minimum"+"Maximum" )/2
            END
        END
        ELSE "Wert"
    END AS "Wert"

FROM(
    SELECT

    ID,
    CASE
        WHEN "Exponent" IS NULL
        THEN "Wert"
        ELSE CASE
            WHEN "Wert" IS NULL
            THEN POWER( 10, "Exponent" )
            ELSE "Wert"*POWER( 10, "Exponent" )
        END
    END AS "Wert",

    CASE
        WHEN "Minimum_exp" IS NULL
        THEN "Minimum"
        ELSE CASE
            WHEN "Minimum" IS NULL
            THEN POWER( 10, "Minimum_exp" )
            ELSE "Minimum"*POWER( 10, "Minimum_exp" )
        END
    END AS "Minimum",

    CASE
        WHEN "Maximum_exp" IS NULL
        THEN "Maximum"
        ELSE CASE
            WHEN "Maximum" IS NULL
            THEN POWER( 10, "Maximum_exp" )
            ELSE "Maximum"*POWER( 10, "Maximum_exp" )
        END
    END AS "Maximum"

    FROM "DoubleKennzahlen"
);








CREATE VIEW "SonstigesEinfach" AS

SELECT

    "Versuchsbedingungen_Sonstiges"."Versuchsbedingungen" AS "Versuchsbedingung",
    "SonstigeParameter"."ID" AS "SonstigesID",

    "SonstigeParameter"."Beschreibung",
    "Einheiten"."Einheit",
    "DoubleKennzahlen"."Wert"

FROM "Versuchsbedingungen_Sonstiges"

LEFT JOIN "Einheiten"
ON "Versuchsbedingungen_Sonstiges"."Einheit"="Einheiten"."ID"

JOIN "SonstigeParameter"
ON "Versuchsbedingungen_Sonstiges"."SonstigeParameter"="SonstigeParameter"."ID"

LEFT JOIN "DoubleKennzahlen"
ON "Versuchsbedingungen_Sonstiges"."Wert"="DoubleKennzahlen"."ID";








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







CREATE VIEW "MicrobialDataView" AS

SELECT

    "VersuchsbedingungenEinfach"."ID" AS "Versuchsbedingung",
    "ImportedCombaseData"."CombaseID",
    "MiscView"."SonstigesID",
    "MiscView"."Sonstiges",
    "VersuchsbedingungenEinfach"."Temperatur",
    "VersuchsbedingungenEinfach"."pH",
    "VersuchsbedingungenEinfach"."aw",
    "VersuchsbedingungenEinfach"."Agens",
    "Agenzien"."Agensname",
    "VersuchsbedingungenEinfach"."AgensDetail",
    "VersuchsbedingungenEinfach"."Matrix",
    "Matrices"."Matrixname",
    "VersuchsbedingungenEinfach"."MatrixDetail",
    "DataView"."Zeit",
    "DataView"."Konzentration",
    "VersuchsbedingungenEinfach"."Kommentar",
    "VersuchsbedingungenEinfach"."Referenz" AS "Literatur",
    CONCAT( "Literatur"."Erstautor", '_', "Literatur"."Jahr" )AS "ReferenzText"

FROM "VersuchsbedingungenEinfach"

LEFT JOIN "ImportedCombaseData"
ON "VersuchsbedingungenEinfach"."ID"="ImportedCombaseData"."Versuchsbedingung"

LEFT JOIN "Agenzien"
ON "VersuchsbedingungenEinfach"."Agens"="Agenzien"."ID"

LEFT JOIN "Matrices"
ON "VersuchsbedingungenEinfach"."Matrix"="Matrices"."ID"

LEFT JOIN "Literatur"
ON "VersuchsbedingungenEinfach"."Referenz"="Literatur"."ID"

LEFT JOIN(

    SELECT

        "Versuchsbedingung",
        GROUP_CONCAT( "SonstigesID" )AS "SonstigesID",

    GROUP_CONCAT(
        CONCAT(
            "Beschreibung",
            CASE
                WHEN "Einheit" IS NULL
                THEN ''
                ELSE CONCAT( '(', "Einheit", ')' )
            END,
            CASE
                WHEN "Wert" IS NULL
                THEN ''
                ELSE CONCAT( ':', "Wert" )
            END
        )
    )AS "Sonstiges"

    FROM "SonstigesEinfach"
    GROUP BY "Versuchsbedingung"

)"MiscView"
ON "VersuchsbedingungenEinfach"."ID"="MiscView"."Versuchsbedingung"

LEFT JOIN(

    SELECT

        "Versuchsbedingung",
        GROUP_CONCAT( "Zeit" )AS "Zeit",
        GROUP_CONCAT( "Konzentration" )AS "Konzentration"

    FROM "MesswerteEinfach"
    WHERE NOT( "Zeit" IS NULL OR "Konzentration" IS NULL )
    GROUP BY "Versuchsbedingung"

)"DataView"
ON "VersuchsbedingungenEinfach"."ID"="DataView"."Versuchsbedingung"

WHERE "Zeit" IS NOT NULL;







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







CREATE VIEW "EstPeiView" AS

SELECT
    "MicrobialDataView"."Versuchsbedingung",
    "MicrobialDataView"."CombaseID",
    "MicrobialDataView"."SonstigesID",
    "MicrobialDataView"."Sonstiges",
    "MicrobialDataView"."Temperatur",
    "MicrobialDataView"."pH",
    "MicrobialDataView"."aw",
    "MicrobialDataView"."Agens",
    "MicrobialDataView"."Agensname",
    "MicrobialDataView"."AgensDetail",
    "MicrobialDataView"."Zeit",
    "MicrobialDataView"."Konzentration",
    "MicrobialDataView"."Kommentar",
    "MicrobialDataView"."Literatur",
    "MicrobialDataView"."ReferenzText",
    "EstModelPrimView"."Formel",
    "EstModelPrimView"."Dependent",
    "EstModelPrimView"."Independent",
    "EstModelPrimView"."Parametername",
    "EstModelPrimView"."Wert",
    "EstModelPrimView"."Name",
    "EstModelPrimView"."Modell",
    "EstModelPrimView"."GeschaetztesModell",
    "EstModelPrimView"."RMS",
    "EstModelPrimView"."Rsquared",
    "EstModelPrimView"."AIC",
    "EstModelPrimView"."BIC",
    "EstModelPrimView"."min",
    "EstModelPrimView"."max",
    "EstModelPrimView"."minIndep",
    "EstModelPrimView"."maxIndep",
    "EstModelPrimView"."LitMID",
    "EstModelPrimView"."LitM",
    "EstModelPrimView"."LitEmID",
    "EstModelPrimView"."LitEm",
    "EstModelPrimView"."StandardError",
    "EstModelPrimView"."VarParMap"

FROM "MicrobialDataView"

RIGHT JOIN "EstModelPrimView"
ON "EstModelPrimView"."Versuchsbedingung"="MicrobialDataView"."Versuchsbedingung";







CREATE VIEW "EstSeiView" AS

SELECT *

FROM "EstPeiView"

JOIN "Sekundaermodelle_Primaermodelle"
ON "Sekundaermodelle_Primaermodelle"."GeschaetztesPrimaermodell"="EstPeiView"."GeschaetztesModell"

JOIN "EstModelSecView"
ON "Sekundaermodelle_Primaermodelle"."GeschaetztesSekundaermodell"="EstModelSecView"."GeschaetztesModell2"

ORDER BY "EstModelSecView"."GeschaetztesModell2" ASC;







CREATE VIEW "SelectModelView" AS

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
ON "Modellkatalog"."ID"="P"."Modell"





GRANT SELECT ON TABLE "SelectModelView" TO "PUBLIC";				
GRANT SELECT ON TABLE "EstSeiView" TO "PUBLIC";				
GRANT SELECT ON TABLE "EstPeiView" TO "PUBLIC";				
GRANT SELECT ON TABLE "EstModelSecView" TO "PUBLIC";				
GRANT SELECT ON TABLE "EstModelPrimView" TO "PUBLIC";				
GRANT SELECT ON TABLE "MicrobialDataView" TO "PUBLIC";				
GRANT SELECT ON TABLE "VersuchsbedingungenEinfach" TO "PUBLIC";				
GRANT SELECT ON TABLE "MesswerteEinfach" TO "PUBLIC";				
GRANT SELECT ON TABLE "DoubleKennzahlenEinfach" TO "PUBLIC";				
GRANT SELECT ON TABLE "SonstigesEinfach" TO "PUBLIC";				