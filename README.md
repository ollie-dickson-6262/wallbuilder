# WALLBUILDER

This project is a simple Java application that generates a build plan for masonry walls created for the Monumental 
Forward Deployed Robot Engineer take-home assignment. All of the business logic is carried out by the application services and can be called via three separate endpoints for different bond patterns: stretcher, flemish and wild.

I have used the p5.js graphical visualisation library to display the wall pattern and show the build path brick by brick. This can run locally in the browser, p5.js seem to strongly advise using VSCode to do this but it should work with other code editors as long as they allow you to open the `index.html` file with a live server.

## Prerequisites

- Java (21)
- Maven
- Ideally, VSCode for p5.js visualisation library

## How to run locally

- To run the application locally run `mvn spring-boot:run` from the root directory.
- To see the visualisation of the brick path, in VSCode, right click the `frontend/index.html` file and select `Open with Live Server` which should launch a local development server and display in your browser.
- The Server url is referenced in the WallController to allow access, so if the url doesn't match that origins paramter: `"http://127.0.0.1:5500"` you may need to update it.
- The default bond will be the Stretcher bond, to switch between bonds, open the `sketch.js` file and change the currentBondUrl value, save the file and reload the page in the browser.
- When the page loads, it should display the full bond pattern and highlight the next brick to place. Press the 'enter' key to place more bricks. Each stride will display in a different colour. There is also a box that will follow the mouse which displays the build envelope.