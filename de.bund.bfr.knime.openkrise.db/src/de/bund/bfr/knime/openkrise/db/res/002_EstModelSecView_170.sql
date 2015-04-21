DROP VIEW IF EXISTS "EstModelSecView";

CREATE VIEW "EstModelSecView" AS

SELECT
    "GeschaetzteModelle"."Name" AS "FittedModelName2",
    "Formel" AS "Formel2",
    "Dependent" AS "Dependent2",
    "Independent" AS "Independent2",
    "Parametername" AS "Parametername2",
    "Wert" AS "Wert2",
    "ZeitEinheit" AS "ZeitEinheit2",
    "ParamView"."Einheiten" AS "Einheiten2",
    "ParamView"."EinheitenOT" AS "EinheitenOT2",
    "Name" AS "Name2",
    "Modellkatalog"."ID" AS "Modell2",
    "GeschaetzteModelle"."ID" AS "GeschaetztesModell2",
    "RMS" AS "RMS2",
    "Rsquared" AS "Rsquared2",
    "AIC" AS "AIC2",
    "BIC" AS "BIC2",
    "GeschaetzteModelle"."Guetescore" AS "Guetescore2",
    "GeschaetzteModelle"."Geprueft" AS "Geprueft2",
    "IndepVarView"."Einheiten" AS "iEinheiten2",
    "min" AS "min2",
    "max" AS "max2",
    "DepCategory" AS "DepCategory2",
    "DepUnit" AS "DepUnit2",
    "DepDescription" AS "DepDescription2",
    "IndepCategory" AS "IndepCategory2",
    "IndepUnit" AS "IndepUnit2",
    "IndepDescription" AS "IndepDescription2",
    "ParCategory" AS "ParCategory2",
    "ParUnit" AS "ParUnit2",
    "ParamDescription" AS "ParamDescription2",
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

WHERE "Level"=2;

GRANT SELECT ON TABLE "EstModelSecView" TO "PUBLIC";