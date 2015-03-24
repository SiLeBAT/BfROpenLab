DROP VIEW IF EXISTS "DepVarView";


CREATE VIEW "DepVarView" AS

    SELECT
	  "GeschaetzteModelle"."ID" AS "GeschaetztesModell",
        GROUP_CONCAT(
            CASE
                WHEN "VarParMap" IS NOT NULL
                THEN "VarParMap"
                ELSE "Parametername"
            END
        )AS "Dependent",
        ARRAY_AGG("ModellkatalogParameter"."Beschreibung") AS "DepDescription",
        GROUP_CONCAT("Einheiten"."object type") AS "DepOT",
        GROUP_CONCAT("Einheiten"."kind of property / quantity") AS "DepCategory",
        GROUP_CONCAT("Einheiten"."display in GUI as") AS "DepUnit"

    FROM "Modellkatalog"

    LEFT JOIN "GeschaetzteModelle"
    ON "Modellkatalog"."ID"="GeschaetzteModelle"."Modell"

    LEFT JOIN "ModellkatalogParameter"
    ON "ModellkatalogParameter"."Modell"="Modellkatalog"."ID"

	LEFT JOIN "Einheiten"
	ON "ModellkatalogParameter"."Einheit"="Einheiten"."ID"

    LEFT JOIN "VarParMaps"
    ON "VarParMaps"."VarPar"="ModellkatalogParameter"."ID" AND "VarParMaps"."GeschaetztesModell"="GeschaetzteModelle"."ID"

    WHERE "Parametertyp"=3 AND "GeschaetzteModelle"."ID" IS NOT NULL
    GROUP BY "GeschaetzteModelle"."ID";


GRANT SELECT ON TABLE "DepVarView" TO "PUBLIC";				
