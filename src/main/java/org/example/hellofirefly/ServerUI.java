package org.example.hellofirefly;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerUI extends Application {

    private static final int GRID_SIZE = 5; // Größe des Gitters
    private static final int CELL_SIZE = 50; // Größe der Zellen
    private final ConcurrentHashMap<Integer, Integer> clientStates = new ConcurrentHashMap<>();
    private final Rectangle[][] grid = new Rectangle[GRID_SIZE][GRID_SIZE];

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE, Color.BLACK);
                grid[i][j] = cell;
                gridPane.add(cell, i, j);
            }
        }

        Scene scene = new Scene(gridPane, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        primaryStage.setTitle("Firefly Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        startThriftServer();
    }

    private void startThriftServer() {
        new Thread(() -> {
            try {
                FireflyService.Processor<FireflyHandler> processor = new FireflyService.Processor<>(new FireflyHandler());
                TServerTransport serverTransport = new TServerSocket(9090);
                TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport);
                serverArgs.processor(processor);
                TServer server = new TThreadPoolServer(serverArgs);

                System.out.println("Server started on port 9090...");
                server.serve();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class FireflyHandler implements FireflyService.Iface {
        @Override
        public void sendState(int clientId, int state) {
            clientStates.put(clientId, state);
            javafx.application.Platform.runLater(ServerUI.this::updateGrid);
        }

        @Override
        public List<Integer> getNeighborStates(int gridX, int gridY) {
            List<Integer> neighbors = new ArrayList<>();
            int[][] positions = {
                    {gridX, (gridY + 1) % GRID_SIZE},  // right
                    {gridX, (gridY - 1 + GRID_SIZE) % GRID_SIZE},  // left
                    {(gridX + 1) % GRID_SIZE, gridY},  // down
                    {(gridX - 1 + GRID_SIZE) % GRID_SIZE, gridY},  // up
                    {(gridX + 1) % GRID_SIZE, (gridY + 1) % GRID_SIZE},  // down-right
                    {(gridX + 1) % GRID_SIZE, (gridY - 1 + GRID_SIZE) % GRID_SIZE},  // down-left
                    {(gridX - 1 + GRID_SIZE) % GRID_SIZE, (gridY + 1) % GRID_SIZE},  // up-right
                    {(gridX - 1 + GRID_SIZE) % GRID_SIZE, (gridY - 1 + GRID_SIZE) % GRID_SIZE}  // up-left
            };

            for (int[] pos : positions) {
                int neighborId = pos[0] * GRID_SIZE + pos[1];
                neighbors.add(clientStates.getOrDefault(neighborId, 0));
            }

            return neighbors;
        }
    }

    private void updateGrid() {
        int index = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int state = clientStates.getOrDefault(index + 1, 0); // Client IDs sind 1-basiert
                Color color = state > 0 ? Color.rgb(state, state, 0) : Color.BLACK; // Gelber Farbton basierend auf Zustand
                grid[i][j].setFill(color);
                index++;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
