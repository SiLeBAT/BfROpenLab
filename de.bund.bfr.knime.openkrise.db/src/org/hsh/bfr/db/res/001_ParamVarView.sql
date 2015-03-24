DROP VIEW IF EXISTS "ParamView";

CREATE VIEW "ParamView" AS

SELECT
    "GeschaetztesModell",
    ARRAY_AGG( "Parametername" )AS "Parametername",
    ARRAY_AGG( "Wert" )AS "Wert",
    ARRAY_AGG( "ZeitEinheit" )AS "ZeitEinheit",
    ARRAY_AGG( "Konz_Einheit" )AS "KonzEinheit",
    ARRAY_AGG( "min" )AS "min",
    ARRAY_AGG( "max" )AS "max",
    ARRAY_AGG( "StandardError" )AS "StandardError"

FROM(

    SELECT

        "GeschaetztesModell",
        "Parametername",
        AVG( "Wert" )AS "Wert",
        "ZeitEinheit",
        "Konz_Einheit",
        MIN( "min" )AS "min",
        MAX( "max" )AS "max",
        AVG( "StandardError" )AS "StandardError"

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
            "Konz_Einheit",
            "min",
            "max",
            "StandardError"

        FROM "GeschaetzteModelle"

        LEFT JOIN "ModellkatalogParameter"
        ON "ModellkatalogParameter"."Modell"="GeschaetzteModelle"."Modell"

        LEFT JOIN "GeschaetzteParameter"
        ON "GeschaetzteParameter"."GeschaetztesModell"="GeschaetzteModelle"."ID" AND "GeschaetzteParameter"."Parameter"="ModellkatalogParameter"."ID"

        LEFT JOIN "VarParMaps"
        ON "VarParMaps"."VarPar"="ModellkatalogParameter"."ID" AND "VarParMaps"."GeschaetztesModell"="GeschaetzteModelle"."ID"
 
        WHERE "Parametertyp"=2 AND "GeschaetzteModelle"."ID" IS NOT NULL
    )

    GROUP BY "GeschaetztesModell", "Parametername", "ZeitEinheit", "Konz_Einheit"

)

GROUP BY "GeschaetztesModell";

GRANT SELECT ON TABLE "ParamView" TO "PUBLIC";