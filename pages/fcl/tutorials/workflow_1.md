---
title: Creating a Workflow in FoodChain-Lab 1
sidebar: fcl_sidebar
permalink: fcl_workflow_1.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Import the Example XLS template to FoodChain-Lab.
 * You can download it from here: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/FCL_Example.xlsx)
 * Via the **Tracing** node assign weights of "1" to the supermarkets in Hamburg, Karlsruhe, Ingolstadt and Hanover to mark them as outbreak locations.
 * Use the **Tracing View** to look at the delivery network.

{% include heading.html text="Step 1" %}

 * Select **Food-Lab > Open DB Gui...** in the menu bar to open the database interface.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/1.png" %}

{% include heading.html text="Step 2" %}

 * If you get a message saying the internal database has been created, click **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/2.png" %}

{% include heading.html text="Step 3" %}

 * In the database interface click the **Table import** button in the upper left corner.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/3.png" %}

{% include heading.html text="Step 4" %}

 * Now a file dialog will pop up.
 * *.xlsx files in FoodChain-Lab format can be selected here.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/4.png" %}

{% include heading.html text="Step 5" %}

 * Download the example file from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/FCL_Example.xlsx).
 * Select the file in the dialog and press **Open**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/5.png" %}

{% include heading.html text="Step 6" %}

 * When the importing is finished you see a dialog with errors/warnings, that occurred in the import process.
 * No errors ocurred, so just press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/6.png" %}

{% include heading.html text="Step 7" %}

 * In the database interface, you can now look at the imported data and check the data for duplicates.
 * Close the dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/7.png" %}

{% include heading.html text="Step 8" %}

 * Now we want to create a workflow, that uses the imported data.
 * Right click on **LOCAL** in the **KNIME Explorer** and select **New KNIME Workflow...**

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/8.png" %}

{% include heading.html text="Step 9" %}

 * In the dialog set the name of the workflow to "MyFirstWorkflow" and click **Finish**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/9.png" %}

{% include heading.html text="Step 10" %}

 * The created workflow will be shown in the editor in the center.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/10.png" %}

{% include heading.html text="Step 11" %}

 * Drag the **Supply Chain Reader** from the **Node Repository** to the workflow.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/11.png" %}

{% include heading.html text="Step 12" %}

 * We do not need to configure the **Supply Chain Reader**.
 * Right click on it and select **Execute**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/12.png" %}

{% include heading.html text="Step 13" %}

 * The **Supply Chain Reader** has now read all data from the internal database.
 * Select the **Supply Chain Reader** in the workflow (so that a rect is drawn around it) and double click on the **Tracing** node in the **Node Repository**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/13.png" %}

{% include heading.html text="Step 14" %}

 * The **Tracing** node should show up in the workflow and its three input ports should be automatically connected to the **Supply Chain Reader**.
 * Double click on the **Tracing** node to configure it.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/14.png" %}

{% include heading.html text="Step 15" %}

 * You will notice tabs for "Station/Delivery Properties".
 * "Weight", "Cross Contamination" and "Kill Contamination" can be set there. Based on these properties a "Score" is computed for each station/delivery.
 * Additionally you can set "Observed" stations/deliveries.
 * Select the **Station Properties** and **Weight**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/15.png" %}

{% include heading.html text="Step 16" %}

 * A table with all available stations will show up.
 * The weight can be set in the left column.
 * Since scrolling through all stations is very inefficient, we can filter out all desired stations.
 * Click on **Set Filter**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/16.png" %}

{% include heading.html text="Step 17" %}

 * In this dialog you can specify which stations should appear in the table.
 * Press the button in the red circle to change the value for **Property**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/17.png" %}

{% include heading.html text="Step 18" %}

 * Select "type of business".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/18.png" %}

{% include heading.html text="Step 19" %}

 * We only want to specify weights for supermarkets, since that is where contaminated products were found.
 * Set **Value** to "Supermarket" and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/19.png" %}

{% include heading.html text="Step 20" %}

 * Now you only see supermarkets in the dialog.
 * Set a weight of "1" to the supermarkets in "Hamburg", "Karlsruhe", "Ingolstadt" und "Hanover" to indicate that contaminated products were found there.
 * Click **OK** to apply the settings and close the dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/20.png" %}

{% include heading.html text="Step 21" %}

 * Right click on the **Tracing** node and select **Execute** to execute the node.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/21.png" %}

{% include heading.html text="Step 22" %}

 * Drag the **Tracing View** from the **Node Repository** to the workflow.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/22.png" %}

{% include heading.html text="Step 23" %}

 * Connect the output ports of the **Tracing** node to the first two input ports of the **Tracing View**.
 * Connect the third output port of the **Supply Chain Reader** to the third input port of the **Tracing View**.
 * Now you open the **Tracing View** and analyze the data. This will be shown in the second part of this tutorial.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/23.png" %}

