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
        GROUP_CONCAT("ModellkatalogParameter"."Kategorie") AS "DepCategory",
        GROUP_CONCAT("ModellkatalogParameter"."Einheit") AS "DepUnit"

    FROM "Modellkatalog"

    LEFT JOIN "GeschaetzteModelle"
    ON "Modellkatalog"."ID"="GeschaetzteModelle"."Modell"

    LEFT JOIN "ModellkatalogParameter"
    ON "ModellkatalogParameter"."Modell"="Modellkatalog"."ID"

    LEFT JOIN "VarParMaps"
    ON "VarParMaps"."VarPar"="ModellkatalogParameter"."ID" AND "VarParMaps"."GeschaetztesModell"="GeschaetzteModelle"."ID"

    WHERE "Parametertyp"=3 AND "GeschaetzteModelle"."ID" IS NOT NULL
    GROUP BY "GeschaetzteModelle"."ID";


GRANT SELECT ON TABLE "DepVarView" TO "PUBLIC";				
