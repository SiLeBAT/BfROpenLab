---
title: How to create a Workflow (Part 2)?
sidebar: fcl_sidebar
permalink: fcl_workflow_2.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Use the **Tracing View** to display the delivery graph in a clearly arranged way.
 * Apply the **Default Highlighting** to color the graph.
 * Visualize the backward and forward trace for an arbitrary station.

{% include heading.html text="Step 1" %}

 * This is the second part of the tutorial.
 * You can either do the first part to create this workflow or download it from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/MyFirstWorkflow.zip).
 * Double click on the **Tracing View** to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/1.png" %}

{% include heading.html text="Step 2" %}

 * In the **Tracing View** you can see the imported delivery network.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/2.png" %}

{% include heading.html text="Step 3" %}

 * To arrange the network in a better way right click in the graph and select **Apply Layout > Fruchterman-Reingold**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/3.png" %}

{% include heading.html text="Step 4" %}

 * This layout process is not deterministic. That means you will get a different result each time.
 * You can apply the layout again, if you are not satisfied with the current result.
 * You can also apply a layout for certain stations only. Therefore you have to select the stations you want to be layouted and apply the layout again.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/4.png" %}

{% include heading.html text="Step 5" %}

 * Right click in the graph to open the context menu and select **Set default Highlighting**.
 * Highlighting uses colors and sizes to visualize certain properties of stations/deliveries.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/5.png" %}

{% include heading.html text="Step 6" %}

 * You will notice, that 4 stations are colored red now and some stations increased in size.
 * The red stations are the supermarkets, where we set the weight to "1".
 * The size of each station is based on its "Score".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/6.png" %}

{% include heading.html text="Step 7" %}

 * Activate **Show Legend** to get a legend for the defined colors.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/7.png" %}

{% include heading.html text="Step 8" %}

 * Now we can observe a station to see its delivery trace.
 * Set "Picking" as **Editing Mode** and double click on any station.
 * We clicked on the station in the red circle.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/8.png" %}

{% include heading.html text="Step 9" %}

 * A dialog will pop up, that shows all attributes of the station.
 * Additionally you can change "Weight", "Cross Contamination", "Kill Contamination" and "Observed".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/9.png" %}

{% include heading.html text="Step 10" %}

 * Select **Observed** and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/10.png" %}

{% include heading.html text="Step 11" %}

 * All stations/deliveries of the forward trace are orange-colored and the ones of the backward trace are purple.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/11.png" %}

{% include heading.html text="Step 12" %}

 * Click at an empty area of the graph to deselect all stations.
 * Now you can see, that the "Observed" station is green.
 * Then activate "Join Deliveries" to simplify the graph. Deliveries with the same supplier and recipient are joined now.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_workflow_2/12.png" %}
