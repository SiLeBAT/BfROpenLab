---
title: Clustering
sidebar: fcl_sidebar
permalink: fcl_cluster.html
folder: fcl
---

<h2 class="tutorial-heading">Tasks</h2>

 * Perform a clustering using the following workflow: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip)
 * Cluster all French primary producers based on their city.
 * That means all stations from the same city should be put into one meta-station.

<h2 class="tutorial-heading">Step 1</h2>

 * Import the Example Workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).
 * Open the **Tracing View** by double-clicking on it.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/1.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/1.png"/></a>

<h2 class="tutorial-heading">Step 2</h2>

 * A window showing the delivery network opens.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/2.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/2.png"/></a>

<h2 class="tutorial-heading">Step 3</h2>

 * Right click in the graph to open the context menu and select **Set Selected Stations**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/3.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/3.png"/></a>

<h2 class="tutorial-heading">Step 4</h2>

 * You should see this dialog now.
 * Press the button in the red circle to change the **Property** value.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/4.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/4.png"/></a>

<h2 class="tutorial-heading">Step 5</h2>

 * Select "Country".

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/5.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/5.png"/></a>

<h2 class="tutorial-heading">Step 6</h2>

 * Now select "FR" as **Value**, since we want to cluster stations in France.
 * Afterwards press **Add** to add another condition.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/6.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/6.png"/></a>

<h2 class="tutorial-heading">Step 7</h2>

 * For the new condition select "type of business" as **Property** and "Primary Producer" as **Value**, since we want to cluster primary producers only.
 * Now press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/7.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/7.png"/></a>

<h2 class="tutorial-heading">Step 8</h2>

 * All French primary producers are selected now, which is indicated by the blue color.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/8.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/8.png"/></a>

<h2 class="tutorial-heading">Step 9</h2>

 * Right click in the graph to open the context menu and select **Collapse by Property** to cluster the selected stations.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/9.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/9.png"/></a>

<h2 class="tutorial-heading">Step 10</h2>

 * Select **Yes** to only cluster selected stations.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/10.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/10.png"/></a>

<h2 class="tutorial-heading">Step 11</h2>

 * We want to cluster on city level. That means all stations from the same city will be merged.
 * Select **City** and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/11.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/11.png"/></a>

<h2 class="tutorial-heading">Step 12</h2>

 * Just press **OK**, since we do not want to exclude any cities.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/12.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/12.png"/></a>

<h2 class="tutorial-heading">Step 13</h2>

 * All French primary producers have been clustered to cities.
 * Each selected station (blue circle) is a French city.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/13.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/13.png"/></a>

<h2 class="tutorial-heading">Step 14</h2>

 * Select "Picking" as **Editing Mode** and click in the graph to deselect all stations.
 * You can now see, that one of the stations is yellow. That means, that this station (French city) is connected to all outbreak spots (red circles).

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/14.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/14.png"/></a>

<h2 class="tutorial-heading">Step 15</h2>

 * Since the graph looks confusing now, we should reapply the layout algorithm.
 * Right click in the graph and select **Apply Layout > Fruchterman-Reingold** in the context menu.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/15.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/15.png"/></a>

<h2 class="tutorial-heading">Step 16</h2>

 * The stations should be arranged in better way now.
 * The layout algorithm is not deterministic, therefore your result will look different from the screenshot.
 * To see which city is connected to all outbreak spots double click on the yellow circle.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/16.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/16.png"/></a>

<h2 class="tutorial-heading">Step 17</h2>

 * As you can see in the dialog the city is "Perpignan".
 * Press **Switch to GIS** to see the city and its relations on a map.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/17.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_cluster/17.png"/></a>
