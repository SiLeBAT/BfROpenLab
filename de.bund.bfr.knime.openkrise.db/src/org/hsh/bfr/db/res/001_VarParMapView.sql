DROP VIEW IF EXISTS "VarParMapView";


CREATE VIEW "VarParMapView" AS

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

    GROUP BY "VarParMaps"."GeschaetztesModell";


GRANT SELECT ON TABLE "VarParMapView" TO "PUBLIC";				
