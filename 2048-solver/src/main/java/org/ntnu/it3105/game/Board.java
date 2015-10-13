package org.ntnu.it3105.game;

import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
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
     * Moves all tiles and promotes (to the power of 2) them if they are merged
     * @param direction The direction to move in
     */
    public void doMove(Direction direction) {
        log.debug("Moving in direction: " + direction);

        int[][] movedBoard = move(this.getCopyOfBoard(), direction);

        if (!Arrays.deepEquals(movedBoard, this.tiles)) {
            log.debug("Board state changed, did move and appending tile");
            this.tiles = movedBoard;
            addTile();
        } else {
            log.debug("Board state did not change, did not append tile");
        }

        this.canMove = isPossibleToMove();
        log.debug("Movement is possible: " + this.canMove);
    }

    /**
     * Add a single tile to the board
     * Choose a random position on the board with a new tile
     * The tile should be 2 or 4. Ensure a probability of 0.9 for 2 and 0.1 for 4
     */
    private void addTile() {
        List<Point> allFreeCells = getAllFreeCells(this.tiles);
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
     * Check if it is possible to make a move
     * @return Boolean stating wether or not a move is possible
     */
    private boolean isPossibleToMove() {
        if (getAllFreeCells(this.tiles).size() > 0) {
            return true;
        }

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
    public void printBoard() {
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
     * Returns a copy of the actual board
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

    /* ================ STATIC FUNCTIONS =================== */

    /**
     * Rotates the board on the right
     */
    private static int[][] rotateClockwise(int[][] board) {
        int[][] rotatedBoard = new int[BOARD_SIZE][BOARD_SIZE];

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                rotatedBoard[i][j] = board[BOARD_SIZE - j - 1][i];
            }
        }

        return rotatedBoard;
    }

    /**
     * Rotates the board on the left
     */
    private static int[][] rotateCounterClockwise(int[][] board) {
        int[][] rotatedBoard = new int[BOARD_SIZE][BOARD_SIZE];

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                rotatedBoard[BOARD_SIZE - j - 1][i] = board[i][j];
            }
        }

        return rotatedBoard;
    }

    /**
     * Returns a list of all possible positions to add a tile
     */
    public static List<Point> getAllFreeCells(int[][] board) {
        List<Point> freeCells = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == 0) {
                    freeCells.add(new Point(col, row));
                }
            }
        }
        return freeCells;
    }

    /**
     * Returns a copy of the board sent as parameter
     * @param board The board to copy
     * @return The copy of the board
     */
    public static int[][] getCopyOfBoard(int[][] board) {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            copy[i] = board[i].clone();
        }

        return copy;
    }

    /**
     * Moves the board paramenter in the parameter direction
     * @param board Board to move
     * @param direction Direction to move in
     * @return Board in moved state
     */
    public static int[][] move(int[][] board, Direction direction) {
        // Rotate the board to make simplify the merging algorithm
        switch (direction) {
            case UP:
                board = rotateCounterClockwise(board);
                break;
            case RIGHT:
                board = rotateCounterClockwise(board);
                board =rotateCounterClockwise(board);
                break;
            case DOWN:
                board = rotateClockwise(board);
                break;
            default:
                break;
        }

        for (int row = 0; row < BOARD_SIZE; row++) {

            int lastMergePosition = 0;

            for (int col = 1; col < BOARD_SIZE; col++) {

                if (board[row][col] == 0) {
                    continue;
                }

                int previousPosition = col - 1;

                while (previousPosition > lastMergePosition && board[row][previousPosition] == 0) { // Skip all unpopulated cells (> 0)
                    --previousPosition;
                }

                if (previousPosition == col) {
                    continue;
                } else if (board[row][previousPosition] == 0) {
                    // Moving to an empty cell
                    board[row][previousPosition] = board[row][col];
                    board[row][col] = 0;
                } else if (board[row][previousPosition] == board[row][col]) {
                    // Merging two matching cells
                    board[row][previousPosition] = board[row][col];
                    board[row][col] = 0;
                    board[row][previousPosition] *= 2;

                    lastMergePosition = previousPosition + 1;
                } else if (board[row][previousPosition] != board[row][col] && previousPosition + 1 != col) {
                    board[row][previousPosition + 1] = board[row][col];
                    board[row][col] = 0;
                }
            }
        }
        switch (direction) {
            case UP:
                board = rotateClockwise(board);
                break;
            case RIGHT:
                board = rotateClockwise(board);
                board = rotateClockwise(board);
                break;
            case DOWN:
                board = rotateCounterClockwise(board);
                break;
            default:
                break;
        }

        return board;
    }

}
