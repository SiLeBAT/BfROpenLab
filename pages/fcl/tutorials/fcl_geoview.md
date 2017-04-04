---
title: Geo-Visualization
sidebar: fcl_sidebar
permalink: fcl_geoview.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Use the following workflow: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).
 * Visualize the delivery network in the GIS mode of the **Tracing View**.
 * Select all stations in Poland and check out where these stations are located in the Graph mode.

{% include heading.html text="Step 1" %}

 * Import the Example Workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).
 * Open the **Tracing View** by double-clicking on it.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geoview/1.png" %}

{% include heading.html text="Step 2" %}

 * Now you should see a graphical representation of the delivery network.
 * To switch to the geographical representation click **Switch to GIS** in the upper right corner.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geoview/2.png" %}

{% include heading.html text="Step 3" %}

 * The country borders, which where read from a shapefile, are used for geographical visualization.
 * To zoom to a certain area of the graph select "Transforming" as **Editing Mode** and zoom/move the graph by using the mouse wheel and the left mouse button (works as in Google Maps).

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geoview/3.png" %}

{% include heading.html text="Step 4" %}

 * If you are online, you can change the type of visualization.
 * Select e.g. "Mapnik" as **GIS Type**.
 * Now you should see a visualization with OpenStreetMap data.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geoview/4.png" %}

{% include heading.html text="Step 5" %}

 * We can now select certain stations based on geography.
 * Select "Picking" as **Editing Mode** and select all stations in Poland by dragging with the left mouse button a rect around the stations.
 * The selected stations are now colored blue.
 * Switch back to the graphical view by clicking **Switch to Graph** in the upper right corner.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geoview/5.png" %}

{% include heading.html text="Step 6" %}

 * The blue stations are the ones you selected in the geographical view, since changes you make in any of the view are automatically applied to the other view.
 * That makes it easy to switch back and forth between both representations and use the benefits of both.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geoview/6.png" %}

{% include heading.html text="Step 7" %}

 * Lets now select a certain "cluster" in the graphical view and see where the stations are in the geographical view.
 * So select the "cluster" in the red circle and click **Switch to GIS**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geoview/7.png" %}

{% include heading.html text="Step 8" %}

 * As you can see the stations of the cluster are located all over France.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geoview/8.png" %}
