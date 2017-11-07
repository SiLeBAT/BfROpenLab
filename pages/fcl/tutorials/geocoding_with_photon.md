---
title: Geocoding with Photon in FoodChain-Lab
sidebar: fcl_sidebar
permalink: fcl_geocoding_with_photon.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Perform a geocoding by using the Geocoding workflow from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/Geocoding_with_Photon.knwf).
 * Add the **FoodChain-Lab** **Geocoding** node.
 * Do the geocoding by using the Photon Geocoding Service.

{% include heading.html text="Step 1" %}

 * Import the Geocoding workflow from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/Geocoding_with_Photon.knwf).

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/1.png" %}

{% include heading.html text="Step 2" %}

 * To perform geocoding we need to add the **FoodChain-Lab** **Geocoding** node.
 * Add the **FoodChain-Lab** **Geocoding** node by double clicking on it in the **Node Repository**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/2.png" %}

{% include heading.html text="Step 3" %}

 * A **Geocoding** node was added to the workflow. It is connected to the **SupplyChainReader** and needs to be set up.
 * Open its configuration by double clicking on it or by using its context menue (right click on the node).

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/3.png" %}

{% include heading.html text="Step 4" %}

 * Set the **Service Provider** to **Photon**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/4.png" %}

{% include heading.html text="Step 5" %}

 * The **Address** is properly set
 * You need to set the **Server Address**. You can use **http://photon.komoot.de**, which is a public available service.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/5.png" %}

{% include heading.html text="Step 6" %}

 * For many requests geocoding services return multiple results (e.g. when there are two streets with the same name).
 * To deal with this we have to decide if we just want to use the first or look at all choices and try to find the best.
 * Looking manually at all choices is a lot of work for large data sets. In this tutorial select **Use first** and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/6.png" %}

{% include heading.html text="Step 7" %}

 * Right click on the **Geocoding** node and select **Execute**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/7.png" %}

{% include heading.html text="Step 8" %}

 * The execution can take a while.
 * The progress bar under the node shows what percentage of data has been processed.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/8.png" %}

{% include heading.html text="Step 9" %}

 * When the execution is finished, we can look at the results.
 * Right click on the **Geocoding** node and select **Coordinates**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/9.png" %}

{% include heading.html text="Step 10" %}

 * In the dialog that pops up, you can look at the whole data table.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/10.png" %}

{% include heading.html text="Step 11" %}

 * Scroll to the right to look at the columns with latitude and longitude (the two rightmost columns).
 * One geocoding request was not successful.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding_with_photon/11.png" %}

