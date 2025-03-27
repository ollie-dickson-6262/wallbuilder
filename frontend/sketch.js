const stretcherUrl = 'http://localhost:8080/stretcher-bond';
const flemishUrl = 'http://localhost:8080/flemish-bond';
const wildUrl = 'http://localhost:8080/wild-bond';

// Change this value to try different bonds using the urls above
const currentBondUrl = stretcherUrl;

let canvasWidth = 2300/2 + 20;
let canvasHeight = 2000/2 + 20;
let bricks = [];
let placedBricks = 0;
let readyToPlace = true;
envelopeWidth = 800/2;
envelopeHeight = 1300/2;

class Brick {
  constructor(x, y, width, height, strideIndex) {
    this.x = x/2 + 10;
    this.y = y/2 + 10;
    this.width = width/2;
    this.height = height/2;
    this.strideIndex = strideIndex;
  }

  drawBrick(color) {
    fill(color);
    rect(this.x, invertY(this.y), this.width, this.height);
  }
}

function setup() {
  getBricks();
  createCanvas(canvasWidth, canvasHeight);
}

function draw() {
  background("silver");

  if (keyIsPressed) {
    // Enter key pressed
    if (keyCode === 13 && readyToPlace) {
      placedBricks++;
      readyToPlace = false;
    }
  } else {
    readyToPlace = true;
  }

  rectMode(CORNER);

  // Draw Design Bricks
  for (let brick of bricks) {
    brick.drawBrick("cornflowerblue");
  }

  const placedBrickColors = [
    "coral", "blueviolet", "brown", "burlywood", "cadetblue",
    "crimson", "darkgoldenrod", "darkgreen", "darkmagenta", "mediumseagreen"
  ];

  // Draw Placed Bricks with colors based on strideIndex
  for (let i = 0; i < placedBricks; i++) {
    const color = placedBrickColors[bricks[i].strideIndex % placedBrickColors.length];
    bricks[i].drawBrick(color);
  }

  // Draw Next Brick
  bricks[placedBricks]?.drawBrick("chartreuse");

  // Draw Build Envelope
  fill('rgba(255, 204, 0, 0.3)');
  rectMode(CENTER);
  rect(mouseX, mouseY, envelopeWidth, envelopeHeight)
}

function getBricks() {
  fetch(currentBondUrl)
    .then(response => {
      if (!response.ok) {
        console.error("Not OK");
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      // Convert JSON bricks into Brick objects
      bricks = data.bricks.map(brick => new Brick(brick.x, brick.y, brick.width, brick.height, brick.strideIndex));
    })
    .catch(error => {
      console.error('Error fetching /stretcher-bond:', error);
    });
}

function invertY(y) {
  return canvasHeight - y;
}
