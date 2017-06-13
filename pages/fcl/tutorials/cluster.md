---
title: Clustering in FoodChain-Lab
sidebar: fcl_sidebar
permalink: fcl_cluster.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Perform a clustering using the following workflow: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip)
 * Cluster all French primary producers based on their city.
 * That means all stations from the same city should be put into one meta-station.

{% include heading.html text="Step 1" %}

 * Import the Example Workflow from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).
 * Open the **Tracing View** by double-clicking on it.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/1.png" %}

{% include heading.html text="Step 2" %}

 * A window showing the delivery network opens.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/2.png" %}

{% include heading.html text="Step 3" %}

 * Right click in the graph to open the context menu and select **Set Selected Stations**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/3.png" %}

{% include heading.html text="Step 4" %}

 * You should see this dialog now.
 * Press the button in the red circle to change the **Property** value.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/4.png" %}

{% include heading.html text="Step 5" %}

 * Select "Country".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/5.png" %}

{% include heading.html text="Step 6" %}

 * Now select "FR" as **Value**, since we want to cluster stations in France.
 * Afterwards press **Add** to add another condition.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/6.png" %}

{% include heading.html text="Step 7" %}

 * For the new condition select "type of business" as **Property** and "Primary Producer" as **Value**, since we want to cluster primary producers only.
 * Now press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/7.png" %}

{% include heading.html text="Step 8" %}

 * All French primary producers are selected now, which is indicated by the blue color.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/8.png" %}

{% include heading.html text="Step 9" %}

 * Right click in the graph to open the context menu and select **Collapse by Property** to cluster the selected stations.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/9.png" %}

{% include heading.html text="Step 10" %}

 * Select **Yes** to only cluster selected stations.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/10.png" %}

{% include heading.html text="Step 11" %}

 * We want to cluster on city level. That means all stations from the same city will be merged.
 * Select **City** and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/11.png" %}

{% include heading.html text="Step 12" %}

 * Just press **OK**, since we do not want to exclude any cities.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/12.png" %}

{% include heading.html text="Step 13" %}

 * All French primary producers have been clustered to cities.
 * Each selected station (blue circle) is a French city.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/13.png" %}

{% include heading.html text="Step 14" %}

 * Select "Picking" as **Editing Mode** and click in the graph to deselect all stations.
 * You can now see, that one of the stations is yellow. That means, that this station (French city) is connected to all outbreak spots (red circles).

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/14.png" %}

{% include heading.html text="Step 15" %}

 * Since the graph looks confusing now, we should reapply the layout algorithm.
 * Right click in the graph and select **Apply Layout > Fruchterman-Reingold** in the context menu.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/15.png" %}

{% include heading.html text="Step 16" %}

 * The stations should be arranged in better way now.
 * The layout algorithm is not deterministic, therefore your result will look different from the screenshot.
 * To see which city is connected to all outbreak spots double click on the yellow circle.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/16.png" %}

{% include heading.html text="Step 17" %}

 * As you can see in the dialog the city is "Perpignan".
 * Press **Switch to GIS** to see the city and its relations on a map.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/17.png" %}

