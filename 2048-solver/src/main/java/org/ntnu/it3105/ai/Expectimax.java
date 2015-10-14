package org.ntnu.it3105.ai;

import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.ntnu.it3105.game.Board;
import org.ntnu.it3105.game.Controller;
import org.ntnu.it3105.game.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

import static org.ntnu.it3105.game.Board.*;


/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 12/10/15.
 * <p>
 */
public class Expectimax implements Solver {

    private Logger log = Logger.getLogger(Expectimax.class);

    private Controller controller;
    private ExecutorService es;
    private int depthLimit;

    public Expectimax(Controller controller, int depthLimit) {
        log.info("Starting Expectimax solver with " + Runtime.getRuntime().availableProcessors() + " core threads");
        this.controller = controller;
        this.depthLimit = depthLimit;
        // Let our thread pool consist of one thread per available processor core
        this.es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Creates a search in all four directions and returns the best direction
     * @return The direction to move in
     */
    public Direction getNextMove() {

        double bestValue = 0.0;
        Direction bestDirection = Direction.UP;

        /**
         * The DirectionValueTuple is used as a return value from the async directional search threads
         */
        class DirectionValueTuple {

            Direction dir;
            Double value;

            public DirectionValueTuple(Direction d, Double v) {
                this.dir = d;
                this.value = v;
            }
        }

        // Set up our 4 different search direction tasks
        ArrayList<Callable<DirectionValueTuple>> tasks = new ArrayList<>();
        for (Direction d : Direction.values()) {
            tasks.add(() -> {
                int[][] boardCopy = controller.getBoard().getCopyOfBoard(); // Get copy of the current state in the actual board.
                int[][] movedBoard = Board.move(boardCopy, d);

                DirectionValueTuple result = new DirectionValueTuple(d, 0.0);

                if (!Arrays.deepEquals(movedBoard, boardCopy)) {
                    result.value = expectimax(movedBoard, depthLimit, false);
                }

                return result;
            });
        }

        // Execute and await results for all our 4 tasks
        try {
            for (Future<DirectionValueTuple> d : this.es.invokeAll(tasks)) {
                DirectionValueTuple result = d.get(5, TimeUnit.SECONDS);
                if (result.value > bestValue) {
                    bestValue = result.value;
                    bestDirection = result.dir;
                }
            }
        } catch (InterruptedException e) {
            log.error("Interrupted during expectimax parallel search!");
        } catch (ExecutionException e) {
            log.error("Caught execution exception during expectimax parallel search: " + e.getMessage());
        } catch (TimeoutException e) {
            log.error("Execution of expectimax parallel directional search task exceeded timeout threshold!");
        }

        return bestDirection;
    }

    private double expectimax(int[][] board, int depth, boolean isMaximizingPlayer) {
        // Due to how 2048 works, we only have a max and chance node. We neglect the min node
        // log.debug("expectimax(" + depth + ", " + isMaximizingPlayer + ")");
        double alpha;

        if (depth == 0) { // TODO: OR IS TERMINAL/VICTORY NODE
            // TODO: ADD HEURISTICS
            double heuristic = getAllFreeCells(board).size();
            return heuristic;
        }

        if (isMaximizingPlayer) {
            /*
            return value of a maximum valued child node
            let α := -∞
            foreach child of node
                α := max(α, expectiminimax(child, depth-1))
             */
            alpha = 0.0;
            double maxValue = Double.MIN_VALUE;
            for (Direction directionToMove : Direction.values()) {
                int[][] boardCopy = getCopyOfBoard(board); // Get copy of the board sent as argument
                int[][] movedBoard = move(boardCopy, directionToMove);

                if (Arrays.deepEquals(movedBoard, boardCopy)) {
                    continue;
                }

                alpha = Math.max(alpha, expectimax(movedBoard, depth - 1, false));
            }
            return maxValue;

        } else {
            /*
            Return weighted average of all child nodes' values
            let α := 0
            foreach child of node
                α := α + (Probability[child] * expectiminimax(child, depth-1))
             */
            alpha = 0.0;
            double totalProbability = 0.0;
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] == 0) {
                        // Create copy for 2 tiles
                        int[][] newBoard1 = getCopyOfBoard(board);
                        newBoard1[row][col] = 2;
                        double score = expectimax(newBoard1, depth - 1, true);
                        alpha += (0.9 * score);
                        totalProbability += 0.9;

                        // Create copy for 4 tiles
                        int[][] newBoard2 = getCopyOfBoard(board);
                        newBoard2[row][col] = 4;
                        double score1 = expectimax(newBoard2, depth - 1, true);
                        alpha += (0.1 * score1);
                        totalProbability += 0.1;
                    }
                }
            }
            return alpha / totalProbability;
        }
    }

    /**
     * Does a move in the direction returned by the search
     */
    public void actuateNextMove() {
        Direction directionToMove = getNextMove();
        controller.doMove(directionToMove);
    }

    /**
     * Solve the entire 2048 problem using the expectimax algorithm
     */
    @Override
    public void solve() {

        long start = System.currentTimeMillis();
        log.info("Starting solver ...");

        // TODO: We need some kind of delay here to ensure a visible GUI update
        while (!controller.getBoard().hasWon() && controller.getBoard().canMove()) {
            Direction directionToMove = getNextMove();
            controller.doMove(directionToMove);
        }

        log.info("Solver ended after " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
    }

    /**
     * Shuts down the solver gracefully, by terminating the thread pool executor
     */
    @Override
    public void shutdown() {
        this.es.shutdown();
        try {
            this.es.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while awaiting ExecutorService shutdown!");
        }
    }

}
