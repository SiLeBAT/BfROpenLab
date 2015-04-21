DROP VIEW IF EXISTS "EstModelPrimView";


CREATE VIEW "EstModelPrimView" AS

SELECT
    "Formel",
    "Dependent",
    "Independent",
    "Parametername",
    "Wert",
    "ZeitEinheit",
    "KonzEinheit",
    "Name",
    "Modellkatalog"."ID" AS "Modell",
    "GeschaetzteModelle"."ID" AS "GeschaetztesModell",
    "RMS",
    "Rsquared",
    "AIC",
    "BIC",
    "GeschaetzteModelle"."Guetescore" AS "Guetescore",
    "GeschaetzteModelle"."Geprueft" AS "Geprueft",
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

LEFT JOIN "DepVarView"
ON "GeschaetzteModelle"."ID"="DepVarView"."GeschaetztesModell"

LEFT JOIN "IndepVarView"
ON "GeschaetzteModelle"."ID"="IndepVarView"."GeschaetztesModell"

LEFT JOIN "ParamView"
ON "GeschaetzteModelle"."ID"="ParamView"."GeschaetztesModell"

LEFT JOIN "LitMView"
ON "Modellkatalog"."ID"="LitMView"."Modell"

LEFT JOIN "LitEmView"
ON "GeschaetzteModelle"."ID"="LitEmView"."GeschaetztesModell"

LEFT JOIN "VarParMapView"
ON "VarParMapView"."GeschaetztesModell"="GeschaetzteModelle"."ID"

WHERE "Level"=1;


GRANT SELECT ON TABLE "EstModelPrimView" TO "PUBLIC";