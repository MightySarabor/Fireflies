package org.example.hellofirefly;

import java.util.ArrayList;
import java.util.List;

public class ClientHandler {

    private final List<FireflyClient> clients = new ArrayList<>();

    public ClientHandler(int numberOfClients) {
        // Erstelle die Clients
        for (int i = 0; i < numberOfClients; i++) {
            clients.add(new FireflyClient(i + 1));
        }
    }

    public void startClients() {
        // Starte jeden Client in einem eigenen Thread
        for (FireflyClient client : clients) {
            new Thread(client).start();
        }
    }

    public static void main(String[] args) {
        // ClientHandler mit 9 Clients (3x3 Gitter)
        ClientHandler handler = new ClientHandler(9);
        handler.startClients();
    }
}
