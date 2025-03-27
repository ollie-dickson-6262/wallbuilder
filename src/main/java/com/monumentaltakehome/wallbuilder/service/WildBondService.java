package com.monumentaltakehome.wallbuilder.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.monumentaltakehome.wallbuilder.domain.Brick;

@Service
public class WildBondService implements WallService {

    // For Wild Bond this refers to the maximum number of full bricks between the edge half and three quarter bricks
    private static final int BRICKS_PER_ROW = 9;
    private static final double THREE_QUARTER_WIDTH = 150;

    private final int indexArraySize = 2 * BRICKS_PER_ROW + 1;
    private final int halfBricksFirstTry = 2;
    private final int fullBricksFirstTry = BRICKS_PER_ROW - halfBricksFirstTry/2;

    private final int maxConsecutiveHalfBricks = 3;
    private final int maxConsecutiveFullBricks = 5;
    private final boolean checkStaggeredSteps = true;
    private final boolean checkFallingTeeth = true;

    @Override
    public double wallWidth() {
        // 2240mm for 9 bricks per row
        return BRICKS_PER_ROW * (FULL_BRICK_WIDTH + HEAD_JOINT) + HALF_BRICK_WIDTH + THREE_QUARTER_WIDTH + HEAD_JOINT;
    }

    @Override
    public ArrayList<Brick> getFullBondMap() {
        if (BRICKS_PER_ROW % 1 != 0) {
            throw new IllegalStateException("Brick and joint widths will not fit the wall width!");
        }

        List<Row> rows = generateWildBondWithRetry();

        // Create bond map after successfull bond generation
        ArrayList<Brick> allBricks = new ArrayList<>();
        for (int i = 1; i <= ROW_QUANTITY; i++) {
            int rowNumber = i;
            List<PlannedBrick> nextPlannedRow = getNextWildRow(rowNumber, rows.get(i-1));
            List<Brick> nextRow = new ArrayList<>();
            for (int j = 0; j < nextPlannedRow.size(); j++) {
                int columnNumber = j;
                nextRow.add(createBrick(nextPlannedRow.get(j), rowNumber, columnNumber));
            }
            allBricks.addAll(nextRow);
        }
        return allBricks;

    }

    public List<Row> generateWildBondWithRetry() {
        for (int i=0; i < 100; i++) {
            try {
                return generateWildBond();
            } catch (FailedBondGenerationException e) {
                if (i >= 99) {
                    throw e;
                }
            }
        }
        return List.of();
    }

    // Produce a new row of random bricks that meets following conditions:
    // 1. No head joints directly on top of eachother (automatic)
    // 2. Check staggered steps (no more than 6)
    // 3. Check falling teeth (no more than 6)
    // 4. Maximum of 3 half bricks next to each other
    // 5. Maximum of 5 full bricks next to each other
    private List<Row> generateWildBond() {
        List<int[]> jointIndexRows = new ArrayList<>();
        List<Row> rows = new ArrayList<>();

        for (int rowNumber = 1; rowNumber <= ROW_QUANTITY; rowNumber++) {

            int adjacentChecks = 0;
            int belowRowChecks = 0;
            boolean validRow = false;
            List<BrickType> brickArray = new ArrayList<>();
            int[] newJointIndexRow = new int[indexArraySize];

            // Try for different quantities of half and full bricks
            for (int halfBrickQuant = halfBricksFirstTry; halfBrickQuant < fullBricksFirstTry; halfBrickQuant+= 2) {
                int fullBrickQuant = BRICKS_PER_ROW - halfBrickQuant/2;
                for (int i=0; i < 300; i++) {
                    brickArray = generateRandomBrickArray(halfBrickQuant, fullBrickQuant);
                    if (rowPassesAdjacentBrickConditions(brickArray)) {
                        newJointIndexRow = convertToJointIndexArray(brickArray);
                        if (rowPassesJointPatternConditions(newJointIndexRow, jointIndexRows)) {
                            validRow = true;
                            break;
                        } else {
                            belowRowChecks++;
                            if (adjacentChecks > 100) {
                                break;
                            }
                        }
                    } else {
                        adjacentChecks++;
                        if (adjacentChecks > 100) {
                            break;
                        }
                    }
                }
                if (validRow) {
                    break;
                }
            }

            if (validRow) {
                jointIndexRows.add(newJointIndexRow);
                rows.add(new Row(brickArray));
            } else {
                throw new FailedBondGenerationException(String.format("Failed to generate wild bond at row: %s, adjacentChecks: %s, belowRowChecks: %s", rowNumber, adjacentChecks, belowRowChecks));
            }
        }
        return rows;
    }

