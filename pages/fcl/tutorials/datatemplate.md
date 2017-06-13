---
title: Importing data into FoodChain-Lab with All-in-one template
sidebar: fcl_sidebar
permalink: fcl_datatemplate.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * In this tutorial we'll show you how to import delivery data to FoodChain-Lab via our All-in-one Excel template.
 * All data will be entered in one file.
 * The All-in-one template can be download from here: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/templates/All_In_One_Template.xlsx)
 * If you do not want to fill the template yourself, you can download the filled out result of this tutorial from here: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/All_In_One_Template_Example.xlsx)

{% include heading.html text="Step 1" %}

 * Open "All_In_One_Template.xlsx" and select the **Stations** sheet.
 * Here you must enter all stations of the delivery network.
 * The **Company_ID** column is mandatory, all other columns are optional.
 * Next to **Additional Fields** you can specify your own columns, for which you would like to enter data.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/1.png" %}

{% include heading.html text="Step 2" %}

 * Now select the **Deliveries** sheet.
 * Here you must enter all deliveries between the stations defined in the previous sheet.
 * The columns with the red headers are mandatory. In **Station** and **Recipient** you must enter **Company_IDs** from the previous sheet.
 * Next to **Additional Fields** you can specify your own columns, for which you would like to enter data.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/2.png" %}

{% include heading.html text="Step 3" %}

 * Now select the **Deliveries2Deliveries** sheet.
 * Here you can connect two deliveries from the previous sheet. In both columns you must enter **DeliveryIDs**.
 * The **From**-delivery is a part/ingredient of the **To**-delivery. A contamination can spread from "**From**" to "**To**".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/3.png" %}

{% include heading.html text="Step 4" %}

 * Switch back to the **Stations** sheet and enter the data from the screenshot.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/4.png" %}

{% include heading.html text="Step 5" %}

 * Switch to the **Deliveries** sheet and enter the data from the screenshot.
 * The "Yogurt Factory" produces two lots of yogurt. Milk from delivery "L117_1" is used for the first lot ("LY1") and milk from deliveries "L117_2" and "L14_1" is used for the second lot ("LY2"). This ingredient information will be entered in the next sheet.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/5.png" %}

{% include heading.html text="Step 6" %}

 * In this sheet you can only reference deliveries and not lots.
 * Therefore we have to connect "L117_1" to both deliveries of lot "LY1" ("LY1_1" and "LY1_2").
 * And "L117_2" and "L14_1" have to be connected to both deliveries of lot "LY2" ("LY2_1" and "LY2_2").

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/6.png" %}

{% include heading.html text="Step 7" %}

 * After entering all data in the template, please open KNIME now.
 * Then select **Food-Lab > Open DB Gui...** in the menu bar to open the database interface.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/7.png" %}

{% include heading.html text="Step 8" %}

 * To import this file click on the **Table import** button in the upper left corner of the database interface.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/8.png" %}

{% include heading.html text="Step 9" %}

 * In the file dialog that appears, select "All_In_One_Template.xlsx" and press **Open**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/9.png" %}

{% include heading.html text="Step 10" %}

 * You'll see a message that the import was successful.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/10.png" %}

{% include heading.html text="Step 11" %}

 * In the database interface you'll notice, that there is now data in the tables.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datatemplate/11.png" %}

