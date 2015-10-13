package org.ntnu.it3105.ai;

import org.apache.log4j.Logger;
import org.ntnu.it3105.game.Controller;
import org.ntnu.it3105.game.Direction;
import java.util.Arrays;

import static org.ntnu.it3105.game.Board.*;


/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 12/10/15.
 * <p>
 */
public class Expectimax {

    private Logger log = Logger.getLogger(Expectimax.class);

    private Controller controller;
    private int depthLimit;

    public Expectimax(Controller controller, int depthLimit) {
        log.info("Starting Expectimax solver");
        this.controller = controller;
        this.depthLimit = depthLimit;
    }

    /**
     * Creates a search in all four directions and returns the best direction
     * @return The direction to move in
     */
    public Direction getNextMove() {

        Direction bestDirection = Direction.UP;
        double bestValue = 0.0;

        // TODO: Create ExecutorService to thread out the creation of search trees
        for (Direction directionToMove : Direction.values()) {
            int[][] boardCopy = controller.getBoard().getCopyOfBoard(); // Get copy of the current state in the actual board.
            int[][] movedBoard = move(boardCopy, directionToMove);

            if (Arrays.deepEquals(movedBoard, boardCopy)) {
                //log.debug("No need to try move to " + directionToMove + " since the state isn't changed");
                continue;
            }

            double directionValue = expectimax(movedBoard, depthLimit, false);

            if (directionValue > bestValue) {
                bestValue = directionValue;
                bestDirection = directionToMove;
            }
        }
        return bestDirection;
    }

    private double expectimax(int[][] board, int depth, boolean isMaximizingPlayer) {
        // Due to how 2048 works, we only have a max and chance node. We neglect the min node
        log.debug("expectimax(" + depth + ", " + isMaximizingPlayer + ")");
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
    public void solve() {
        // TODO: We need some kind of delay here to ensure a visible GUI update
        while (!controller.getBoard().hasWon() && controller.getBoard().canMove()) {
            Direction directionToMove = getNextMove();
            controller.doMove(directionToMove);
        }
    }

}
