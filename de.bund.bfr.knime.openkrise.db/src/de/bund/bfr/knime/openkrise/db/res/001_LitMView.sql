DROP VIEW IF EXISTS "LitMView";


CREATE VIEW "LitMView" AS

    SELECT
        "Modell",
        GROUP_CONCAT( CONCAT( "Erstautor", '_', "Jahr" ) )AS "LitM",
        GROUP_CONCAT( "Literatur"."ID" )AS "LitMID"
    FROM "Modell_Referenz"
    JOIN "Literatur"
    ON "Modell_Referenz"."Literatur"="Literatur"."ID"

    GROUP BY "Modell";


GRANT SELECT ON TABLE "LitMView" TO "PUBLIC";				
