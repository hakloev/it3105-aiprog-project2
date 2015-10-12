package org.ntnu.it3105.game;

import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 07/10/15.
 * <p>
 */
public class Tile extends StackPane {

    public static int TILE_SIZE = 90;

    private int value;

    private Rectangle rectangle;
    private Text text;

    /**
     * Constructs a tile for the board
     * @param value
     */
    public Tile(int value) {
        this.value = value;
        constructTile();
    }

    private void constructTile() {
        // Configurate StackPane
        setPrefHeight(TILE_SIZE);
        setPrefWidth(TILE_SIZE);

        // Create Rectangle
        rectangle = new Rectangle(TILE_SIZE, TILE_SIZE, Color.TRANSPARENT);
        rectangle.setEffect(new DropShadow(10, 5, 5, Color.GRAY));
        rectangle.setArcWidth(5);
        rectangle.setArcHeight(5);
        getStyleClass().add("tile-color-" + value);

        // Create Text
        if (this.value != 0) {
            text = new Text(String.valueOf(this.value));
            if (this.value == Controller.TARGET_VALUE) {
                getStyleClass().add("tile-victory");
            }
        } else {
            text = new Text(" ");
        }
        text.getStyleClass().add("tile-label");

        // Append it to the StackPane
        getChildren().addAll(text); // rectangle
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Tile{" + this.value + "}";
    }

}
