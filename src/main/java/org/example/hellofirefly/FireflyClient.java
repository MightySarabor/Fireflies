package org.example.hellofirefly;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import java.util.Random;

public class FireflyClient implements Runnable {

    // Client ID
    private final int clientId;
    // X-Position im Gitter
    private final int gridX;
    // Y-Position im Gitter
    private final int gridY;
    // Aktuelle Phase
    private double phase;

    // Zeitintervall für das Senden des Zustands in Millisekunden
    private static final long SLEEP_TIME = 100; // 1 Sekunde
    // Skalierungsfaktor für die Phasenanpassung
    private static final double ADJUSTMENT_FACTOR = 0.1;

    public FireflyClient(int clientId, int gridX, int gridY) {
        this.clientId = clientId;
        this.gridX = gridX;
        this.gridY = gridY;
        this.phase = new Random().nextDouble();  // Zufällige Startphase
    }

    @Override
    public void run() {
        try (TTransport transport = new TSocket("localhost", 9090)) {
            transport.open();
            FireflyService.Client client = new FireflyService.Client(new TBinaryProtocol(transport));

            while (true) {
                // Zustände der Nachbarn abfragen
                int[] neighborStates = client.getNeighborStates(gridX, gridY).stream().mapToInt(Integer::intValue).toArray();

                // Phasenberechnung
                if (neighborStates.length > 0) {
                    phase = updatePhase(neighborStates, phase);
                } else {
                    System.out.println("Keine Nachbarn gefunden für Client " + clientId);
                }

                // Neuen Zustand an den Server senden
                client.sendState(clientId, phaseToState(phase));
                System.out.println("Client " + clientId + " sent phase: " + phase);

                // Wartezeit entsprechend der SLEEP_TIME
                Thread.sleep(SLEEP_TIME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double updatePhase(int[] neighborStates, double currentPhase) {
        double sum = 0.0;
        for (int state : neighborStates) {
            double neighborPhase = stateToPhase(state);
            sum += neighborPhase;
            System.out.println("Nachbar Zustand: " + state + ", Phase: " + neighborPhase);
        }

        double averagePhase = sum / neighborStates.length;
        System.out.println("Client " + clientId + " aktuelle Phase: " + currentPhase + ", durchschnittliche Nachbar-Phase: " + averagePhase);
        return currentPhase + ADJUSTMENT_FACTOR * (averagePhase - currentPhase);
    }

    private int phaseToState(double phase) {
        return (int) ((phase % 1.0) * 255); // Phase auf Bereich [0, 255] skalieren
    }

    private double stateToPhase(int state) {
        return state / 255.0; // Zustand zurück auf Bereich [0.0, 1.0] skalieren
    }
}
