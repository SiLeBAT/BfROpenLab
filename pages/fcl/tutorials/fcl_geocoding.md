---
title: Geocoding
sidebar: fcl_sidebar
permalink: fcl_geocoding.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Perform a geocoding by using the Geocoding workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/Geocoding.zip).
 * Use "Street", "HouseNumber", "City" and "Country" as input parameters.
 * Do the geocoding by using the MapQuest Geocoding Service.

{% include heading.html text="Step 1" %}

 * Import the Geocoding workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/Geocoding.zip).
 * In this tutorial we are using the MapQuest Open Geocoding service.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/1.png" %}

{% include heading.html text="Step 2" %}

 * For using MapQuest you have to register and create a key at [https://developer.mapquest.com](https://developer.mapquest.com)
 * This key has to be entered in the KNIME preferences.
 * Select **File < Preferences** in the menu bar.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/2.png" %}

{% include heading.html text="Step 3" %}

 * The Preferences dialog will pop up.
 * Here you can specify all preferences for KNIME and FoodChain-Lab.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/3.png" %}

{% include heading.html text="Step 4" %}

 * Select **KNIME < Geocoding** in the navigation tree on the left.
 * Enter your **MapQuest Application Key** and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/4.png" %}

{% include heading.html text="Step 5" %}

 * To perform geocoding we need one column with addresses in our data table. The **Supply Chain Reader** puts out all parts of the address (street, city, ...) in different columns.
 * The address column is created via the **Address Creator** node.
 * Double click on this node to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/5.png" %}

{% include heading.html text="Step 6" %}

 * In the dialog you can specify the columns that should used for creating the address column.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/6.png" %}

{% include heading.html text="Step 7" %}

 * Since we want to do the Geocoding based on "Street", "HouseNumber", "City" and "Country", we have to set the **Country Column** to "Country" and the **Postal Code Column** to "none".
 * Press **OK** to close the dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/7.png" %}

{% include heading.html text="Step 8" %}

 * Since we changed the settings, the node resets automatically.
 * Press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/8.png" %}

{% include heading.html text="Step 9" %}

 * The configuration for the **Address Creator** has been updated.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/9.png" %}

{% include heading.html text="Step 10" %}

 * Right click on the **Address Creator** node and select **Execute**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/10.png" %}

{% include heading.html text="Step 11" %}

 * Now that we updated the **Address**, the geocoding can be set up.
 * Double click on the **Geocoding** node to open its dialog.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/11.png" %}

{% include heading.html text="Step 12" %}

 * Here you can specify the **Service Provider** for geocoding and the column that should be used.
 * Both are already correctly set, so we don't need to change anything here.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/12.png" %}

{% include heading.html text="Step 13" %}

 * For many requests geocoding services return multiple results (e.g. when there are two streets with the same name).
 * To deal with this we have to decide if we just want to use the first or look at all choices and try to find the best.
 * Looking manually at all choices is a lot of work for large data sets. In this tutorial select **Use first** and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/13.png" %}

{% include heading.html text="Step 14" %}

 * Right click on the **Geocoding** node and select **Execute**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/14.png" %}

{% include heading.html text="Step 15" %}

 * The execution can take a while.
 * The progress bar under the node shows what percentage of data has been processed.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/15.png" %}

{% include heading.html text="Step 16" %}

 * When the execution is finished, we can look at the results.
 * Right click on the **Geocoding** node and select **Coordinates**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/16.png" %}

{% include heading.html text="Step 17" %}

 * In the dialog that pops up, you can look at the whole data table.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/17.png" %}

{% include heading.html text="Step 18" %}

 * Scroll to the right to look at the columns with latitude and longitude (the two rightmost columns).
 * Some geocoding requests were not successful. MapQuest returned US coordinates, although all addresses are in Germany.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/18.png" %}
