---
title: Data Collection (Part 2)
sidebar: fcl_sidebar
permalink: fcl_datacollecting_2.html
folder: fcl
---

<h2 class="tutorial-heading">Tasks</h2>

 * In this part of the tutorial we will do a back- and forward-tracing from the "Frozen Fruit Sales" station.
 * You can either complete part 1 of the tutorial or directly import the following files into an empty database before starting this tutorial.
 * Start Template: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/Start_Tracing_Caterers.xlsx)
 * Caterer 1 Template: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/Backtrace_request_Caterer 1.xlsx)
 * Caterer 2 Template: [https://github.com/SiLeBAT/BfROpenLab...](https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/Backtrace_request_Caterer 2.xlsx)

<h2 class="tutorial-heading">Step 1</h2>

 * You should have the database interface open.
 * Press the button for generating backtracing templates, which is marked by the red circle.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/1.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/1.png"/></a>

<h2 class="tutorial-heading">Step 2</h2>

 * Since we want to do the backtracing for the supplier "Frozen Fruit Sales", select **Supplier** only and press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/2.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/2.png"/></a>

<h2 class="tutorial-heading">Step 3</h2>

 * In the file dialog that appears, you can specify the folder where the generated templates should be saved.
 * Select the desired folder and press **Save**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/3.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/3.png"/></a>

<h2 class="tutorial-heading">Step 4</h2>

 * You'll be noticed, that one template was generated in the folder you specified.
 * Press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/4.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/4.png"/></a>

<h2 class="tutorial-heading">Step 5</h2>

 * Open the generated template "Backtrace_request_Frozen Fruit Sales.xlsx".
 * Add the stations from the screenshot to the **Stations** sheet.
 * These station include 3 strawberry suppliers that delivered strawberries to "Frozen Fruit Sales" and also a caterer that received strawberries.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/5.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/5.png"/></a>

<h2 class="tutorial-heading">Step 6</h2>

 * In the **BackTracing** sheet you can see the three outgoing deliveries of "Frozen Fruit Sales".
 * These deliveries belong to lots "108" and "139".

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/6.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/6.png"/></a>

<h2 class="tutorial-heading">Step 7</h2>

 * Now scroll to the **Ingredients for Lot(s)** section.
 * Enter the 3 deliveries, that were used as ingredients for lot "108", from the screenshot.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/7.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/7.png"/></a>

<h2 class="tutorial-heading">Step 8</h2>

 * Lot "108" was not only delivered to "Caterer 1" and "Caterer 2", but also to a third caterer.
 * We can add this information in the **ForwardTracing_Opt** sheet.
 * Enter the delivery to "Caterer 3" from the screenshot.
 * Save the completed document ("Backtrace_request_Frozen Fruit Sales.xlsx").

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/8.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/8.png"/></a>

<h2 class="tutorial-heading">Step 9</h2>

 * To import this file click on the **Table import** button in the upper left corner of the database interface.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/9.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/9.png"/></a>

<h2 class="tutorial-heading">Step 10</h2>

 * In the file dialog that appears, select "Backtrace_request_Frozen Fruit Sales.xlsx" and press **Open**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/10.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/10.png"/></a>

<h2 class="tutorial-heading">Step 11</h2>

 * You'll be notified, that some warnings occurred during import.
 * Press **Show Details** to have at look at the warnings.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/11.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/11.png"/></a>

<h2 class="tutorial-heading">Step 12</h2>

 * 730kg of strawberries went into lot "108", but all the deliveries of frozen strawberries from lot "108" just add up to 99.75kg.
 * Warnings like these are supposed to help finding errors in the data.
 * In this tutorial we just ignore the warning. So press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/12.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/12.png"/></a>

<h2 class="tutorial-heading">Step 13</h2>

 * We now know that lot "108" was also delivered to "Caterer 3" and we have imported this delivery into the database.
 * Let's do a forward tracing from "Caterer 3".
 * Press the button for generating the forward template for a specific station marked by the red circle.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/13.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/13.png"/></a>

<h2 class="tutorial-heading">Step 14</h2>

 * In the dialog that appears, you must select the station for which the template should be generated.
 * Enter "cate" in the field after **Enter Search Query** and you will see that the table below only shows the caterers now.
 * Press the **Select** button for "Caterer 3".

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/14.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/14.png"/></a>

<h2 class="tutorial-heading">Step 15</h2>

 * In the file dialog that appears, you can specify the folder where the generated templates should be saved.
 * Select the desired folder and press **Save**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/15.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/15.png"/></a>

<h2 class="tutorial-heading">Step 16</h2>

 * You'll be noticed, that one template was generated in the folder you specified.
 * Press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/16.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/16.png"/></a>

<h2 class="tutorial-heading">Step 17</h2>

 * Open "StationFwdtrace_request_Caterer 3.xlsx".
 * Enter the station from the screenshot in the **Stations** sheet.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/17.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/17.png"/></a>

<h2 class="tutorial-heading">Step 18</h2>

 * In the **FwdTracing** sheet you first have to enter all relevant outgoing lots of "Caterer 3". So add lot "C3M1" to the **Lot Information** section as shown on the screenshot (red rectangle).
 * Now you can specify, that the delivery of "Frozen Strawberries" was used as an ingredient in this lot (red circle).

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/18.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/18.png"/></a>

<h2 class="tutorial-heading">Step 19</h2>

 * Scroll down to the **Products Out** section to specify the outgoing deliveries from lot "C3M1".
 * Add the deliveries as shown on the screenshot.
 * Save the completed document ("StationFwdtrace_request_Caterer 3.xlsx").

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/19.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/19.png"/></a>

<h2 class="tutorial-heading">Step 20</h2>

 * To import this file click on the **Table import** button in the upper left corner of the database interface.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/20.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/20.png"/></a>

<h2 class="tutorial-heading">Step 21</h2>

 * In the file dialog that appears, select "StationFwdtrace_request_Caterer 3.xlsx" and press **Open**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/21.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/21.png"/></a>

<h2 class="tutorial-heading">Step 22</h2>

 * You'll see a message that the import was successful.
 * Press **OK**.

<a href="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/22.png"><img class="aligncenter" src="https://github.com/SiLeBAT/BfROpenLabResources/raw/master/GitHubPages/documents/foodchainlab_datacollecting_2/22.png"/></a>