    private List<BrickType> generateRandomBrickArray(int halfBrickQuant, int fullBrickQuant) {
        List<BrickType> brickArray = new ArrayList<>();
        for (int i = 0; i < halfBrickQuant; i++) {
            brickArray.add(BrickType.HALF);
        }
        for (int i = 0; i < fullBrickQuant; i++) {
            brickArray.add(BrickType.FULL);
        }
        Collections.shuffle(brickArray);

        return brickArray;
    }

    private int[] convertToJointIndexArray(List<BrickType> brickArray) {
        int[] jointIndexArray = new int[indexArraySize];
        jointIndexArray[0] = 1;
        int j = 0;
        for (int i = 0; i < brickArray.size(); i++) {
            if (brickArray.get(i) == BrickType.HALF) {
                jointIndexArray[j+1] = 1;
                j++;
            } else {
                jointIndexArray[j+2] = 1;
                j += 2;
            }
        }
        return jointIndexArray;
    }

    private boolean rowPassesAdjacentBrickConditions(List<BrickType> brickTypes) {
        int consecutiveHalfBricks = 0;
        int consecutiveFullBricks = 0;
    
        for (BrickType brickType : brickTypes) {
            if (brickType == BrickType.HALF) {
                consecutiveHalfBricks++;
                consecutiveFullBricks = 0;
                if (consecutiveHalfBricks > maxConsecutiveHalfBricks) {
                    return false;
                }
            } else if (brickType == BrickType.FULL) {
                consecutiveFullBricks++;
                consecutiveHalfBricks = 0;
                if (consecutiveFullBricks > maxConsecutiveFullBricks) {
                    return false;
                }
            }
        }
    
        return true;
    }

    private boolean rowPassesJointPatternConditions(int[] newRow, List<int[]> existingRows) {
        if (checkStaggeredSteps && !rowPassesStaggeredStepCheck(newRow, existingRows)) {
            return false;
        }
        if (checkFallingTeeth && !rowPassesFallingTeethCheck(newRow, existingRows)) {
            return false;
        }
        return true;
    }

    private boolean rowPassesStaggeredStepCheck(int[] newRow, List<int[]> existingRows) {
        int numberOfExistingRows = existingRows.size();
        boolean oddRow = numberOfExistingRows % 2 == 0; // odd considering 1st row as odd (index 0)
        if (numberOfExistingRows >= 7 && newRow.length >= 4) {
            if (!rowPassesLeftStepCheck(newRow, existingRows, oddRow, numberOfExistingRows)) {
                return false;
            }
            if (!rowPassesRightStepCheck(newRow, existingRows, oddRow, numberOfExistingRows)) {
                return false;
            }
        }
        return true;
    }

