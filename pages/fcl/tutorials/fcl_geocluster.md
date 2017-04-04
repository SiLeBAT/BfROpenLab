---
title: Geo-Clustering
sidebar: fcl_sidebar
permalink: fcl_geocluster.html
folder: fcl
---

<h2 class="tutorial-heading">Tasks</h2>

 * Perform a clustering using the following workflow: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip)
 * Cluster all French primary producers by using the **GIS Cluster** node.
 * Use a **Max Neighborhood Distance** of 100km.
 * That means two stations are put into the same cluster if their distance is less than 100km.

<h2 class="tutorial-heading">Step 1</h2>

 * Import the Example Workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/1.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/1.png"/></a>

<h2 class="tutorial-heading">Step 2</h2>

 * Drag the **GIS Cluster** node from **FoodChain-Lab** in the **Node Repository** to the **Workflow Editor**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/2.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/2.png"/></a>

<h2 class="tutorial-heading">Step 3</h2>

 * Connect the output of **Joiner** to the input of **GIS Cluster**.
 * Connect the output of **GIS Cluster** to the first input of **Tracing View**.
 * Double click on the **GIS Cluster** node to open its dialog.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/3.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/3.png"/></a>

<h2 class="tutorial-heading">Step 4</h2>

 * In this dialog you can set up an algorithm for geographical clustering based on latitude and longitude.
 * Click on **Set Filter** to define which stations should be clustered.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/4.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/4.png"/></a>

<h2 class="tutorial-heading">Step 5</h2>

 * You should see this dialog now.
 * Press the button in the red circle to change the **Property** value.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/5.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/5.png"/></a>

<h2 class="tutorial-heading">Step 6</h2>

 * Select "Country".

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/6.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/6.png"/></a>

<h2 class="tutorial-heading">Step 7</h2>

 * Now select "FR" as **Value**, since we want to cluster stations in France.
 * Afterwards press **Add** to add another condition.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/7.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/7.png"/></a>

<h2 class="tutorial-heading">Step 8</h2>

 * For the new condition select "type of business" as **Property** and "Primary Producer" as **Value**, since we want to cluster primary producers only.
 * Now press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/8.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/8.png"/></a>

<h2 class="tutorial-heading">Step 9</h2>

 * Set the **Max Neighborhood Distance** to 100km. That means that stations with distance of less than 100km are put into the same cluster. For details on the algorithm look here: [https://en.wikipedia.org/wiki/DBSCAN](https://en.wikipedia.org/wiki/DBSCAN)
 * Press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/9.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/9.png"/></a>

<h2 class="tutorial-heading">Step 10</h2>

 * Right click on **GIS Cluster** to open its context menu and select **Execute** to execute the node.
 * The results of the clustering are put into the new column **ClusterID**. This column will now be used in the **Tracing View**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/10.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/10.png"/></a>

<h2 class="tutorial-heading">Step 11</h2>

 * Open the **Tracing View** by double-clicking on it.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/11.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/11.png"/></a>

<h2 class="tutorial-heading">Step 12</h2>

 * A window showing the delivery network should open now.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/12.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/12.png"/></a>

<h2 class="tutorial-heading">Step 13</h2>

 * Right click in the graph to open the context menu and select **Collapse by Property**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/13.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/13.png"/></a>

<h2 class="tutorial-heading">Step 14</h2>

 * The clustering will be done based on the results of the **GIS Cluster** node.
 * Select **ClusterID** and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/14.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/14.png"/></a>

<h2 class="tutorial-heading">Step 15</h2>

 * Just press **OK**, since we do not want to exclude any area.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/15.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/15.png"/></a>

<h2 class="tutorial-heading">Step 16</h2>

 * All French primary producers have been clustered to areas.
 * Each selected station (blue circle) is an area in France.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/16.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/16.png"/></a>

<h2 class="tutorial-heading">Step 17</h2>

 * Select "Picking" as **Editing Mode** and click in the graph to deselect all stations.
 * You can now see, that one of the stations is yellow. That means, that this stations (French area) is connected to all outbreak spots (red circles).
 * Press **Switch to GIS** to see where this area is.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/17.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/17.png"/></a>

<h2 class="tutorial-heading">Step 18</h2>

 * The area is in Southern France.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/18.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/18.png"/></a>
