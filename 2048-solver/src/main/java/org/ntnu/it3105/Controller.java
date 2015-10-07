package org.ntnu.it3105;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 07/10/15.
 * <p>
 */
public class Controller {

    private Logger log = Logger.getLogger(Controller.class);

    public static int BOARD_HEIGHT = 4;
    public static int BOARD_WIDTH = 4;

    //private @FXML AnchorPane root;
    private @FXML GridPane grid;
    private @FXML Label scoreLabel;

    private int currentScore;
    private Tile[][] tiles = new Tile[BOARD_HEIGHT][BOARD_WIDTH];

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
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                grid.getChildren().remove(tiles[x][y]);
                tiles[y][x] = null;
            }
        }

        currentScore = 0;
        scoreLabel.setText(String.valueOf(currentScore));

        addTile();
        //addTile();
    }

    private int curr_x = 1, curr_y = 2;

    /**
     * Add a single tile to the board
     * Choose a random position on the board with a new tile
     * The tile should be 2 or 4. Ensure a probability of 0.9 for 2 and 0.1 for 4
     */
    private void addTile() {
        // TODO: GET LIST OF POSSIBLE POSITIONS
        // TODO: ADD TILE TO POSITION

        tiles[curr_x][curr_y] = Math.random() < 0.9 ? new Tile(2) : new Tile(4);
        grid.add(tiles[curr_x][curr_y], curr_x, curr_y);
    }


    /**
     * Moves all tiles and promotes (to the power of 2) them if possible
     * @param direction
     */
    public void doMove(Direction direction) {
        log.info("Moving in direction: " + direction);
        // TODO: MOVE ALL TILES, NOT JUST THIS HARDCODED ONE
        int from_x = curr_x, from_y = curr_y;
        int to_x = (from_x + direction.x), to_y = (from_y + direction.y);

        grid.getChildren().remove(tiles[from_x][from_y]);
        grid.add(tiles[from_x][from_y], to_x, to_y);
        tiles[to_x][to_y] = tiles[from_x][from_y];
        tiles[from_x][from_y] = null;

        curr_x = to_x;
        curr_y = to_y;
    }
}
