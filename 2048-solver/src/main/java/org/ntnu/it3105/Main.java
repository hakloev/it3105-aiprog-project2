package org.ntnu.it3105;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ntnu.it3105.ai.Expectimax;
import org.ntnu.it3105.game.Controller;
import org.ntnu.it3105.game.Direction;
import org.ntnu.it3105.utils.GameDataAppender;

import static org.ntnu.it3105.ai.Expectimax.GAME_DATA_SCRAPER;

import java.io.IOException;

public class Main extends Application {

    public static boolean USE_GUI = Boolean.parseBoolean(System.getProperty("useGui", "false"));
    public static boolean USE_SOCKET = Boolean.parseBoolean(System.getProperty("useSocket", "true"));

    private static final Logger log = Logger.getLogger(Main.class);

    private int NUMBER_OF_STATISTIC_RUNS = Integer.parseInt(System.getProperty("maxRuns", "30"));

    private Stage primaryStage;
    private Scene scene;
    private AnchorPane root;
    private Controller controller;

    private Expectimax solver;

    public Main() {
        if (!USE_GUI) {
            PropertyConfigurator.configure(getClass().getClassLoader().getResource("config/log4j.properties"));
            log.info("Starting 2048 Statistics Scrapper with " + NUMBER_OF_STATISTIC_RUNS + " runs");

            controller = new Controller();
            controller.initialize();
            solver = new Expectimax(controller, 4);

            if (USE_SOCKET) {
                solver.solveForStatisticsUsingSocket();
            } else {
                solver.solveForStatistics();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        PropertyConfigurator.configure(getClass().getClassLoader().getResource("config/log4j.properties"));

        log.info("Starting 2048 JavaFX application");
        this.primaryStage = primaryStage;

        loadBoardAndSetScene();
        configureAndShowPrimaryStage();

        // Initiate the Expectimax AI
        solver = new Expectimax(controller, 4);

        // Setting global key listener for the scene
        scene.setOnKeyReleased((keyEvent) -> {
            log.info("User pressed key: " + keyEvent.getCode());

            switch (keyEvent.getCode()) {
                case R:
                    log.debug("Restarted the game");
                    controller.reset();
                    break;
                case S:
                    log.info("Use AI to solve the game");
                    solver.solve();
                    break;
                case ENTER:
                    log.info("Use AI to do one move");
                    solver.actuateNextMove();
                    break;
                case E:
                    if (USE_SOCKET) {
                        solver.solveForStatisticsUsingSocket();
                    } else {
                        solver.solveForStatistics();
                    }
                default:
                    try {
                        controller.doMove(Direction.directionFor(keyEvent.getCode()));
                    } catch (IllegalArgumentException e) {
                        log.debug("The user entered an invalid key code for direction: " + keyEvent.getCode());
                    }
                    break;
            }

        });
    }

    /**
     * Method for configuring the primaryStage
     */
    private void configureAndShowPrimaryStage() {
        primaryStage.setResizable(false);
        primaryStage.setTitle("2048-solver");
        primaryStage.show();

        // We send shutdown signals to our solver and controller, since they might use a separate thread pool
        primaryStage.setOnCloseRequest(e -> {
            solver.shutdown();
            controller.shutdown();
        });
    }

    /**
     * Method for loading the board-fxml file and populating the scene and primaryStage
     */
    private void loadBoardAndSetScene() {
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
        // Change theme here

        //scene.getStylesheets().add("css/stylesheet.css");
        scene.getStylesheets().add("css/dudes.css");

        this.primaryStage.setScene(scene);
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Main method
     * @param args
     */
    public static void main( String[] args ) {
        if (USE_GUI) {
            launch(args);
        } else if (!USE_GUI) {
            Main m = new Main();
        } else {
            log.info("This is not a valid startup combination");
        }

    }
}
