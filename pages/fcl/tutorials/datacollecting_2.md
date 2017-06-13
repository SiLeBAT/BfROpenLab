---
title: Importing data into FoodChain-Lab 2
sidebar: fcl_sidebar
permalink: fcl_datacollecting_2.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * In this part of the tutorial we will do a back- and forward-tracing from the "Frozen Fruit Sales" station.
 * You can either complete part 1 of the tutorial or directly import the following files into an empty database before starting this tutorial.
 * Caterer 1 Start: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/Start_Tracing_Forward_Caterer1.xlsx)
 * Caterer 2 Start: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/Start_Tracing_Forward_Caterer2.xlsx)
 * Caterer 1 Backward: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/StationBacktrace_request_Caterer 1_-1839649003.xlsx)
 * Caterer 2 Backward: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/StationBacktrace_request_Caterer 2_-530502898.xlsx)

{% include heading.html text="Step 1" %}

 * You should have the database interface open.
 * Press the button for generating a backtracing template for one station, which is marked by the red circle.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/1.png" %}

{% include heading.html text="Step 2" %}

 * In the dialog that appears, you can see a table with all stations from the database.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/2.png" %}

{% include heading.html text="Step 3" %}

 * Type the first letters of "Frozen Fruit Sales" in the search box to search for it.
 * After typing some letters all other stations should have disappeared. Click on the **Select** button for "Frozen Fruit Sales".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/3.png" %}

{% include heading.html text="Step 4" %}

 * In the file dialog that appears, you can specify the folder where the generated template should be saved.
 * Select the desired folder and press **Save**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/4.png" %}

{% include heading.html text="Step 5" %}

 * You'll be noticed, that one template was generated in the folder you specified.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/5.png" %}

{% include heading.html text="Step 6" %}

 * Open the generated template "StationBacktrace_request_Frozen Fruit Sales_725897958...".
 * In the upper part of the sheet you can see the three outgoing deliveries from to "Caterer 1" and "Caterer 2".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/6.png" %}

{% include heading.html text="Step 7" %}

 * Now scroll down to the section where you can enter the ingredients.
 * Enter the 3 deliveries, that were used as ingredients for lot "108" (marked by the red box).
 * Save and close the document.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/7.png" %}

{% include heading.html text="Step 8" %}

 * To import this file click on the **Table import** button in the upper left corner of the database interface.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/8.png" %}

{% include heading.html text="Step 9" %}

 * In the file dialog that appears, select "StationBacktrace_request_Frozen Fruit Sales_725897958..." and press **Open**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/9.png" %}

{% include heading.html text="Step 10" %}

 * You'll see a message that the import was successful.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/10.png" %}

{% include heading.html text="Step 11" %}

 * The frozen strawberries from lot "108" were not only delivered to "Caterer 1" and "Caterer 2", but also to a third caterer.
 * To get this information into the database we have to create a forward tracing template for "Frozen Fruit Sales".
 * First we have to uncheck **Generate only the missing data**. Otherwise no template would be generated, since FoodChain-Lab assumes that no data is missing for lot "108" as its ingredients and the deliveries of the lot to "Caterer 1" and "Caterer 2" are already in the database.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/11.png" %}

{% include heading.html text="Step 12" %}

 * Press the button for generating a forward tracing template for one station, which is marked by the red circle.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/12.png" %}

{% include heading.html text="Step 13" %}

 * In the dialog that appears search for "Frozen Fruit Sales" and click its **Select** button.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/13.png" %}

{% include heading.html text="Step 14" %}

 * In the file dialog that appears, you can specify the folder where the generated templates should be saved.
 * Select the desired folder and press **Save**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/14.png" %}

{% include heading.html text="Step 15" %}

 * You'll be noticed, that one template was generated in the folder you specified.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/15.png" %}

{% include heading.html text="Step 16" %}

 * Open the generated template ("StationFwdtrace_request_Frozen Fruit Sales...").
 * In the upper part you can see all incoming deliveries (ingredients) of "Frozen Fruit Sales".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/16.png" %}

{% include heading.html text="Step 17" %}

 * Scroll down to the section where you can enter the outgoing deliveries.
 * There was one delivery of "Frozen Strawberries" of lot "108" to "Caterer 3".
 * Since lot "108" already exists, we do not have to define any ingredients and can leave the first cell empty.
 * Enter the delivery as shown on the screenshot (red box).
 * After entering the data save and close the document.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/17.png" %}

{% include heading.html text="Step 18" %}

 * To import this file click on the **Table import** button in the upper left corner of the database interface.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/18.png" %}

{% include heading.html text="Step 19" %}

 * In the file dialog that appears, select "StationFwdtrace_request_Frozen Fruit Sales..." and press **Open**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/19.png" %}

{% include heading.html text="Step 20" %}

 * You'll see a message that the import was successful.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/20.png" %}

