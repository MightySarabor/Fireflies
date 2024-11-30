package org.example.hellofirefly;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class FireflyClient implements Runnable {

    private final int clientId;
    private int state = 0;

    public FireflyClient(int clientId) {
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try (TTransport transport = new TSocket("localhost", 9090)) {
            transport.open();
            FireflyService.Client client = new FireflyService.Client(new TBinaryProtocol(transport));

            while (true) {
                // Sende aktuellen Zustand an den Server
                client.sendState(clientId, state);
                System.out.println("Client " + clientId + " sent state: " + state); // Debug-Ausgabe

                // Zustand wechseln (0 -> 1 oder 1 -> 0)
                state = 1 - state;

                // Wartezeit entsprechend der Client-ID (z. B. Client 1: 1 Sekunde, Client 2: 2 Sekunden)
                Thread.sleep(clientId * 1000L); // Variiere die Wartezeit pro Client
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
