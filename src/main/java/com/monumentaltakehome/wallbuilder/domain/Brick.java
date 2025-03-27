package com.monumentaltakehome.wallbuilder.domain;

import java.util.List;
import java.util.stream.Stream;

public record Brick(double x, double y, double width, double height, boolean placed, int rowNumber, int columnNumber) {

    public boolean isFoundationOf(Brick otherBrick) {
        double thisLeftX = this.x;
        double thisRightX = this.x + this.width;
        double otherBrickLeftX = otherBrick.x();
        double otherBrickRightX = otherBrick.x() + otherBrick.width();
        // Check if the two bricks overlap horizontally
        return thisLeftX < otherBrickRightX && otherBrickLeftX < thisRightX;
    }

    public boolean isPlaceable(List<Brick> allBricks, boolean onlyPlaceAfterLeftNeigbour) {
        return onlyPlaceAfterLeftNeigbour 
            ? isPlaceableWithLeftNeighbour(allBricks)
            : isPlaceable(allBricks);
    }

    // isPlaceable considering only bricks below:
    private boolean isPlaceable(List<Brick> allBricks) {
        List<Brick> unPlacedFoundationBricks = allBricks.stream()
            .filter(brick -> brick.rowNumber() == this.rowNumber - 1)
            .filter(brick -> brick.isFoundationOf(this))
            .filter(brick -> !brick.placed())
            .toList();

        return this.placed == false && unPlacedFoundationBricks.isEmpty();
    }

    // isPlaceable considering bricks below and to the left:
    private boolean isPlaceableWithLeftNeighbour(List<Brick> allBricks) {
        Stream<Brick> unPlacedFoundationBricks = allBricks.stream()
            .filter(brick -> brick.rowNumber() == this.rowNumber - 1)
            .filter(brick -> brick.isFoundationOf(this))
            .filter(brick -> !brick.placed());
        Stream<Brick> leftNeighbour = allBricks.stream()
            .filter(brick -> brick.rowNumber() == this.rowNumber)
            .filter(brick -> brick.columnNumber() == this.columnNumber - 1)
            .filter(brick -> !brick.placed());

        List<Brick> unplacedDependantBricks = Stream.concat(unPlacedFoundationBricks, leftNeighbour).toList();

        return this.placed == false && unplacedDependantBricks.isEmpty();
    }

    public Brick place() {
        return new Brick(this.x, this.y, this.width, this.height, true, this.rowNumber, this.columnNumber);
    }

}
