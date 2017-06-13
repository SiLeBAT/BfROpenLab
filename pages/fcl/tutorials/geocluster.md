---
title: Geo-Clustering in FoodChain-Lab
sidebar: fcl_sidebar
permalink: fcl_geocluster.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Perform a clustering using the following workflow: [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip)
 * Cluster all French primary producers by using the **GIS Cluster** node.
 * Use a **Max Neighborhood Distance** of 100km.
 * That means two stations are put into the same cluster if their distance is less than 100km.

{% include heading.html text="Step 1" %}

 * Import the Example Workflow from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/1.png" %}

{% include heading.html text="Step 2" %}

 * Drag the **GIS Cluster** node from **FoodChain-Lab** in the **Node Repository** to the **Workflow Editor**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/2.png" %}

{% include heading.html text="Step 3" %}

 * Connect the output of **Joiner** to the input of **GIS Cluster**.
 * Connect the output of **GIS Cluster** to the first input of **Tracing View**.
 * Double click on the **GIS Cluster** node to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/3.png" %}

{% include heading.html text="Step 4" %}

 * In this dialog you can set up an algorithm for geographical clustering based on latitude and longitude.
 * Click on **Set Filter** to define which stations should be clustered.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/4.png" %}

{% include heading.html text="Step 5" %}

 * You should see this dialog now.
 * Press the button in the red circle to change the **Property** value.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/5.png" %}

{% include heading.html text="Step 6" %}

 * Select "Country".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/6.png" %}

{% include heading.html text="Step 7" %}

 * Now select "FR" as **Value**, since we want to cluster stations in France.
 * Afterwards press **Add** to add another condition.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/7.png" %}

{% include heading.html text="Step 8" %}

 * For the new condition select "type of business" as **Property** and "Primary Producer" as **Value**, since we want to cluster primary producers only.
 * Now press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/8.png" %}

{% include heading.html text="Step 9" %}

 * Set the **Max Neighborhood Distance** to 100km. That means that stations with distance of less than 100km are put into the same cluster. For details on the algorithm look here: [https://en.wikipedia.org/wiki/DBSCAN](https://en.wikipedia.org/wiki/DBSCAN)
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/9.png" %}

{% include heading.html text="Step 10" %}

 * Right click on **GIS Cluster** to open its context menu and select **Execute** to execute the node.
 * The results of the clustering are put into the new column **ClusterID**. This column will now be used in the **Tracing View**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/10.png" %}

{% include heading.html text="Step 11" %}

 * Open the **Tracing View** by double-clicking on it.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/11.png" %}

{% include heading.html text="Step 12" %}

 * A window showing the delivery network should open now.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/12.png" %}

{% include heading.html text="Step 13" %}

 * Right click in the graph to open the context menu and select **Collapse by Property**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/13.png" %}

{% include heading.html text="Step 14" %}

 * The clustering will be done based on the results of the **GIS Cluster** node.
 * Select **ClusterID** and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/14.png" %}

{% include heading.html text="Step 15" %}

 * Just press **OK**, since we do not want to exclude any area.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/15.png" %}

{% include heading.html text="Step 16" %}

 * All French primary producers have been clustered to areas.
 * Each selected station (blue circle) is an area in France.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/16.png" %}

{% include heading.html text="Step 17" %}

 * Select "Picking" as **Editing Mode** and click in the graph to deselect all stations.
 * You can now see, that one of the stations is yellow. That means, that this stations (French area) is connected to all outbreak spots (red circles).
 * Press **Switch to GIS** to see where this area is.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/17.png" %}

{% include heading.html text="Step 18" %}

 * The area is in Southern France.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocluster/18.png" %}

