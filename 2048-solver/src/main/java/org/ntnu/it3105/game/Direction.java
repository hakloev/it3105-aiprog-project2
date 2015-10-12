package org.ntnu.it3105.game;

import javafx.scene.input.KeyCode;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 07/10/15.
 * <p>
 */
public enum Direction {

    UP(0, -1),
    DOWN(0, 1),
    RIGHT(1, 0),
    LEFT(-1, 0);

    public final int x, y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Direction directionFor(KeyCode keyCode) throws IllegalArgumentException {
        return valueOf(keyCode.name());
    }

}
