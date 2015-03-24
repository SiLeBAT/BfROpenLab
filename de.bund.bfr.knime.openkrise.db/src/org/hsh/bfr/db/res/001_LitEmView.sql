DROP VIEW IF EXISTS "LitEmView";


CREATE VIEW "LitEmView" AS

    SELECT
        "GeschaetztesModell",
        GROUP_CONCAT( CONCAT( "Erstautor", '_', "Jahr" ) )AS "LitEm",
        GROUP_CONCAT( "Literatur"."ID" )AS "LitEmID"

    FROM "GeschaetztesModell_Referenz"

    JOIN "Literatur"
    ON "GeschaetztesModell_Referenz"."Literatur"="Literatur"."ID"

    GROUP BY "GeschaetztesModell";


GRANT SELECT ON TABLE "LitEmView" TO "PUBLIC";				
