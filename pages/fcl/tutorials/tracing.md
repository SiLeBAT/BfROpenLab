---
title: Tracing in FoodChain-Lab
sidebar: fcl_sidebar
permalink: fcl_tracing.html
folder: fcl
---

{% include heading.html text="Tasks" %}

 * Use the example workflow from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).
 * Visualize the forward and backward trace of a station via the **Tracing View**.
 * Deactivate "Cross Contamination" for the black station and see how the trace changes.

{% include heading.html text="Step 1" %}

 * Import the Example Workflow from [https://github.com/SiLeBAT/BfROpenLa...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/workflows/FCL_Example.zip).
 * Open the **Tracing View** by double-clicking on it.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/1.png" %}

{% include heading.html text="Step 2" %}

 * In the delivery graph you can see 9 **Outbreak** stations (red) and one station where **Cross Contamination** is assumed (black).
 * The size of each station is based on its "Score", which depends on the **Outbreak** stations that can be reached from the station.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/2.png" %}

{% include heading.html text="Step 3" %}

 * We will now observe the trace of a single station in detail.
 * Set "Picking" as **Editing Mode** and double click on the station in the red circle.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/3.png" %}

{% include heading.html text="Step 4" %}

 * A dialog will pop up, that all attributes of the station.
 * Additionally you can change "Weight", "Cross Contamination", "Kill Contamination" and "Observed".

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/4.png" %}

{% include heading.html text="Step 5" %}

 * Select **Observed** and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/5.png" %}

{% include heading.html text="Step 6" %}

 * All stations/deliveries of the forward trace are orange-colored and the ones of the backward trace are purple.
 * Three **Outbreak** stations are also orange striped now. That means they are also on the forward trace of the observed station.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/6.png" %}

{% include heading.html text="Step 7" %}

 * Let's see what happens if we deactivate cross contamination in the station in the red circle.
 * So double click on it.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/7.png" %}

{% include heading.html text="Step 8" %}

 * Uncheck **CrossContamination** and press **OK**.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/8.png" %}

{% include heading.html text="Step 9" %}

 * Deactivating cross contamination changed the forward trace of the observed station.
 * Now two of **Outbreak** stations, that were striped before, cannot be reached anymore.

{% include screenshot.html img="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_tracing/9.png" %}

