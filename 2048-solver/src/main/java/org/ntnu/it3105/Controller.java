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

    //private @FXML AnchorPane root;
    private @FXML GridPane grid;
    private @FXML Label scoreLabel;

    private int currentScore;
    private Tile[][] tiles = new Tile[BOARD_SIZE][BOARD_SIZE];

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
                grid.getChildren().remove(tiles[row][col]);
                tiles[row][col] = null;
            }
        }

        currentScore = 0;
        scoreLabel.setText(String.valueOf(currentScore));

        addTile();
        addTile();

        printBoard();
    }

    /**
     * Returns a list of all possible positions to add a tile
     */
    private List<Point> getAllFreeCells() {
        List<Point> freeCells = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (tiles[row][col] == null) {
                    freeCells.add(new Point(col, row));
                }
            }
        }
        // TODO: If list empty, game is lost
        return freeCells;
    }

    /**
     * Add a single tile to the board
     * Choose a random position on the board with a new tile
     * The tile should be 2 or 4. Ensure a probability of 0.9 for 2 and 0.1 for 4
     */
    private void addTile() {
        List<Point> allFreeCells = getAllFreeCells();
        int cellToPopulate = (int)(Math.random() * allFreeCells.size());
        Point cord = allFreeCells.get(cellToPopulate);

        log.debug("Adding tile to: (" + cord.x + ", " + cord.y + ") col/row");

        tiles[cord.y][cord.x] = Math.random() < 0.9 ? new Tile(2) : new Tile(4);
        grid.add(tiles[cord.y][cord.x], cord.x, cord.y); /* The grid coordinates in the GridPane is reversed in the parameters */
    }

    /**
     * Rotates the board on the left
     */
    private void rotateLeft() {
        Tile[][] rotatedBoard = new Tile[BOARD_SIZE][BOARD_SIZE];

        for(int row = 0; row < BOARD_SIZE; row++) {
            for(int col = 0 ; col < BOARD_SIZE; col++) {
                rotatedBoard[row][col] = tiles[col][BOARD_SIZE - row - 1];
            }
        }

        tiles = rotatedBoard;
    }

    /**
     * Rotates the board on the right
     */
    private void rotateRight() {
        Tile[][] rotatedBoard = new Tile[BOARD_SIZE][BOARD_SIZE];

        for(int row = 0; row < BOARD_SIZE; row++) {
            for(int col = 0; col < BOARD_SIZE; col++) {
                rotatedBoard[row][col] = tiles[col][row];
            }
        }
        tiles = rotatedBoard;
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
            rotateRight();
        }
        else if (direction == Direction.RIGHT) {
            rotateLeft();
            rotateLeft();
        }
        else if (direction == Direction.DOWN) {
            rotateRight();
        }

        for (int row = 0; row < BOARD_SIZE; row++) {

            int lastMergePosition = 0;

            for (int j = 1; j < BOARD_SIZE; ++j) {

                if (tiles[row][j] == null) {
                    continue; //skip moving zeros
                }

                int previousPosition = j - 1;

                while (previousPosition > lastMergePosition && tiles[row][previousPosition] == null) { //skip all the zeros
                    --previousPosition;
                }

                if (previousPosition == j) {
                    //we can't move this at all
                } else if (tiles[row][previousPosition] == null) {
                    //move to empty value
                    log.debug("Move to empty cell");
                    tiles[row][previousPosition] = tiles[row][j];
                    tiles[row][j] = null;
                } else if (tiles[row][previousPosition].getValue() == tiles[row][j].getValue()) {
                    //merge with matching value
                    log.debug("Merge with matching value");
                    tiles[row][j].increaseValue();
                    tiles[row][previousPosition] = tiles[row][j];
                    tiles[row][j] = null;

                    points += tiles[row][previousPosition].getValue();
                    lastMergePosition = previousPosition + 1;

                } else if (tiles[row][previousPosition] != tiles[row][j] && previousPosition + 1 != j) {
                    log.debug("DERP ");
                    tiles[row][previousPosition + 1] = tiles[row][j];
                    tiles[row][j] = null;
                }
            }
        }

        currentScore += points;
        scoreLabel.setText(String.valueOf(currentScore));


        //reverse back the board to the original orientation
        if (direction == Direction.UP) {
            rotateRight();
        }
        else if(direction == Direction.RIGHT) {
            rotateLeft();
            rotateLeft();
        }
        else if (direction == Direction.DOWN) {
            rotateLeft();
        }

        addTile();
        printBoard();

        // ADD TILE OF NOT FINISHED
        /*
        // TODO: MOVE ALL TILES, NOT JUST THIS HARDCODED ONE
        int from_x = curr_x, from_y = curr_y;
        int to_x = (from_x + direction.x), to_y = (from_y + direction.y);

        grid.getChildren().remove(tiles[from_x][from_y]);
        grid.add(tiles[from_x][from_y], to_x, to_y);
        tiles[to_x][to_y] = tiles[from_x][from_y];
        tiles[from_x][from_y] = null;

        curr_x = to_x;
        curr_y = to_y;
        */

    }

    private void printBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (tiles[row][col] == null) {
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
