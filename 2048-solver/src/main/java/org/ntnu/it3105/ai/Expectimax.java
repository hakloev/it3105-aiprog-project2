package org.ntnu.it3105.ai;

import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.ntnu.it3105.game.Controller;
import org.ntnu.it3105.game.Direction;
import org.ntnu.it3105.utils.GameDataAppender;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.ServerSocket;

import static org.ntnu.it3105.game.Board.*;


/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 12/10/15.
 * <p>
 */
public class Expectimax implements Solver {

    public static boolean GAME_DATA_SCRAPER = Boolean.parseBoolean(System.getProperty("dataScraper", "true"));;
    private static long GUI_UPDATE_DELAY = 0L;

    private Logger log = Logger.getLogger(Expectimax.class);

    private Direction[] directions;
    private Controller controller;
    private ExecutorService es;
    private Random random;
    private int depthLimit;
    private int maxDepthLimit;

    public Expectimax(Controller controller, int depthLimit) {
        log.info("Starting Expectimax solver with " + Runtime.getRuntime().availableProcessors() + " core threads");
        this.controller = controller;
        this.depthLimit = depthLimit;
        this.maxDepthLimit = Integer.parseInt(System.getProperty("maxDepth", "8"));
        this.directions = Direction.values();
        this.random = new Random();
        // Let our thread pool consist of one thread per available processor core
        this.es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Creates a search in all four directions and returns the best direction
     * @return The direction to move in
     */
    public Direction getNextMove() {

        double bestValue = 0.0;
        Direction bestDirection = directions[random.nextInt(4)];

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
                Object[] values = move(boardCopy, d); // Both board and new score returned. We only want the board
                int[][] movedBoard = (int[][]) values[0];

                DirectionValueTuple result = new DirectionValueTuple(d, 0.0);

                if (d == Direction.DOWN && rightmostNotFull(movedBoard)) {
                    //log.info("Not full, skip it");
                    result.value = Double.MIN_VALUE;
                    return result;
                }

                if (!Arrays.deepEquals(movedBoard, boardCopy)) {
                    // Dynamically adjust depth limit based on free cells
                    int freeCells = getFreeCellCount(movedBoard);
                    int dl = depthLimit;
                    if (freeCells < 2) dl = this.maxDepthLimit;
                    else if (freeCells < 4) dl = this.maxDepthLimit - 2;
                    else if (freeCells < 6) dl = this.maxDepthLimit - 4;

                    result.value = expectimax(movedBoard, dl, false);
                }

                return result;
            });
        }

        // Execute and await results for all our 4 tasks
        try {
            for (Future<DirectionValueTuple> d : this.es.invokeAll(tasks)) {
                DirectionValueTuple result = d.get(5, TimeUnit.SECONDS);

                log.debug("Direction " + result.dir + " with value " + result.value);
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

        log.debug("Moving in direction: " + bestDirection + " with value " + bestValue);
        log.debug("Direction code " + bestDirection.directionCode );

        /* Appends the current state, and the move actuated to a file on the following format:

            2,0,2,8,2,8,16,256,2,16,8,64,4,2,8,32,2

            Each pair of four represent a row on the board, from top to bottom. The last
            digit represent the direction. See Direction for a detailed description of the
            direction code.

         */
        /*
        if ((GAME_DATA_SCRAPER) && (boardState != null)) {
            GameDataAppender.appendToFile(getFlattenedBoard(boardState) + bestDirection.directionCode + "\n");
        }
        */

        return bestDirection;
    }

    private double expectimax(int[][] board, int depth, boolean isMaximizingPlayer) {
        // Due to how 2048 works, we only have a max and chance node. We neglect the min node
        // log.debug("expectimax(" + depth + ", " + isMaximizingPlayer + ")");
        double alpha;
        boolean victory = isVictory(board);

        if (depth == 0 || victory) {
            /* THIS IS THE GRADIENT VERSION */
            //double h1 = getGradientValue(board) * 1.2;

            /* THIS IS THE SNAKE VERSION */
            double h1 = getGradientValue(board);

            /* OTHER HEURISTIC */
            //double h2 = highestInCorner(board) * 1.3;
            //double h3 = Math.log(getFreeCellCount(board));
            //double h4 = Math.log(getNumPossibleMerges(board));

            //log.info("BottomORVictory (" + depth + "): h2: " + h2 + " h3: " + h3  + " h4: " + h4);
            //log.info("BottomORVictory ("+depth+"): Heuristic: " + h2);
            //return h1 + h2 + h3;
            //return h1 + h2 + h3 + h4;
            return h1;
        }

        if (isMaximizingPlayer) {
            /*
            return value of a maximum valued child node
            let α := -∞
            foreach child of node
                α := max(α, expectiminimax(child, depth-1))
             */
            alpha = 0.0;
            for (Direction directionToMove : Direction.values()) {
                int[][] boardCopy = getCopyOfBoard(board); // Get copy of the board sent as argument
                Object[] values = move(boardCopy, directionToMove);
                int[][] movedBoard = (int[][]) values[0];

                if (Arrays.deepEquals(movedBoard, board)) {
                    continue;
                }

                alpha = Math.max(alpha, expectimax(movedBoard, depth - 1, false));
            }
            //log.info("MAX NODE (" + depth + "): MaxValue: " + alpha);
            return alpha;

        } else {
            /*
            Return weighted average of all child nodes' values
            let α := 0
            foreach child of node
                α := α + (Probability[child] * expectiminimax(child, depth-1))
             */
            alpha = 0.0;
            double totalChildren = 0;
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] == 0) {
                        // Create copy for 2 tiles
                        int[][] newBoard1 = getCopyOfBoard(board);
                        newBoard1[row][col] = 2;
                        double score = expectimax(newBoard1, depth - 1, true);
                        alpha += (0.9 * score);

                        // Create copy for 4 tiles
                        int[][] newBoard2 = getCopyOfBoard(board);
                        newBoard2[row][col] = 4;
                        double score1 = expectimax(newBoard2, depth - 1, true);
                        alpha += (0.1 * score1);

                        totalChildren++;
                    }
                }
            }
            double value = alpha / totalChildren;
            //log.info("CHANCE NODE ("+ depth +"): Alpha: " + alpha + " TotalProb: " + totalProbability + " Tot: " + value);
            return value;
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
        controller.startSolveLoop(() -> {
            long start = System.currentTimeMillis();
            log.info("Starting solver ...");

            // We need an atomic value signaling when the FX Application is complete with its UI update
            AtomicBoolean complete = new AtomicBoolean(true);
            while (!controller.getBoard().hasWon() && controller.getBoard().canMove()) {
                // Spin until last GUI update is complete
                while(!complete.get());
                // Set that we are now working
                complete.getAndSet(false);

                if (GUI_UPDATE_DELAY > 0) {
                    // Sleep for the specifi ed GUI update interval
                    try {
                        Thread.sleep(GUI_UPDATE_DELAY);
                    } catch (InterruptedException e) {
                        log.error("Interrupted during GUI Update sleep");
                    }
                }

                // Enqueue the next move + GUI update
                Platform.runLater(() -> {
                    Direction directionToMove = getNextMove();
                    controller.doMove(directionToMove);
                    // Signal that we are good to go for the next move
                    complete.getAndSet(true);
                });
            }

            log.info("Solver ended after " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
        });
    }

    public void solveForStatistics() {
        log.info("Starting statistics solver ...");
        log.info("Setting up server socket");
        ServerSocket ss = null;
        Socket net;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            ss = new ServerSocket(57315);
            log.info("Serversocket accepting connections on localhost:57315");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (true) {
            try {
                net = ss.accept();
                log.info("Connection established: " + net.toString());
                in = new BufferedReader(new InputStreamReader(net.getInputStream()));
                out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(
                                net.getOutputStream(), "UTF-8")), true);
                while (true) {
                    controller.reset();
                    long start = System.currentTimeMillis();
                    log.info("Starting new iteration...");
                    while (!controller.getBoard().hasWon() && controller.getBoard().canMove()) {
                        Direction directionToMove = getNextMoveANN(controller.getBoard().getBoard(), in, out);
                        controller.doMove(directionToMove);

                    }
                    out.println("END");
                    out.flush();
                    log.info("Solver ended after " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e1) {
                log.info("Client disconnected...");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * Talks to the ANN through a socket
     * @param b: Board representation
     * @param in: A BufferedReader
     * @param out: A PrintWriter
     * @return A direction
     */
    private Direction getNextMoveANN(int[][] b, BufferedReader in, PrintWriter out) {
        String board = "";
        Random r = new Random();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                board += b[y][x] + ",";
            }
        }
        board = board.substring(0, board.length() - 1);
        out.println(board);
        out.flush();
        int direction = r.nextInt();
        try {
            direction = Integer.parseInt(in.readLine().trim());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        GAME_DATA_SCRAPER = false;
        if (GAME_DATA_SCRAPER) {
            Direction expectimaxMove = getNextMove();
            GameDataAppender.appendToFile(getFlattenedBoard(b) + expectimaxMove.directionCode + "\n");
        }

        return Direction.values()[direction];
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
