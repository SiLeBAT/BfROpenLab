---
title: How to create a Workflow (Part 1)?
sidebar: fcl_sidebar
permalink: fcl_workflow_1.html
folder: fcl
---

<h2 class="tutorial-heading">Tasks</h2>

 * Import the Example XLS template to FoodChain-Lab.
 * You can download it from here: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/FCL_Example.xlsx)
 * Via the **Tracing** node assign weights of "1" to the supermarkets in Hamburg, Karlsruhe, Ingolstadt and Hanover to mark them as outbreak locations.
 * Use the **Tracing View** to look at the delivery network.

<h2 class="tutorial-heading">Step 1</h2>

 * Select **Food-Lab > Open DB Gui...** in the menu bar to open the database interface.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/1.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/1.png"/></a>

<h2 class="tutorial-heading">Step 2</h2>

 * If you get a message saying the internal database has been created, click **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/2.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/2.png"/></a>

<h2 class="tutorial-heading">Step 3</h2>

 * In the database interface click the **Table import** button in the upper left corner.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/3.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/3.png"/></a>

<h2 class="tutorial-heading">Step 4</h2>

 * Now a file dialog will pop up.
 * *.xlsx files in FoodChain-Lab format can be selected here.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/4.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/4.png"/></a>

<h2 class="tutorial-heading">Step 5</h2>

 * Download the example file from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/FCL_Example.xlsx).
 * Select the file in the dialog and press **Open**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/5.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/5.png"/></a>

<h2 class="tutorial-heading">Step 6</h2>

 * When the importing is finished you see a dialog with errors/warnings, that occurred in the import process.
 * No errors ocurred, so just press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/6.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/6.png"/></a>

<h2 class="tutorial-heading">Step 7</h2>

 * In the database interface, you can now look at the imported data and check the data for duplicates.
 * Close the dialog.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/7.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/7.png"/></a>

<h2 class="tutorial-heading">Step 8</h2>

 * Now we want to create a workflow, that uses the imported data.
 * Right click on **LOCAL** in the **KNIME Explorer** and select **New KNIME Workflow...**

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/8.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/8.png"/></a>

<h2 class="tutorial-heading">Step 9</h2>

 * In the dialog set the name of the workflow to "MyFirstWorkflow" and click **Finish**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/9.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/9.png"/></a>

<h2 class="tutorial-heading">Step 10</h2>

 * The created workflow will be shown in the editor in the center.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/10.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/10.png"/></a>

<h2 class="tutorial-heading">Step 11</h2>

 * Drag the **Supply Chain Reader** from the **Node Repository** to the workflow.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/11.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/11.png"/></a>

<h2 class="tutorial-heading">Step 12</h2>

 * We do not need to configure the **Supply Chain Reader**.
 * Right click on it and select **Execute**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/12.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/12.png"/></a>

<h2 class="tutorial-heading">Step 13</h2>

 * The **Supply Chain Reader** has now read all data from the internal database.
 * Select the **Supply Chain Reader** in the workflow (so that a rect is drawn around it) and double click on the **Tracing** node in the **Node Repository**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/13.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/13.png"/></a>

<h2 class="tutorial-heading">Step 14</h2>

 * The **Tracing** node should show up in the workflow and its three input ports should be automatically connected to the **Supply Chain Reader**.
 * Double click on the **Tracing** node to configure it.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/14.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/14.png"/></a>

<h2 class="tutorial-heading">Step 15</h2>

 * You will notice tabs for "Station/Delivery Properties".
 * "Weight", "Cross Contamination" and "Kill Contamination" can be set there. Based on these properties a "Score" is computed for each station/delivery.
 * Additionally you can set "Observed" stations/deliveries.
 * Select the **Station Properties** and **Weight**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/15.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/15.png"/></a>

<h2 class="tutorial-heading">Step 16</h2>

 * A table with all available stations will show up.
 * The weight can be set in the left column.
 * Since scrolling through all stations is very inefficient, we can filter out all desired stations.
 * Click on **Set Filter**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/16.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/16.png"/></a>

<h2 class="tutorial-heading">Step 17</h2>

 * In this dialog you can specify which stations should appear in the table.
 * Press the button in the red circle to change the value for **Property**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/17.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/17.png"/></a>

<h2 class="tutorial-heading">Step 18</h2>

 * Select "type of business".

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/18.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/18.png"/></a>

<h2 class="tutorial-heading">Step 19</h2>

 * We only want to specify weights for supermarkets, since that is where contaminated products were found.
 * Set **Value** to "Supermarket" and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/19.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/19.png"/></a>

<h2 class="tutorial-heading">Step 20</h2>

 * Now you only see supermarkets in the dialog.
 * Set a weight of "1" to the supermarkets in "Hamburg", "Karlsruhe", "Ingolstadt" und "Hanover" to indicate that contaminated products were found there.
 * Click **OK** to apply the settings and close the dialog.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/20.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/20.png"/></a>

<h2 class="tutorial-heading">Step 21</h2>

 * Right click on the **Tracing** node and select **Execute** to execute the node.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/21.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/21.png"/></a>

<h2 class="tutorial-heading">Step 22</h2>

 * Drag the **Tracing View** from the **Node Repository** to the workflow.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/22.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/22.png"/></a>

<h2 class="tutorial-heading">Step 23</h2>

 * Connect the output ports of the **Tracing** node to the first two input ports of the **Tracing View**.
 * Connect the third output port of the **Supply Chain Reader** to the third input port of the **Tracing View**.
 * Now you open the **Tracing View** and analyze the data. This will be shown in the second part of this tutorial.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/23.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_1/23.png"/></a>
