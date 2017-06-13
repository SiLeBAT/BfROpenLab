---
title: Highlighting in FoodChain-Lab
sidebar: fcl_sidebar
permalink: fcl_highlighting.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Open the following workflow: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/Small_Example.zip).
 * Change the color of **Outbreak** stations in the **Tracing View** from red to turquoise.
 * Add the label "node" to each station.

{% include heading.html text="Step 1" %}

 * Import the Small Example Workflow from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/Small_Example.zip).
 * Open the **Tracing View** by double-clicking on it.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/1.png" %}

{% include heading.html text="Step 2" %}

 * Here you can see a small delivery graph with three outbreak locations (red stations) in the lower part.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/2.png" %}

{% include heading.html text="Step 3" %}

 * Let's now change the color of the outbreak stations.
 * Right click in the graph and select **Station Highlighting > Edit**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/3.png" %}

{% include heading.html text="Step 4" %}

 * A dialog with all defined highlighting conditions will pop up.
 * Double click on the **Outbreak** condition to edit it.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/4.png" %}

{% include heading.html text="Step 5" %}

 * A new dialog for editing the outbreak condition will pop up.
 * In this dialog you can also change what stations the highlighting should be applied to.
 * Currently it is applied on all station with "Weight" > 0. You can add other expressions by pressing the **Add** button on the right. These expressions can be combined via "And" or "Or".
 * To edit the color click red square next to **Color**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/5.png" %}

{% include heading.html text="Step 6" %}

 * In the color chooser dialog select turquoise and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/6.png" %}

{% include heading.html text="Step 7" %}

 * The square next to **Color** should be turquoise now.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/7.png" %}

{% include heading.html text="Step 8" %}

 * In the dialog showing all highlighting conditions press **OK** to apply your changes.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/8.png" %}

{% include heading.html text="Step 9" %}

 * The outbreak stations in the delivery graph should now be turquoise.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/9.png" %}

{% include heading.html text="Step 10" %}

 * Now we want to add labels to all stations in the graph.
 * Right click in the graph and select **Station Highlighting > Edit**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/10.png" %}

{% include heading.html text="Step 11" %}

 * We add the labeling as a new highlight condition.
 * Press **Add** to add a new condition.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/11.png" %}

{% include heading.html text="Step 12" %}

 * Add dialog will show up where you can configure the desired highlight condition.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/12.png" %}

{% include heading.html text="Step 13" %}

 * Since we want to use labels for all stations, select the **Type** "Apply To All".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/13.png" %}

{% include heading.html text="Step 14" %}

 * Set the **Name** to "Labeling" (just for documentation).
 * Uncheck **Use Color**, since we just want to create labels without coloring.
 * Set the **Label** to "node", since that is the column with station names.
 * Press **OK** to create the highlight condition.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/14.png" %}

{% include heading.html text="Step 15" %}

 * In the dialog with all highlight conditions you should now see a new condition "Labeling".
 * Press **OK** to apply the changes.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/15.png" %}

{% include heading.html text="Step 16" %}

 * In the delivery graph there is now a label next to each station.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/16.png" %}

{% include heading.html text="Step 17" %}

 * FoodChain-Lab also allows to color or label deliveries the same way.
 * To open the dialog for editing delivery highlight conditions right click in the graph, select **Delivery Highlighting > Edit** and proceed in the same way as you have just done with the stations.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_highlighting/17.png" %}

