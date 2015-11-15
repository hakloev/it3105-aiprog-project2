package org.ntnu.it3105.game;
import static org.ntnu.it3105.ai.Expectimax.STATISTICS_SCRAPPER;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;


import java.util.concurrent.*;

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
    private Board drawingboard;
    private Record recordManager;
    private ExecutorService es;

    /**
     * Initialize method called when FXMLLoader loads the Board.fxml
     */
    public void initialize() {
        log.info("GameController initializing");
        board = new Board();
        board.initializeNewGame();
        es = Executors.newSingleThreadExecutor();
        if (!STATISTICS_SCRAPPER) {
            drawingboard = new Board(board.getCopyOfBoard());
            recordManager = Record.getInstance();
            redraw(drawingboard.getBoard());
        }
    }

    /**
     * Delegates the movement to the board if possible
     * @param directionToMove The direction to move
     */
    public void doMove(Direction directionToMove) {
        if (!board.hasWon() && board.canMove()) {
            board.doMove(directionToMove);
            if (!STATISTICS_SCRAPPER) {
                recordManager.saveRecord(board.getCurrentScore());
                redraw(board.getBoard());
            }
        }
    }

    /**
     * Redraw the entire GridPane with new tiles
     */
    public void redraw(int[][] board) {
        grid.getChildren().clear();

        scoreLabel.setText(String.valueOf(this.board.getCurrentScore()));
        bestLabel.setText(String.valueOf(recordManager.getRecord()));

        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                Tile toAdd = new Tile(board[row][col]);
                grid.add(toAdd, col, row);
            }
        }
    }

    /**
     * Resets the board
     */
    public void reset() {
        this.board.initializeNewGame();
        if (!STATISTICS_SCRAPPER) {
            this.drawingboard = new Board(this.board.getCopyOfBoard());
            redraw(this.drawingboard.getBoard());
        }
    }

    /**
     * Starts the solve loop
     * @param r The Runnable job
     */
    public void startSolveLoop(Runnable r) {
        this.es.execute(r);
    }

    /**
     * Get the board reference
     * @return The board reference
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Shuts down the Controller gracefully
     */
    public void shutdown() {
        this.es.shutdown();
        try {
            this.es.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted during ExecutorService shutdown");
        }
    }
}
