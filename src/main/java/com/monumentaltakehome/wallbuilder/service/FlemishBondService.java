package com.monumentaltakehome.wallbuilder.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FlemishBondService implements WallService {
    
    // For flemish bond this refers to the number of full and half bricks, excluding queen closer bricks
    // Only supports odd numbers
    private static final double BRICKS_PER_ROW = 13;
    private static final double QUEEN_CLOSER = (FULL_BRICK_WIDTH/2 - HALF_BRICK_WIDTH/2) - HEAD_JOINT;

    @Override
    public double wallWidth() {
        // 2190mm for 13 bricks per row;
        return BRICKS_PER_ROW * (HALF_BRICK_WIDTH + FULL_BRICK_WIDTH + 2 * HEAD_JOINT) / 2;
    }

    @SuppressWarnings("unused")
    @Override
    public List<PlannedBrick> getNextRow(int rowNumber) {
        if (BRICKS_PER_ROW % 1 != 0 || BRICKS_PER_ROW % 2 == 0) {
            throw new IllegalStateException("Brick and joint widths will not fit the wall width!");
        }
        List<PlannedBrick> row = new ArrayList<>();
        double y = rowNumber * COURSE_HEIGHT;
        if (rowNumber % 2 == 0) {
            // Add Half brick and Queen Closer brick to start
            double x = 0;
            row.add(new PlannedBrick(x, y, HALF_BRICK_WIDTH, BRICK_HEIGHT));
            x += HALF_BRICK_WIDTH + HEAD_JOINT;
            row.add(new PlannedBrick(x, y, QUEEN_CLOSER, BRICK_HEIGHT));
            x += QUEEN_CLOSER + HEAD_JOINT;
            // add consecutive full and half bricks
            for (int i=0; i<BRICKS_PER_ROW-2; i++) {
                if (i % 2 != 0) {
                    row.add(new PlannedBrick(x, y, HALF_BRICK_WIDTH, BRICK_HEIGHT));
                    x += HALF_BRICK_WIDTH + HEAD_JOINT;
                } else {
                    row.add(new PlannedBrick(x, y, FULL_BRICK_WIDTH, BRICK_HEIGHT));
                    x += FULL_BRICK_WIDTH + HEAD_JOINT;
                }
            }
            // add Queen Closer and Half brick to finish
            row.add(new PlannedBrick(x, y, QUEEN_CLOSER, BRICK_HEIGHT));
            x += QUEEN_CLOSER + HEAD_JOINT;
            row.add(new PlannedBrick(x, y, HALF_BRICK_WIDTH, BRICK_HEIGHT));
        } else {
            // add consecutive full and half bricks
            double x = 0;
            for (int i=0; i<BRICKS_PER_ROW; i++) {
                if (i % 2 != 0) {
                    row.add(new PlannedBrick(x, y, HALF_BRICK_WIDTH, BRICK_HEIGHT));
                    x += HALF_BRICK_WIDTH + HEAD_JOINT;
                } else {
                    row.add(new PlannedBrick(x, y, FULL_BRICK_WIDTH, BRICK_HEIGHT));
                    x += FULL_BRICK_WIDTH + HEAD_JOINT;
                }
            }
            
        }
        return row;
    }

}
