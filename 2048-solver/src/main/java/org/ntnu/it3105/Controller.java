package org.ntnu.it3105;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 07/10/15.
 * <p>
 */
public class Controller {

    private Logger log = Logger.getLogger(Controller.class);

    public static int BOARD_SIZE = 4;
    public static int GOAL_SIZE = 2048;

    //private @FXML AnchorPane root;
    private @FXML GridPane grid;
    private @FXML Label scoreLabel;
    private @FXML Label bestLabel;

    private int currentScore;
    private int[][] tiles = new int[BOARD_SIZE][BOARD_SIZE];

    /**
     * Intitalize method called when FXMLLoader loads the Board.fxml
     */
    public void initialize() {
        log.info("GameController initializing");

        scoreLabel.setText(String.valueOf(currentScore));

        initializeNewGame();
    }

    /**
     * Reset the board to a new state
     */
    private void initializeNewGame() {
        log.info("Initializing a new game, clearing board");
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col] = 0;
            }
        }

        currentScore = 0;
        scoreLabel.setText(String.valueOf(currentScore));

        addTile();
        addTile();

        //tiles[0][3] = 512;
        //tiles[3][1] = 512;

        redraw();
    }

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
            log.info("Game Over, not possible to add any more tiles to the board");
            log.debug("No free cells, not possible to add any tiles to the board");
            return;
        }

        int cellToPopulate = (int)(Math.random() * allFreeCells.size());
        Point cord = allFreeCells.get(cellToPopulate);

        log.debug("Adding tile to: (" + cord.x + ", " + cord.y + ") col/row");

        tiles[cord.y][cord.x] = Math.random() < 0.9 ? 2 : 4;
    }

    /**
     * Moves all tiles and promotes (to the power of 2) them if possible
     * @param direction
     */
    public void doMove(Direction direction) {
        log.info("Moving in direction: " + direction);

        int points = 0;

        //Rotate the board to make simplify the merging algorithm
        if (direction == Direction.UP) {
            rotateCounterClockwise();
        }
        else if (direction == Direction.RIGHT) {
            rotateCounterClockwise();
            rotateCounterClockwise();
        }
        else if (direction == Direction.DOWN) {
            rotateClockwise();
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

                    if (tiles[row][previousPosition] == GOAL_SIZE) {
                        log.info("REACHED GOAL AND WON THE GAME");
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
        scoreLabel.setText(String.valueOf(currentScore));

        //reverse back the board to the original orientation
        if (direction == Direction.UP) {
            rotateClockwise();
        }
        else if(direction == Direction.RIGHT) {
            rotateClockwise();
            rotateClockwise();
        }
        else if (direction == Direction.DOWN) {
            rotateCounterClockwise();
        }

        addTile();
        //printBoard();
        redraw();

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

    public void redraw() {
        log.info("Redrawing the GUI after move");
        grid.getChildren().clear();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Tile toAdd = new Tile(tiles[row][col]);
                grid.add(toAdd, col, row);
            }
        }
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
}
