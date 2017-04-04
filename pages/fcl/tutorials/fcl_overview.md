---
title: Overview
sidebar: fcl_sidebar
permalink: fcl_overview.html
folder: fcl
---

<h2 class="tutorial-heading">FoodChain-Lab Concepts 1</h2>

 * **Delivery**: Something send from A to B at a certain date. A delivery can have preceding and subsequent deliveries (e.g. strawberry-delivery -> strawberry-cake-delivery).
 * **Station**: Any food business operator, that sends and/or receives deliveries.
 * **Trace**: The path a contamination can take. A station/delivery "B" is on the **forward trace** of a station/delivery "A", if a contamination at "A" can spread to "B" via the food chain network. If "B" is on the **forward trace** of "A", then "A" is on the **backward trace** of "B".

<h2 class="tutorial-heading">FoodChain-Lab Concepts 2</h2>

 * **Weight**: Weights are assigned to stations/deliveries, that are involved in an outbreak (e.g. a restaurant where customers got sick). Different weights can be used to model differences between involved stations/deliveries (e.g. higher weight = higher likelihood that station is involved)..
 * **Cross Contamination**: When it is applied at a station, its incoming deliveries contaminate its outgoing deliveries. When applied on delivery level, the selected incoming deliveries of station contaminate each others subsequent deliveries.
 * **Kill Contamination**: When it is applied at a station/delivery, the contamination is killed there. That means it does not spread to subsequent stations/deliveries.
 * **Score**: Is computed based on given weights and cross contamination. Should help to estimate the likelihood that a certain station is the origin of the outbreak (higher score = more/higher weighted stations on forward trace).

<h2 class="tutorial-heading">FoodChain-Lab Score Computation 1</h2>

 * s<sub>i</sub> is the i-th station or delivery
 * w<sub>j</sub> is the weight of the j-th station or delivery
 * t<sub>ij</sub> has a value of 1, if there is a trace from s<sub>i</sub> to s<sub>j</sub> and a value of 0 otherwise
 * n is the total number of stations and deliveries

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/score.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/score.png"/></a>

<h2 class="tutorial-heading">FoodChain-Lab Score Computation 2</h2>

 * FoodChain-Lab also allows to assign negative weights to stations/deliveries.
 * A negative weight should indicate, that a station/delivery is not involved in the outbreak.
 * When negative weights are used, the score computation changes to this formula.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/new_score.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/new_score.png"/></a>

<h2 class="tutorial-heading">Introduction to KNIME</h2>

 * KNIME is an open source data analytics platform, that allows users to assemble a data pipeline called "workflow".
 * A workflow is built by dragging nodes from the **Node Repository** onto the **Workflow Editor** and connecting them ([https://tech.knime.org/workbench](https://tech.knime.org/workbench)).
 * Nodes are processing units with input- and/or output ports.
 * Data is transferred over a connection from an out-port to the in-port of another node.
 * A comprehensive KNIME quickstart guide can be found at [https://tech.knime.org/files/KNIME_qu...](https://tech.knime.org/files/KNIME_quickstart.pdf).
 * An introduction video is available at [https://www.youtube.com/watch?v=ft7Ks...](https://www.youtube.com/watch?v=ft7Ksgss3Tc).

<h2 class="tutorial-heading">Available Nodes</h2>

 * Detailed descriptions of all nodes are available in the **Node Description** view of the KNIME workbench ([https://tech.knime.org/workbench](https://tech.knime.org/workbench)).
 * All inputs and outputs are either **data tables** (triangles) or **images** (green square). Therefore standard KNIME nodes (**Row Filter**, **Image Port Writer**, ...) can be used in FoodChain-Lab workflows.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/1.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/1.png"/></a>

<h2 class="tutorial-heading">Tracing</h2>

 * Supply chain data is read from the internal database via the **Supply Chain Reader**.
 * This data can be visualized with the **Tracing View**. The **Tracing View** also allows to perform a tracing on the data.
 * The **Tracing** node performs tracing without visualization. Its output can be used in the **Tracing View** (e.g. to perform some tracings as a preprocessing step)

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/2.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/2.png"/></a>

<h2 class="tutorial-heading">Using GIS data</h2>

 * The **Geocoding** node allows to acquire latitude/longitude data from addresses.
 * This data can be geographically clustered with the **GIS Cluster** node.
 * The **Tracing View** allows geographical visualization, if GIS data is provided from the **Shapefile Reader**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/3.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_overview/3.png"/></a>
