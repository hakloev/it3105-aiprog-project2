package org.ntnu.it3105.game;

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

    private @FXML GridPane grid;
    private @FXML Label scoreLabel; // Current score
    private @FXML Label bestLabel; // Best score over time

    private Board board;

    /**
     * Initialize method called when FXMLLoader loads the Board.fxml
     */
    public void initialize() {
        log.info("GameController initializing");
        board = new Board();
        board.initializeNewGame();
        redraw(board.getBoard());
    }

    /**
     * Delegates the movement to the board if possible
     * @param directionToMove
     */
    public void doMove(Direction directionToMove) {
        if (!board.hasWon() && board.canMove()) {
            board.doMove(directionToMove);
            redraw(board.getBoard());
        }
    }

    /**
     * Redraw the entire GridPane with new tiles
     */
    public void redraw(int[][] board) {
        log.debug("Redrawing the GUI");
        grid.getChildren().clear();

        scoreLabel.setText(String.valueOf(this.board.getCurrentScore()));

        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                Tile toAdd = new Tile(board[row][col]);
                grid.add(toAdd, col, row);
            }
        }
    }

    /**
     * Get the board reference
     * @return The board reference
     */
    public Board getBoard() {
        return board;
    }

}
