---
title: Geocoding
sidebar: fcl_sidebar
permalink: fcl_geocoding.html
folder: fcl
---

<h2 class="tutorial-heading">Tasks</h2>

 * Perform a geocoding by using the Geocoding workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/Geocoding.zip).
 * Use "Street", "HouseNumber", "City" and "Country" as input parameters.
 * Do the geocoding by using the MapQuest Geocoding Service.

<h2 class="tutorial-heading">Step 1</h2>

 * Import the Geocoding workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/Geocoding.zip).
 * In this tutorial we are using the MapQuest Open Geocoding service.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/1.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/1.png"/></a>

<h2 class="tutorial-heading">Step 2</h2>

 * For using MapQuest you have to register and create a key at [https://developer.mapquest.com](https://developer.mapquest.com)
 * This key has to be entered in the KNIME preferences.
 * Select **File < Preferences** in the menu bar.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/2.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/2.png"/></a>

<h2 class="tutorial-heading">Step 3</h2>

 * The Preferences dialog will pop up.
 * Here you can specify all preferences for KNIME and FoodChain-Lab.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/3.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/3.png"/></a>

<h2 class="tutorial-heading">Step 4</h2>

 * Select **KNIME < Geocoding** in the navigation tree on the left.
 * Enter your **MapQuest Application Key** and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/4.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/4.png"/></a>

<h2 class="tutorial-heading">Step 5</h2>

 * To perform geocoding we need one column with addresses in our data table. The **Supply Chain Reader** puts out all parts of the address (street, city, ...) in different columns.
 * The address column is created via the **Address Creator** node.
 * Double click on this node to open its dialog.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/5.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/5.png"/></a>

<h2 class="tutorial-heading">Step 6</h2>

 * In the dialog you can specify the columns that should used for creating the address column.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/6.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/6.png"/></a>

<h2 class="tutorial-heading">Step 7</h2>

 * Since we want to do the Geocoding based on "Street", "HouseNumber", "City" and "Country", we have to set the **Country Column** to "Country" and the **Postal Code Column** to "none".
 * Press **OK** to close the dialog.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/7.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/7.png"/></a>

<h2 class="tutorial-heading">Step 8</h2>

 * Since we changed the settings, the node resets automatically.
 * Press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/8.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/8.png"/></a>

<h2 class="tutorial-heading">Step 9</h2>

 * The configuration for the **Address Creator** has been updated.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/9.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/9.png"/></a>

<h2 class="tutorial-heading">Step 10</h2>

 * Right click on the **Address Creator** node and select **Execute**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/10.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/10.png"/></a>

<h2 class="tutorial-heading">Step 11</h2>

 * Now that we updated the **Address**, the geocoding can be set up.
 * Double click on the **Geocoding** node to open its dialog.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/11.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/11.png"/></a>

<h2 class="tutorial-heading">Step 12</h2>

 * Here you can specify the **Service Provider** for geocoding and the column that should be used.
 * Both are already correctly set, so we don't need to change anything here.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/12.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/12.png"/></a>

<h2 class="tutorial-heading">Step 13</h2>

 * For many requests geocoding services return multiple results (e.g. when there are two streets with the same name).
 * To deal with this we have to decide if we just want to use the first or look at all choices and try to find the best.
 * Looking manually at all choices is a lot of work for large data sets. In this tutorial select **Use first** and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/13.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/13.png"/></a>

<h2 class="tutorial-heading">Step 14</h2>

 * Right click on the **Geocoding** node and select **Execute**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/14.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/14.png"/></a>

<h2 class="tutorial-heading">Step 15</h2>

 * The execution can take a while.
 * The progress bar under the node shows what percentage of data has been processed.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/15.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/15.png"/></a>

<h2 class="tutorial-heading">Step 16</h2>

 * When the execution is finished, we can look at the results.
 * Right click on the **Geocoding** node and select **Coordinates**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/16.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/16.png"/></a>

<h2 class="tutorial-heading">Step 17</h2>

 * In the dialog that pops up, you can look at the whole data table.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/17.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/17.png"/></a>

<h2 class="tutorial-heading">Step 18</h2>

 * Scroll to the right to look at the columns with latitude and longitude (the two rightmost columns).
 * Some geocoding requests were not successful. MapQuest returned US coordinates, although all addresses are in Germany.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/18.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_geocoding/18.png"/></a>
