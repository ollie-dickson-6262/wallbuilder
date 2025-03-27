package com.monumentaltakehome.wallbuilder.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.monumentaltakehome.wallbuilder.domain.Brick;
import com.monumentaltakehome.wallbuilder.response.BrickDto;
import com.monumentaltakehome.wallbuilder.response.WallDto;

public interface WallService {

    public static final double FULL_BRICK_WIDTH = 210;
    public static final double HALF_BRICK_WIDTH = 100;
    public static final double BRICK_HEIGHT = 50;
    public static final double HEAD_JOINT = 10;
    public static final double BED_JOINT = 12.5;
    public static final double COURSE_WIDTH = FULL_BRICK_WIDTH + HEAD_JOINT;
    public static final double COURSE_HEIGHT = BRICK_HEIGHT + BED_JOINT;

    // Toggle this field to change the constraints of brick placement
    public final boolean onlyPlaceAfterLeftNeigbour = true;

    public static final int BUILD_ENV_WIDTH = 800;
    public static final int BUILD_ENV_HEIGHT = 1300;

    public static final double WALL_HEIGHT = 2000;

    public static final double ROW_QUANTITY = WALL_HEIGHT / COURSE_HEIGHT;

    double wallWidth();

    List<PlannedBrick> getNextRow(int rowNumber);

    @SuppressWarnings("unused")
    default WallDto generateWall() {
        if (WALL_HEIGHT % COURSE_HEIGHT != 0) {
            throw new IllegalStateException("Brick and joint heights will not fit the wall height!");
        }

        ArrayList<Brick> allBricks = getFullBondMap();

        List<Stride> strides = new ArrayList<>();

        Optional<Double> minY = getMinY(allBricks);
        
        while (minY.isPresent()) {
            // Find optimum buildEnvX at this Y to maximise stride size
            double buildEnvX = findOptimumBuildEnvX(minY.get(), allBricks);
            Stride nextStride = getNextStride(allBricks, buildEnvX, minY.get());
            strides.add(nextStride);
            minY = getMinY(allBricks);
        }

        List<BrickDto> brickDtos = new ArrayList<>();
        for (int i=0; i<strides.size(); i++) {
            int strideIndex = i;
            brickDtos.addAll(strides.get(i).bricks.stream()
                .map(brick -> new BrickDto(brick.x(), brick.y(), brick.width(), brick.height(), strideIndex))
                .toList());
        }
        return new WallDto(brickDtos);
    }

    private Double findOptimumBuildEnvX(double buildEnvY, List<Brick> allBricks) {
        double bestX = 0;
        int maxStride = 0;
        for (int x = 0; x < wallWidth() - BUILD_ENV_WIDTH; x += HALF_BRICK_WIDTH) {
            ArrayList<Brick> allBricksCopy = new ArrayList<>(allBricks);
            Stride stride = getNextStride(allBricksCopy, x, buildEnvY);
            if (stride.bricks.size() > maxStride) {
                bestX = x;
                maxStride = stride.bricks.size();
            }
        }
        return bestX;
    }

    private Optional<Double> getMinY(List<Brick> allBricks) {
        return allBricks.stream().filter(brick -> brick.isPlaceable(allBricks, onlyPlaceAfterLeftNeigbour)).map(Brick::y).min(Double::compare);
    }

    default ArrayList<Brick> getFullBondMap() {
        ArrayList<Brick> allBricks = new ArrayList<>();
        for (int i = 1; i <= ROW_QUANTITY; i++) {
            int rowNumber = i;
            List<PlannedBrick> nextPlannedRow = getNextRow(i);
            List<Brick> nextRow = new ArrayList<>();
            for (int j = 0; j < nextPlannedRow.size(); j++) {
                int columnNumber = j;
                nextRow.add(createBrick(nextPlannedRow.get(j), rowNumber, columnNumber));
            }
            allBricks.addAll(nextRow);
        }
        return allBricks;
    }

    default Brick createBrick(PlannedBrick plannedBrick, int rowNumber, int columnNumber) {
        return new Brick(plannedBrick.x(), plannedBrick.y(), plannedBrick.width(), plannedBrick.height(), false, rowNumber, columnNumber);
    }

    private Stride getNextStride(ArrayList<Brick> allBricks, double buildEnvX, double buildEnvY) {
        List<Brick> nextStride = new ArrayList<>();
        List<Brick> bricksWithinEnvelope = getBricksWithinEnvelope(allBricks, buildEnvX, buildEnvY);
        
        while(containsPlaceableBrick(bricksWithinEnvelope, allBricks)) {
            Optional<Brick> placeableBrick = bricksWithinEnvelope.stream()
                .filter(brick -> brick.isPlaceable(allBricks, onlyPlaceAfterLeftNeigbour)).findFirst();

            if (placeableBrick.isPresent()) {
                Brick brick = placeableBrick.get();

                // Add the placeable brick to the nextStride
                nextStride.add(brick);

                // Update the brick in allBricks as placed
                int index = allBricks.indexOf(brick);
                if (index != -1) {
                    allBricks.set(index, brick.place());
                }

                // Refresh bricksWithinEnvelope to reflect the updated state
                bricksWithinEnvelope = getBricksWithinEnvelope(allBricks, buildEnvX, buildEnvY);
            }
        }

        return new Stride(nextStride);
    }

    private List<Brick> getBricksWithinEnvelope(List<Brick> bricks, double buildEnvX, double buildEnvY) {
        return bricks.stream()
            .filter(brick -> brick.x() >= buildEnvX && brick.x() <= buildEnvX + BUILD_ENV_WIDTH)
            .filter(brick -> brick.y() >= buildEnvY && brick.y() <= buildEnvY + BUILD_ENV_HEIGHT)
            .toList();
    }

    private boolean containsPlaceableBrick(List<Brick> bricks, List<Brick> allBricks) {
        return bricks.stream().anyMatch(brick -> brick.isPlaceable(allBricks, onlyPlaceAfterLeftNeigbour));
    }
    
    public record PlannedBrick(double x, double y, double width, double height) {}

    public record Stride(List<Brick> bricks) {}
}
