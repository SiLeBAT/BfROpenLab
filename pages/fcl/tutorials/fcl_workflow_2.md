---
title: How to create a Workflow (Part 2)?
sidebar: fcl_sidebar
permalink: fcl_workflow_2.html
folder: fcl
---

<h2 class="tutorial-heading">Tasks</h2>

 * Use the **Tracing View** to display the delivery graph in a clearly arranged way.
 * Apply the **Default Highlighting** to color the graph.
 * Visualize the backward and forward trace for an arbitrary station.

<h2 class="tutorial-heading">Step 1</h2>

 * This is the second part of the tutorial.
 * You can either do the first part to create this workflow or download it from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/MyFirstWorkflow.zip).
 * Double click on the **Tracing View** to open its dialog.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/1.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/1.png"/></a>

<h2 class="tutorial-heading">Step 2</h2>

 * In the **Tracing View** you can see the imported delivery network.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/2.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/2.png"/></a>

<h2 class="tutorial-heading">Step 3</h2>

 * To arrange the network in a better way right click in the graph and select **Apply Layout > Fruchterman-Reingold**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/3.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/3.png"/></a>

<h2 class="tutorial-heading">Step 4</h2>

 * This layout process is not deterministic. That means you will get a different result each time.
 * You can apply the layout again, if you are not satisfied with the current result.
 * You can also apply a layout for certain stations only. Therefore you have to select the stations you want to be layouted and apply the layout again.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/4.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/4.png"/></a>

<h2 class="tutorial-heading">Step 5</h2>

 * Right click in the graph to open the context menu and select **Set default Highlighting**.
 * Highlighting uses colors and sizes to visualize certain properties of stations/deliveries.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/5.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/5.png"/></a>

<h2 class="tutorial-heading">Step 6</h2>

 * You will notice, that 4 stations are colored red now and some stations increased in size.
 * The red stations are the supermarkets, where we set the weight to "1".
 * The size of each station is based on its "Score".

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/6.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/6.png"/></a>

<h2 class="tutorial-heading">Step 7</h2>

 * Activate **Show Legend** to get a legend for the defined colors.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/7.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/7.png"/></a>

<h2 class="tutorial-heading">Step 8</h2>

 * Now we can observe a station to see its delivery trace.
 * Set "Picking" as **Editing Mode** and double click on any station.
 * We clicked on the station in the red circle.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/8.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/8.png"/></a>

<h2 class="tutorial-heading">Step 9</h2>

 * A dialog will pop up, that shows all attributes of the station.
 * Additionally you can change "Weight", "Cross Contamination", "Kill Contamination" and "Observed".

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/9.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/9.png"/></a>

<h2 class="tutorial-heading">Step 10</h2>

 * Select **Observed** and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/10.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/10.png"/></a>

<h2 class="tutorial-heading">Step 11</h2>

 * All stations/deliveries of the forward trace are orange-colored and the ones of the backward trace are purple.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/11.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/11.png"/></a>

<h2 class="tutorial-heading">Step 12</h2>

 * Click at an empty area of the graph to deselect all stations.
 * Now you can see, that the "Observed" station is green.
 * Then activate "Join Deliveries" to simplify the graph. Deliveries with the same supplier and recipient are joined now.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/12.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/12.png"/></a>
