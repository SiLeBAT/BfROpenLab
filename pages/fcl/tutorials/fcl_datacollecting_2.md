---
title: Data Collection (Part 2)
sidebar: fcl_sidebar
permalink: fcl_datacollecting_2.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * In this part of the tutorial we will do a back- and forward-tracing from the "Frozen Fruit Sales" station.
 * You can either complete part 1 of the tutorial or directly import the following files into an empty database before starting this tutorial.
 * Start Template: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/Start_Tracing_Caterers.xlsx)
 * Caterer 1 Template: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/Backtrace_request_Caterer 1.xlsx)
 * Caterer 2 Template: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/Backtrace_request_Caterer 2.xlsx)

{% include heading.html text="Step 1" %}

 * You should have the database interface open.
 * Press the button for generating backtracing templates, which is marked by the red circle.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/1.png" %}

{% include heading.html text="Step 2" %}

 * Since we want to do the backtracing for the supplier "Frozen Fruit Sales", select **Supplier** only and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/2.png" %}

{% include heading.html text="Step 3" %}

 * In the file dialog that appears, you can specify the folder where the generated templates should be saved.
 * Select the desired folder and press **Save**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/3.png" %}

{% include heading.html text="Step 4" %}

 * You'll be noticed, that one template was generated in the folder you specified.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/4.png" %}

{% include heading.html text="Step 5" %}

 * Open the generated template "Backtrace_request_Frozen Fruit Sales.xlsx".
 * Add the stations from the screenshot to the **Stations** sheet.
 * These station include 3 strawberry suppliers that delivered strawberries to "Frozen Fruit Sales" and also a caterer that received strawberries.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/5.png" %}

{% include heading.html text="Step 6" %}

 * In the **BackTracing** sheet you can see the three outgoing deliveries of "Frozen Fruit Sales".
 * These deliveries belong to lots "108" and "139".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/6.png" %}

{% include heading.html text="Step 7" %}

 * Now scroll to the **Ingredients for Lot(s)** section.
 * Enter the 3 deliveries, that were used as ingredients for lot "108", from the screenshot.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/7.png" %}

{% include heading.html text="Step 8" %}

 * Lot "108" was not only delivered to "Caterer 1" and "Caterer 2", but also to a third caterer.
 * We can add this information in the **ForwardTracing_Opt** sheet.
 * Enter the delivery to "Caterer 3" from the screenshot.
 * Save the completed document ("Backtrace_request_Frozen Fruit Sales.xlsx").

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/8.png" %}

{% include heading.html text="Step 9" %}

 * To import this file click on the **Table import** button in the upper left corner of the database interface.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/9.png" %}

{% include heading.html text="Step 10" %}

 * In the file dialog that appears, select "Backtrace_request_Frozen Fruit Sales.xlsx" and press **Open**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/10.png" %}

{% include heading.html text="Step 11" %}

 * You'll be notified, that some warnings occurred during import.
 * Press **Show Details** to have at look at the warnings.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/11.png" %}

{% include heading.html text="Step 12" %}

 * 730kg of strawberries went into lot "108", but all the deliveries of frozen strawberries from lot "108" just add up to 99.75kg.
 * Warnings like these are supposed to help finding errors in the data.
 * In this tutorial we just ignore the warning. So press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/12.png" %}

{% include heading.html text="Step 13" %}

 * We now know that lot "108" was also delivered to "Caterer 3" and we have imported this delivery into the database.
 * Let's do a forward tracing from "Caterer 3".
 * Press the button for generating the forward template for a specific station marked by the red circle.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/13.png" %}

{% include heading.html text="Step 14" %}

 * In the dialog that appears, you must select the station for which the template should be generated.
 * Enter "cate" in the field after **Enter Search Query** and you will see that the table below only shows the caterers now.
 * Press the **Select** button for "Caterer 3".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/14.png" %}

{% include heading.html text="Step 15" %}

 * In the file dialog that appears, you can specify the folder where the generated templates should be saved.
 * Select the desired folder and press **Save**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/15.png" %}

{% include heading.html text="Step 16" %}

 * You'll be noticed, that one template was generated in the folder you specified.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/16.png" %}

{% include heading.html text="Step 17" %}

 * Open "StationFwdtrace_request_Caterer 3.xlsx".
 * Enter the station from the screenshot in the **Stations** sheet.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/17.png" %}

{% include heading.html text="Step 18" %}

 * In the **FwdTracing** sheet you first have to enter all relevant outgoing lots of "Caterer 3". So add lot "C3M1" to the **Lot Information** section as shown on the screenshot (red rectangle).
 * Now you can specify, that the delivery of "Frozen Strawberries" was used as an ingredient in this lot (red circle).

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/18.png" %}

{% include heading.html text="Step 19" %}

 * Scroll down to the **Products Out** section to specify the outgoing deliveries from lot "C3M1".
 * Add the deliveries as shown on the screenshot.
 * Save the completed document ("StationFwdtrace_request_Caterer 3.xlsx").

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/19.png" %}

{% include heading.html text="Step 20" %}

 * To import this file click on the **Table import** button in the upper left corner of the database interface.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/20.png" %}

{% include heading.html text="Step 21" %}

 * In the file dialog that appears, select "StationFwdtrace_request_Caterer 3.xlsx" and press **Open**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/21.png" %}

{% include heading.html text="Step 22" %}

 * You'll see a message that the import was successful.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/22.png" %}
