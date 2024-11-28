package org.example.hellofirefly;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FireflySimulation extends Application {

    private static final int GRID_SIZE = 10; // Größe des Torus
    private static final int RECT_SIZE = 30; // Größe der Rechtecke
    private static final double SYNC_RATE = 0.1; // Synchronisationsrate
    private Torus torus;

    @Override
    public void start(Stage primaryStage) {
        torus = new Torus(GRID_SIZE, SYNC_RATE);

        GridPane gridPane = new GridPane();
        Rectangle[][] rectangles = new Rectangle[GRID_SIZE][GRID_SIZE];

        // Erstellen der Rechtecke und Hinzufügen zur GUI
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Rectangle rect = new Rectangle(RECT_SIZE, RECT_SIZE);
                rectangles[i][j] = rect;
                gridPane.add(rect, j, i);
            }
        }

        // Timeline für die Animation und Aktualisierung
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> update(rectangles))); // Reduzierte Zeit für schnellere Updates
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Scene scene = new Scene(gridPane);
        primaryStage.setTitle("Firefly Synchronization Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Aktualisierung der GUI auf Basis des Glühwürmchen-Zustands
    private void update(Rectangle[][] rectangles) {
        torus.update(); // Phase aller Glühwürmchen im Torus aktualisieren

        Firefly[][] grid = torus.getGrid();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Firefly firefly = grid[i][j];

                // Wenn das Glühwürmchen leuchtet, wird es gelb; ansonsten wird es schwarz dargestellt
                rectangles[i][j].setFill(firefly.isFlashing() ? Color.YELLOW : Color.BLACK);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