    private boolean rowPassesLeftStepCheck(int[] newRow, List<int[]> existingRows, boolean oddRow, int numberOfExistingRows) {
        for (int i = 3; i < newRow.length; i++) {
            List<Integer> leftStepJointPositions = new ArrayList<>();
            if (oddRow) {
                leftStepJointPositions.add(newRow[i]);
                leftStepJointPositions.add(existingRows.getLast()[i]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 2)[i-1]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 3)[i-1]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 4)[i-2]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 5)[i-2]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 6)[i-3]);
            } else {
                leftStepJointPositions.add(newRow[i]);
                leftStepJointPositions.add(existingRows.getLast()[i-1]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 2)[i-1]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 3)[i-2]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 4)[i-2]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 5)[i-3]);
                leftStepJointPositions.add(existingRows.get(numberOfExistingRows - 6)[i-3]);
            }
            if (!leftStepJointPositions.stream().anyMatch(joint -> joint == 0)) {
                return false;
            }
        }
        return true;
    }

    private boolean rowPassesRightStepCheck(int[] newRow, List<int[]> existingRows, boolean oddRow, int numberOfExistingRows) {
        for (int i = 0; i < newRow.length-3; i++) {
            List<Integer> rightStepJointPositions = new ArrayList<>();
            if (oddRow) {
                rightStepJointPositions.add(newRow[i]);
                rightStepJointPositions.add(existingRows.getLast()[i+1]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 2)[i+1]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 3)[i+2]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 4)[i+2]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 5)[i+3]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 6)[i+3]);
            } else {
                rightStepJointPositions.add(newRow[i]);
                rightStepJointPositions.add(existingRows.getLast()[i]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 2)[i+1]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 3)[i+1]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 4)[i+2]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 5)[i+2]);
                rightStepJointPositions.add(existingRows.get(numberOfExistingRows - 6)[i+3]);
            }
            if (!rightStepJointPositions.stream().anyMatch(joint -> joint == 0)) {
                return false;
            }
        }
        return true;
    }

    private boolean rowPassesFallingTeethCheck(int[] newRow, List<int[]> existingRows) {
        int numberOfExistingRows = existingRows.size();
        boolean oddRow = numberOfExistingRows % 2 == 0; // odd considering 1st row as odd (index 0)

        if (numberOfExistingRows >= 6 && newRow.length >= 4) {
            //Check left teeth
            if (!rowPassesLeftTeethCheck(newRow, existingRows, oddRow, numberOfExistingRows)) {
                return false;
            }
            //Check right teeth
            if (!rowPassesRightTeethCheck(newRow, existingRows, oddRow, numberOfExistingRows)) {
                return false;
            }

        }
        return true;
    }

    private boolean rowPassesLeftTeethCheck(int[] newRow, List<int[]> existingRows, boolean oddRow, int numberOfExistingRows) {
        for (int i = 1; i < newRow.length-1; i++) {
            List<Integer> leftTeethJointPositions = new ArrayList<>();
            if (oddRow) {
                leftTeethJointPositions.add(newRow[i]);
                leftTeethJointPositions.add(existingRows.getLast()[i]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 2)[i]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 3)[i]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 4)[i]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 5)[i]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 6)[i]);
            } else {
                leftTeethJointPositions.add(newRow[i]);
                leftTeethJointPositions.add(existingRows.getLast()[i-1]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 2)[i]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 3)[i-1]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 4)[i]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 5)[i-1]);
                leftTeethJointPositions.add(existingRows.get(numberOfExistingRows - 6)[i]);
            }
            if (!leftTeethJointPositions.stream().anyMatch(joint -> joint == 0)) {
                return false;
            }
        }
        return true;
    }

    private boolean rowPassesRightTeethCheck(int[] newRow, List<int[]> existingRows, boolean oddRow, int numberOfExistingRows) {
        for (int i = 1; i < newRow.length-1; i++) {
            List<Integer> rightTeethJointPositions = new ArrayList<>();
            if (oddRow) {
                rightTeethJointPositions.add(newRow[i]);
                rightTeethJointPositions.add(existingRows.getLast()[i+1]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 2)[i]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 3)[i+1]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 4)[i]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 5)[i+1]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 6)[i]);
            } else {
                rightTeethJointPositions.add(newRow[i]);
                rightTeethJointPositions.add(existingRows.getLast()[i]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 2)[i]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 3)[i]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 4)[i]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 5)[i]);
                rightTeethJointPositions.add(existingRows.get(numberOfExistingRows - 6)[i]);
            }

            if (!rightTeethJointPositions.stream().anyMatch(joint -> joint == 0)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    public List<PlannedBrick> getNextWildRow(int rowNumber, Row brickRow) {
        List<BrickType> brickArray = brickRow.brickTypes;
        List<PlannedBrick> row = new ArrayList<>();
        double y = rowNumber * COURSE_HEIGHT;
        if (rowNumber % 2 == 0) {
            // Add Half brick to start
            double x = 0;
            row.add(new PlannedBrick(x, y, HALF_BRICK_WIDTH, BRICK_HEIGHT));
            x+= HALF_BRICK_WIDTH + HEAD_JOINT;
            // Add the randomly arranged bricks
            for (int i = 0; i < brickArray.size(); i++) {
                double brickWidth = brickArray.get(i) == BrickType.HALF
                    ? HALF_BRICK_WIDTH
                    : FULL_BRICK_WIDTH;
                row.add(new PlannedBrick(x, y, brickWidth, BRICK_HEIGHT));
                x += brickWidth + HEAD_JOINT;
            }
            // Add Three Quarter brick to end
            row.add(new PlannedBrick(x, y, THREE_QUARTER_WIDTH, BRICK_HEIGHT));
        } else {
            // Add Three Quarter brick to start
            double x = 0;
            row.add(new PlannedBrick(x, y, THREE_QUARTER_WIDTH, BRICK_HEIGHT));
            x+= THREE_QUARTER_WIDTH + HEAD_JOINT;
            // Add the randomly arranged bricks
            for (int i = 0; i < brickArray.size(); i++) {
                double brickWidth = brickArray.get(i) == BrickType.HALF
                    ? HALF_BRICK_WIDTH
                    : FULL_BRICK_WIDTH;
                row.add(new PlannedBrick(x, y, brickWidth, BRICK_HEIGHT));
                x += brickWidth + HEAD_JOINT;
            }
            // Add Half brick to end 
            row.add(new PlannedBrick(x, y, HALF_BRICK_WIDTH, BRICK_HEIGHT));
        }
        return row;
    }

    @Override
    public List<PlannedBrick> getNextRow(int rowNumber) {
        // Method not required for Wild Bond
        return List.of();
    }

    private enum BrickType {
        FULL,
        HALF
    }

    private record Row(List<BrickType> brickTypes) {}


    public class FailedBondGenerationException extends RuntimeException {
        public FailedBondGenerationException(String message) {
            super(message);
        }
    }
}
