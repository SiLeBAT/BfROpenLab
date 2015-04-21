DROP VIEW IF EXISTS "ParamView";

CREATE VIEW "ParamView" AS

SELECT
    "GeschaetztesModell",
    ARRAY_AGG( "Parametername" )AS "Parametername",
    ARRAY_AGG( "Wert" )AS "Wert",
    ARRAY_AGG( "ZeitEinheit" )AS "ZeitEinheit",
    ARRAY_AGG( "Einheiten" )AS "Einheiten",
    ARRAY_AGG( "min" )AS "min",
    ARRAY_AGG( "max" )AS "max",
    ARRAY_AGG( "StandardError" )AS "StandardError",
        ARRAY_AGG("ParCategory") AS "ParCategory",
        ARRAY_AGG("ParUnit") AS "ParUnit"

FROM(

    SELECT

        "GeschaetztesModell",
        "Parametername",
        AVG( "Wert" )AS "Wert",
        "ZeitEinheit",
        "Einheiten",
        MIN( "min" )AS "min",
        MAX( "max" )AS "max",
        AVG( "StandardError" )AS "StandardError",
        "Kategorie" AS "ParCategory",
        "Einheit" AS "ParUnit"

    FROM(

        SELECT

            "GeschaetzteModelle"."ID" AS "GeschaetztesModell",
        
            CASE
                WHEN "VarParMap" IS NOT NULL
                THEN "VarParMap"
                ELSE "Parametername"
            END
            AS "Parametername",
            "Wert",
            "ZeitEinheit",
            "Einheiten"."Einheit" AS "Einheiten",
            "min",
            "max",
        	"ModellkatalogParameter"."Kategorie" AS "Kategorie",
        	"ModellkatalogParameter"."Einheit" AS "Einheit",
            "StandardError"

        FROM "GeschaetzteModelle"

        LEFT JOIN "ModellkatalogParameter"
        ON "ModellkatalogParameter"."Modell"="GeschaetzteModelle"."Modell"

        LEFT JOIN "GeschaetzteParameter"
        ON "GeschaetzteParameter"."GeschaetztesModell"="GeschaetzteModelle"."ID" AND "GeschaetzteParameter"."Parameter"="ModellkatalogParameter"."ID"

        LEFT JOIN "Einheiten"
        ON "GeschaetzteParameter"."Einheit"="Einheiten"."ID"

        LEFT JOIN "VarParMaps"
        ON "VarParMaps"."VarPar"="ModellkatalogParameter"."ID" AND "VarParMaps"."GeschaetztesModell"="GeschaetzteModelle"."ID"
 
        WHERE "Parametertyp"=2 AND "GeschaetzteModelle"."ID" IS NOT NULL
    )

    GROUP BY "GeschaetztesModell", "Parametername", "ZeitEinheit", "Einheiten", "Kategorie", "Einheit"

)

GROUP BY "GeschaetztesModell";

GRANT SELECT ON TABLE "ParamView" TO "PUBLIC";