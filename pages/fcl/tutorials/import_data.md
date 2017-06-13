---
title: Import Data from External Sources into FoodChain-Lab
sidebar: fcl_sidebar
permalink: fcl_import_data.html
folder: fcl
---

{% include heading.html text="Content" %}

 * This tutorial shows how data can imported into FoodChain-Lab without using the Excel templates.
 * Therefore the external data has to read into KNIME tables.
 * KNIME provides a multitude of reader nodes for various data formats (e.g. csv, SQL databases, ...).
 * Here we just show which tables are needed by FoodChain-Lab and which columns they must contain. To do so the we have created these tables with **Table Creator** nodes.

{% include heading.html text="Step 1" %}

 * Import the workflow from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Import.knwf).

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/1.png" %}

{% include heading.html text="Step 2" %}

 * FoodChain-Lab needs three tables to perform a tracing analysis: **Stations**, **Deliveries** and **Delivery Relations**.
 * The **Table Creator** nodes on the left show all mandatory columns for these three tables.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/2.png" %}

{% include heading.html text="Step 3" %}

 * Double click on the **Table Creator** for the **Stations** table to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/3.png" %}

{% include heading.html text="Step 4" %}

 * As you can see the only mandatory column in the **Stations** table is the column **ID** of type string.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/4.png" %}

{% include heading.html text="Step 5" %}

 * Double click on the **Table Creator** for the **Deliveries** table to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/5.png" %}

{% include heading.html text="Step 6" %}

 * The **Deliveries** table has three mandatory columns: **ID**, **from** and **to** (all of type string)
 * The **from** and **to** columns contain the source station and target station **IDs**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/6.png" %}

{% include heading.html text="Step 7" %}

 * Double click on the **Table Creator** for the **Delivery Relations** table to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/7.png" %}

{% include heading.html text="Step 8" %}

 * The **Delivery Relations** table has the columns **from** and **to** of type string.
 * These columns contain **IDs** from the **Delivery** table and are meant to connect two deliveries.
 * The **from**-delivery is a part/ingredient of the **to**-delivery. A contamination can spread from "**from**" to "**to**".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/8.png" %}

{% include heading.html text="Step 9" %}

 * Now we will look the **Table Creator** nodes on the right which show the optional columns for the three tables.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/9.png" %}

{% include heading.html text="Step 10" %}

 * Double click on the **Table Creator** for the **Stations** table to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/10.png" %}

{% include heading.html text="Step 11" %}

 * The optional columns for the **Stations** table are **Address**, **Country**, **Latitude** and **Longitude**.
 * **Latitude** and **Longitude** are used for the geographical visualization in the **Tracing View** and **Address** and **Country** can be used for geocoding, if **Latitude** and **Longitude** are not known yet.
 * Any other arbitrary columns of this table can also be used in the **Tracing View** for highlighting etc.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/11.png" %}

{% include heading.html text="Step 12" %}

 * The **Geocoding** node in the upper right corner shows how **Address** and **Country** can be used for geocoding to compute **Latitude** and **Longitude**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/12.png" %}

{% include heading.html text="Step 13" %}

 * Double click on the **Table Creator** for the **Deliveries** table to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/13.png" %}

{% include heading.html text="Step 14" %}

 * The optional columns are **Date Delivery**, **Date Delivery Arrival**, **Lot ID**, **Amount** and **Amount Unit**.
 * The date columns are of type string and in YYYY-MM-DD format. They used for computing cross contamination.
 * If the **Lot ID** column (of type string) is provided, lot-based scores are computed.
 * The amount columns are just used for plausibility checks.
 * Any other arbitrary columns of this table can also be used in the **Tracing View** for highlighting etc.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/14.png" %}

{% include heading.html text="Step 15" %}

 * Double click on the **Table Creator** for the **Deliveries Relations** table to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/15.png" %}

{% include heading.html text="Step 16" %}

 * The **Delivery Relations** table does not have any optional columns.
 * All columns other than **from** and **to** are ignored.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_import_data/16.png" %}

