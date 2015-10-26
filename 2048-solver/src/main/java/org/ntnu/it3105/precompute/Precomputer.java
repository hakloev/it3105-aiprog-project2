package org.ntnu.it3105.precompute;

import org.apache.log4j.Logger;
import org.ntnu.it3105.game.Direction;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Aleksander Skraastad (myth) on 10/16/15.
 * <p/>
 * 2048-solver is licenced under the MIT licence.
 */
public class Precomputer {

    public static Logger log = Logger.getLogger(Precomputer.class);
    public static int[] values = {0,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192};
    // Lookup table is store in MOVE LEFT
    public static HashMap<String, ResultTuple> lookupTable = new HashMap<>();

    /**
     * Computes all transformations and their score results
     */
    public static void compute() {

        log.info("Precomputer computing transformation lookup table");

        // Perform permutation
        int[][] permutations = generatePermutations();

        // Calculate effect and push to lookup table
        for (int[] i : permutations) {
            String seq = rowToString(i);
            lookupTable.put(seq, result(i));
        }
    }

    private static MoveResult get(int[][] board, Direction d) {
        MoveResult mr = new MoveResult();
        ResultTuple rt;
        int[] col;
        for (int i = 0; i < 4; i++) {
            switch (d) {
                case UP:
                    col = new int[]{board[0][i],board[1][i],board[2][i],board[3][i]};
                    rt = lookupTable.get(rowToString(col));
                    mr.res[0][i] = rt.res[0];
                    mr.res[1][i] = rt.res[1];
                    mr.res[2][i] = rt.res[2];
                    mr.res[3][i] = rt.res[3];
                    mr.score += rt.score;
                    break;
                case DOWN:

                    col = new int[]{board[3][i],board[2][i],board[1][i],board[0][i]};
                    rt = lookupTable.get(rowToString(col));
                    mr.res[0][i] = rt.res[3];
                    mr.res[1][i] = rt.res[2];
                    mr.res[2][i] = rt.res[1];
                    mr.res[3][i] = rt.res[0];
                    mr.score += rt.score;
                    break;
                case RIGHT:
                    int[] row = new int[]{board[i][3],board[i][2],board[i][1],board[i][0]};
                    rt = lookupTable.get(rowToString(row));
                    mr.res[i][0] = rt.res[3];
                    mr.res[i][1] = rt.res[2];
                    mr.res[i][2] = rt.res[1];
                    mr.res[i][3] = rt.res[0];
                    mr.score += rt.score;
                    break;
                default:
                    rt = lookupTable.get(rowToString(board[i]));
                    mr.res[i] = rt.res;
                    mr.score += rt.score;
            }
        }

        return mr;
    }

    /**
     * Generates all permutations of input of length 4 with repetition
     * @return A permutation matrix of length n^4
     */
    private static int[][] generatePermutations() {

        int n = values.length;
        int[] indexes = new int[n];
        int total = (int) Math.pow(n, 4);

        int[][] permutations = new int[total][4];

        while (--total >= 0) {
            for (int i = 0; i < n - (n - 4); i++)
                permutations[total][i] = values[indexes[i]];
            for (int i = 0; i < n; i++) {
                if (indexes[i] >= n - 1) {
                    indexes[i] = 0;
                } else {
                    indexes[i]++;
                    break;
                }
            }
        }

        return permutations;
    }

    /**
     * Calculates the result of performing a merge operation (direction right) on an input vector
     * @param state The state vector
     * @return A result vector
     */
    private static ResultTuple result(int[] state) {

        int lastMergePosition = 0;
        int score = 0;

        int[] res = Arrays.copyOf(state, 4);

        for (int col = 1; col < 4; col++) {

            if (res[col] == 0) {
                continue;
            }

            int previousPosition = col - 1;

            while (previousPosition > lastMergePosition && res[previousPosition] == 0) { // Skip all unpopulated cells (> 0)
                --previousPosition;
            }

            if (previousPosition == col) {
                continue;
            } else if (res[previousPosition] == 0) {
                // Moving to an empty cell
                res[previousPosition] = res[col];
                res[col] = 0;
            } else if (res[previousPosition] == res[col]) {
                // Merging two matching cells
                res[previousPosition] = res[col];
                res[col] = 0;
                res[previousPosition] *= 2;
                score += res[previousPosition]; // Update the score

                lastMergePosition = previousPosition + 1;
            } else if (res[previousPosition] != res[col] && previousPosition + 1 != col) {
                res[previousPosition + 1] = res[col];
                res[col] = 0;
            }
        }

        return new ResultTuple(res, score);
    }

    /**
     * Represents a row as a CSV string
     * @param row int array
     * @return CSV row representation
     */
    private static String rowToString(int[] row) {
        return "" + row[0] + "," + row[1] + "," + row[2] + "," + row[3];
    }

    /**
     * Wrapper object that contains the result of a left move and its score on a single row
     */
    public static class ResultTuple {
        public int[] res;
        public int score;
        public ResultTuple(int[] res, int score) {
            this.res = res;
            this.score = score;
        }
    }

    /**
     * Wrapper object that contains the result of a move on an entire board and its score
     */
    public static class MoveResult {
        public int[][] res;
        public int score;

        public MoveResult() {
            score = 0;
            res = new int[4][4];
        }
    }

    public static void main(String[] args) {

        compute();

        int[][] test = new int[][]{
            {0,0,0,0},
            {2,0,2,16},
            {0,8,0,0},
            {2,2,0,0}
        };

        for (int[] i : get(test, Direction.UP).res) {
            System.out.println(rowToString(i));
        }
        System.out.println();
        for (int[] i : get(test, Direction.RIGHT).res) {
            System.out.println(rowToString(i));
        }
        System.out.println();
        for (int[] i : get(test, Direction.DOWN).res) {
            System.out.println(rowToString(i));
        }
        System.out.println();
        for (int[] i : get(test, Direction.LEFT).res) {
            System.out.println(rowToString(i));
        }
    }
}