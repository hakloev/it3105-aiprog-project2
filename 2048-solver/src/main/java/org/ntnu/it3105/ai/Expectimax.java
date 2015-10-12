package org.ntnu.it3105.ai;

import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.ntnu.it3105.game.Controller;
import org.ntnu.it3105.game.Direction;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 12/10/15.
 * <p>
 */
public class Expectimax {

    private Logger log = Logger.getLogger(Expectimax.class);

    private Controller controller;
    private Stage primaryStage;
    private int depthLimit;

    public Expectimax(Controller controller, Stage primaryStage, int depthLimit) {
        log.info("Starting Expectimax solver");
        this.controller = controller;
        this.primaryStage = primaryStage;
        this.depthLimit = depthLimit;
    }

    public Direction getNextMove() {
        Direction nextMove = Direction.LEFT;
        return nextMove;
    }
}
