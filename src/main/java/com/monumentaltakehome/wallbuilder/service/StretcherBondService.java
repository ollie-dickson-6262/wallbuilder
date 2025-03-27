package com.monumentaltakehome.wallbuilder.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

@Service
public class StretcherBondService implements WallService {

    private static final double BRICKS_PER_ROW = 10.5;

    @Override
    public double wallWidth() {
        // 2300mm for 10.5 bricks per row
        return BRICKS_PER_ROW * COURSE_WIDTH - HEAD_JOINT;
    }

    @SuppressWarnings("unused")
    @Override
    public List<PlannedBrick> getNextRow(int rowNumber) {
        if (BRICKS_PER_ROW % 0.5 != 0) {
            throw new IllegalStateException("Brick and joint widths will not fit the wall width!");
        }
        if (BRICKS_PER_ROW % 1 == 0) {
            if (rowNumber % 2 == 0) {
                List<PlannedBrick> row = new ArrayList<>();
                row.add(new PlannedBrick(0, rowNumber * COURSE_HEIGHT, HALF_BRICK_WIDTH, BRICK_HEIGHT));
                row.addAll(IntStream.range(0, (int) BRICKS_PER_ROW - 1)
                    .mapToDouble(i -> COURSE_WIDTH * (double) i + HALF_BRICK_WIDTH + HEAD_JOINT)
                    .boxed()
                    .map(x -> new PlannedBrick(x, rowNumber * COURSE_HEIGHT, FULL_BRICK_WIDTH, BRICK_HEIGHT))
                    .toList());
                row.add(new PlannedBrick(COURSE_WIDTH * (double) (BRICKS_PER_ROW - 1) + HALF_BRICK_WIDTH + HEAD_JOINT, rowNumber * COURSE_HEIGHT, HALF_BRICK_WIDTH, BRICK_HEIGHT));
                return row;
            } else {
                return IntStream.range(0, (int) BRICKS_PER_ROW)
                    .mapToDouble(i -> COURSE_WIDTH * (double) i)
                    .boxed()
                    .map(x -> new PlannedBrick(x, rowNumber * COURSE_HEIGHT, FULL_BRICK_WIDTH, BRICK_HEIGHT))
                    .toList();
            }
        } else {
            List<PlannedBrick> row = new ArrayList<>();
            int fullBricksPerRow = (int) Math.floor(BRICKS_PER_ROW);
            if (rowNumber % 2 == 0) {
                row.add(new PlannedBrick(0, rowNumber * COURSE_HEIGHT, HALF_BRICK_WIDTH, BRICK_HEIGHT));
                row.addAll(IntStream.range(0, fullBricksPerRow)
                    .mapToDouble(i -> COURSE_WIDTH * (double) i + HALF_BRICK_WIDTH + HEAD_JOINT)
                    .boxed()
                    .map(x -> new PlannedBrick(x, rowNumber * COURSE_HEIGHT, FULL_BRICK_WIDTH, BRICK_HEIGHT))
                    .toList());
            } else {
                row.addAll(IntStream.range(0, fullBricksPerRow)
                    .mapToDouble(i -> COURSE_WIDTH * (double) i)
                    .boxed()
                    .map(x -> new PlannedBrick(x, rowNumber * COURSE_HEIGHT, FULL_BRICK_WIDTH, BRICK_HEIGHT))
                    .toList());
                row.add(new PlannedBrick(COURSE_WIDTH * (double) fullBricksPerRow, rowNumber * COURSE_HEIGHT, HALF_BRICK_WIDTH, BRICK_HEIGHT));
            }
            return row;
        }
    }
}
