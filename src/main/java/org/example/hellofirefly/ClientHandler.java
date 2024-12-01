package org.example.hellofirefly;

import java.util.ArrayList;
import java.util.List;

public class ClientHandler {

    private final List<FireflyClient> clients = new ArrayList<>();

    public ClientHandler(int numberOfClients) {
        // Erstelle die Clients
        int gridSize = (int) Math.sqrt(numberOfClients);
        for (int i = 0; i < numberOfClients; i++) {
            int gridX = i / gridSize;
            int gridY = i % gridSize;
            clients.add(new FireflyClient(i + 1, gridX, gridY));
        }
    }

    public void startClients() {
        // Starte jeden Client in einem eigenen Thread
        for (FireflyClient client : clients) {
            new Thread(client).start();
            try {
                Thread.sleep(10); // 10 Millisekunden Verzögerung
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        // ClientHandler mit 9 Clients (3x3 Gitter)
        ClientHandler handler = new ClientHandler(100);
        handler.startClients();
    }
}
