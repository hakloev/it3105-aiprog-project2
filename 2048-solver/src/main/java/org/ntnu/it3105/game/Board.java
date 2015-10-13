package org.ntnu.it3105.game;

import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 13/10/15.
 * <p>
 */
public class Board {

    private Logger log = Logger.getLogger(Controller.class);

    public static int BOARD_SIZE = 4;
    public static int TARGET_VALUE = 2048;

    private boolean hasWon;
    private boolean canMove;
    private int currentScore;
    private int[][] tiles = new int[BOARD_SIZE][BOARD_SIZE];


    /**
     * Returns a list of all possible positions to add a tile
     */
    private List<Point> getAllFreeCells() {
        List<Point> freeCells = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (tiles[row][col] == 0) {
                    freeCells.add(new Point(col, row));
                }
            }
        }
        return freeCells;
    }

    /**
     * Add a single tile to the board
     * Choose a random position on the board with a new tile
     * The tile should be 2 or 4. Ensure a probability of 0.9 for 2 and 0.1 for 4
     */
    private void addTile() {
        List<Point> allFreeCells = getAllFreeCells();
        if (allFreeCells.size() == 0) {
            if (!isPossibleToMove()) {
                log.info("It is not possible to move, setting canMove to false. GAME OVER");
                canMove = false;
                return;
            }
            log.debug("Possible to merge cells, but full board");
            return;
        }

        int cellToPopulate = (int)(Math.random() * allFreeCells.size());
        Point cord = allFreeCells.get(cellToPopulate);
        log.debug("Adding tile to: (" + cord.x + ", " + cord.y + ") col/row");
        tiles[cord.y][cord.x] = Math.random() < 0.9 ? 2 : 4;
    }

    /**
     * Moves all tiles and promotes (to the power of 2) them if they are merged
     * @param direction The direction to move in
     */
    public void doMove(Direction direction) {
        log.debug("Moving in direction: " + direction);

        int points = 0;

        // Rotate the board to make simplify the merging algorithm
        switch (direction) {
            case UP:
                rotateCounterClockwise();
                break;
            case RIGHT:
                rotateCounterClockwise();
                rotateCounterClockwise();
                break;
            case DOWN:
                rotateClockwise();
                break;
            default:
                break;
        }

        for (int row = 0; row < BOARD_SIZE; row++) {

            int lastMergePosition = 0;

            for (int col = 1; col < BOARD_SIZE; col++) {

                if (tiles[row][col] == 0) {
                    log.debug("We do not move empty cells");
                    continue;
                }

                int previousPosition = col - 1;

                while (previousPosition > lastMergePosition && tiles[row][previousPosition] == 0) { // Skip all unpopulated cells (> 0)
                    --previousPosition;
                }

                if (previousPosition == col) {
                    log.debug("This cell can not be moved, same cell");
                } else if (tiles[row][previousPosition] == 0) {
                    // Moving to an empty cell
                    log.debug("Moving to an empty cell (" + row + ", " + previousPosition + ")");
                    tiles[row][previousPosition] = tiles[row][col];
                    tiles[row][col] = 0;
                } else if (tiles[row][previousPosition] == tiles[row][col]) {
                    // Merging two matching cells
                    tiles[row][previousPosition] = tiles[row][col];
                    tiles[row][col] = 0;
                    tiles[row][previousPosition] *= 2;
                    log.debug("Merging with matching value, new value is: " + tiles[row][previousPosition] + " and position is (" + row + ", " + previousPosition + ")");

                    points += tiles[row][previousPosition];

                    if (tiles[row][previousPosition] == TARGET_VALUE) {
                        hasWon = true;
                        log.info("Reached the target value, setting hasWon to true");
                    }

                    lastMergePosition = previousPosition + 1;
                } else if (tiles[row][previousPosition] != tiles[row][col] && previousPosition + 1 != col) {
                    log.debug("");
                    tiles[row][previousPosition + 1] = tiles[row][col];
                    tiles[row][col] = 0;
                }
            }
        }

        currentScore += points;

        switch (direction) {
            case UP:
                rotateClockwise();
                break;
            case RIGHT:
                rotateClockwise();
                rotateClockwise();
                break;
            case DOWN:
                rotateCounterClockwise();
                break;
            default:
                break;
        }

        addTile();
    }

    /**
     * Rotates the board on the right
     */
    private void rotateClockwise() {
        int[][] rotatedBoard = new int[BOARD_SIZE][BOARD_SIZE];

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                rotatedBoard[i][j]=tiles[BOARD_SIZE - j - 1][i];
            }
        }

        tiles = rotatedBoard;
    }

    /**
     * Rotates the board on the left
     */
    private void rotateCounterClockwise() {
        int[][] rotatedBoard = new int[BOARD_SIZE][BOARD_SIZE];

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                rotatedBoard[BOARD_SIZE - j - 1][i] = tiles[i][j];
            }
        }

        tiles = rotatedBoard;
    }

    /**
     * Check if it is possible to make a move
     * @return Boolean stating wether or not a move is possible
     */
    private boolean isPossibleToMove() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int value = tiles[row][col];
                if ((row < 3 && value == tiles[row + 1][col]) || ((col < 3) && value == tiles[row][col + 1])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Prints the board to the console
     */
    private void printBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (tiles[row][col] == 0) {
                    System.out.print("#");
                } else {
                    System.out.print(tiles[row][col]);
                }
            }
            System.out.println();
        }
        System.out.println("\n");
    }

    /**
     * Returns the original board
     * @return The board
     */
    public int[][] getBoard() {
        return tiles;
    }

    /**
     * Returns a copy of the board
     * @return The copy of the board
     */
    public int[][] getCopyOfBoard() {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            copy[i] = tiles[i].clone();
        }

        return copy;
    }

    /**
     * Reset the board to a new state
     */
    public void initializeNewGame() {
        log.info("Initializing a new game, clearing the board and adding two random tiles");
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col] = 0;
            }
        }

        currentScore = 0;

        hasWon = false;
        canMove = true;

        addTile();
        addTile();
    }

    /**
     * Returns the current score
     * @return the current score
     */
    public int getCurrentScore() {
        return currentScore;
    }

    /**
     * Returns is a move is possible
     * @return Boolean with movement value
     */
    public boolean canMove() {
        return canMove;
    }

    /**
     * Returns if the game is won
     * @return Boolean with the status
     */
    public boolean hasWon() {
        return hasWon;
    }
}
