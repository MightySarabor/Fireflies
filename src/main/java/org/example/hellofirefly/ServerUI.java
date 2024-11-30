package org.example.hellofirefly;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.util.concurrent.ConcurrentHashMap;

public class ServerUI extends Application {

    // Map zum Speichern des Zustands jedes Clients
    private final ConcurrentHashMap<Integer, Integer> clientStates = new ConcurrentHashMap<>();

    // 3x3 Gitter von Textfeldern
    private final Text[][] grid = new Text[3][3];

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();

        // Initialisiere das Gitter
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Text cell = new Text("0");
                grid[i][j] = cell;
                gridPane.add(cell, i, j);
            }
        }

        Scene scene = new Scene(gridPane, 300, 300);
        primaryStage.setTitle("Firefly Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start Thrift Server
        startThriftServer();
    }

    private void startThriftServer() {
        new Thread(() -> {
            try {
                // Thrift-Processor für den FireflyHandler
                FireflyService.Processor<FireflyHandler> processor = new FireflyService.Processor<>(new FireflyHandler());

                // Server-Transport (Zugangspunkt für Verbindungen)
                TServerTransport serverTransport = new TServerSocket(9090);

                // Verwende TThreadPoolServer für parallele Client-Verarbeitung
                TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport);
                serverArgs.processor(processor);
                TServer server = new TThreadPoolServer(serverArgs);

                System.out.println("Server started on port 9090...");
                server.serve();  // Starte den Server
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Handler für den Thrift-Service
    private class FireflyHandler implements FireflyService.Iface {
        @Override
        public void sendState(int clientId, int state) {
            // Zustand des Clients speichern
            clientStates.put(clientId, state);

            // Gitter im JavaFX-Thread aktualisieren
            javafx.application.Platform.runLater(() -> updateGrid());
        }

        private void updateGrid() {
            // Gehe durch die Gitterzellen und setze den Zustand des Clients
            int index = 0;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    // Hole den Zustand des aktuellen Clients, falls vorhanden
                    int state = clientStates.getOrDefault(index, 0);
                    grid[i][j].setText(String.valueOf(state));
                    index++;  // Erhöhe den Index, um die Gitterzellen zu füllen
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
