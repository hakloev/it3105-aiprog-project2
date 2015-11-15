package org.ntnu.it3105.game;

import javafx.scene.input.KeyCode;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 07/10/15.
 * <p>
 */
public enum Direction {

    /* The direction code is on the following a clockwise format */
    UP(0, -1, 0),
    RIGHT(1, 0, 1),
    DOWN(0, 1, 2),
    LEFT(-1, 0, 3);

    public final int x, y, directionCode;

    Direction(int x, int y, int directionCode) {
        this.x = x;
        this.y = y;
        this.directionCode = directionCode;
    }

    public static Direction directionFor(KeyCode keyCode) throws IllegalArgumentException {
        return valueOf(keyCode.name());
    }

}
