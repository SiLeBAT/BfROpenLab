DROP VIEW IF EXISTS "SeiView";
DROP VIEW IF EXISTS "PeiView";
DROP VIEW IF EXISTS "MicrobialDataView";
DROP VIEW IF EXISTS "VersuchsbedingungenEinfach";
DROP VIEW IF EXISTS "MesswerteEinfach";
DROP VIEW IF EXISTS "DoubleKennzahlenEinfach";

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

GRANT SELECT ON TABLE "DoubleKennzahlenEinfach" TO "PUBLIC";