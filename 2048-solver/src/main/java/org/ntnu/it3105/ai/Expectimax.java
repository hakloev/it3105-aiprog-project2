package org.ntnu.it3105.ai;

import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.ntnu.it3105.game.Controller;
import org.ntnu.it3105.game.Direction;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 12/10/15.
 * <p>
 */
public class ExpectiMax {

    private Logger log = Logger.getLogger(ExpectiMax.class);

    private Controller controller;
    private Stage primaryStage;
    private int depthLimit;

    public ExpectiMax(Controller controller, Stage primaryStage, int depthLimit) {
        log.info("Starting Expectimax solver");
        this.controller = controller;
        this.primaryStage = primaryStage;
        this.depthLimit = depthLimit;
    }

    /*
    function expectiminimax(node, depth)
    if node is a terminal node or depth = 0
        return the heuristic value of node
    if the adversary is to play at node
        // Return value of minimum-valued child node
        let α := +∞
        foreach child of node
            α := min(α, expectiminimax(child, depth-1))
    else if we are to play at node
        // Return value of maximum-valued child node
        let α := -∞
        foreach child of node
            α := max(α, expectiminimax(child, depth-1))
    else if random event at node
        // Return weighted average of all child nodes' values
        let α := 0
        foreach child of node
            α := α + (Probability[child] * expectiminimax(child, depth-1))
    return α
     */

    public Direction getNextMove() {
        Direction nextMove = Direction.LEFT;
        return nextMove;
    }
}
