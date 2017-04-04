---
title: Tracing
sidebar: fcl_sidebar
permalink: fcl_tracing.html
folder: fcl
---

<h2 class="tutorial-heading">Tasks</h2>

 * Use the example workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).
 * Visualize the forward and backward trace of a station via the **Tracing View**.
 * Deactivate "Cross Contamination" for the black station and see how the trace changes.

<h2 class="tutorial-heading">Step 1</h2>

 * Import the Example Workflow from [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).
 * Open the **Tracing View** by double-clicking on it.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/1.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/1.png"/></a>

<h2 class="tutorial-heading">Step 2</h2>

 * In the delivery graph you can see 9 **Outbreak** stations (red) and one station where **Cross Contamination** is assumed (black).
 * The size of each station is based on its "Score", which depends on the **Outbreak** stations that can be reached from the station.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/2.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/2.png"/></a>

<h2 class="tutorial-heading">Step 3</h2>

 * We will now observe the trace of a single station in detail.
 * Set "Picking" as **Editing Mode** and double click on the station in the red circle.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/3.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/3.png"/></a>

<h2 class="tutorial-heading">Step 4</h2>

 * A dialog will pop up, that all attributes of the station.
 * Additionally you can change "Weight", "Cross Contamination", "Kill Contamination" and "Observed".

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/4.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/4.png"/></a>

<h2 class="tutorial-heading">Step 5</h2>

 * Select **Observed** and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/5.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/5.png"/></a>

<h2 class="tutorial-heading">Step 6</h2>

 * All stations/deliveries of the forward trace are orange-colored and the ones of the backward trace are purple.
 * Three **Outbreak** stations are also orange striped now. That means they are also on the forward trace of the observed station.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/6.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/6.png"/></a>

<h2 class="tutorial-heading">Step 7</h2>

 * Let's see what happens if we deactivate cross contamination in the station in the red circle.
 * So double click on it.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/7.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/7.png"/></a>

<h2 class="tutorial-heading">Step 8</h2>

 * Uncheck **CrossContamination** and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/8.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/8.png"/></a>

<h2 class="tutorial-heading">Step 9</h2>

 * Deactivating cross contamination changed the forward trace of the observed station.
 * Now two of **Outbreak** stations, that were striped before, cannot be reached anymore.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/9.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/9.png"/></a>
