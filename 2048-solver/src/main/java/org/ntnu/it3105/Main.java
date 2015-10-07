package org.ntnu.it3105;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 *
 *
 */
public class Main extends Application {

    private static final Logger log = Logger.getLogger(Main.class);

    private Stage primaryStage;
    private Scene scene;
    private AnchorPane root;
    private Controller controller;

    @Override
    public void start(Stage primaryStage) {
        BasicConfigurator.configure();

        log.info("Starting 2048 JavaFX application");

        this.primaryStage = primaryStage;

        loadBoard();

        primaryStage.setResizable(false);
        primaryStage.setTitle("2048-solver");
        primaryStage.show();

        // Setting global key listener for the scene
        scene.setOnKeyReleased((keyEvent) -> {
            log.info("User pressed key: " + keyEvent.getCode());
            try {
                controller.doMove(Direction.directionFor(keyEvent.getCode()));
            } catch (IllegalArgumentException e) {
                log.debug("The user entered an invalid key code for direction: " + keyEvent.getCode());
            }
        });
    }

    private void loadBoard() {
        log.info("Loading Board.fxml onto primaryStage");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/Board.fxml"));

        try {
            root = loader.load();
            controller  = loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }

        scene = new Scene(root);
        this.primaryStage.setScene(scene);
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main( String[] args ) {
        launch(args);
    }
}
