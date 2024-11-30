package org.example.hellofirefly;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import java.util.Random;

public class FireflyClient implements Runnable {

    // Parameter für einfache Anpassungen
    private static final long SLEEP_TIME = 100; // Kürzere Wartezeit, um schneller zu blinken
    private static final double ADJUSTMENT_FACTOR = 0.2; // Erhöhter Anpassungsfaktor für schnellere Synchronisation
    private static final double PERIOD = 1.0; // Beispielwert für die Periode
    private static final double TIME_STEP = 0.1; // Zeitschritt für die Phasenaktualisierung

    // Client ID
    private final int clientId;
    // X-Position im Gitter
    private final int gridX;
    // Y-Position im Gitter
    private final int gridY;
    // Aktuelle Phase
    private double phase;

    public FireflyClient(int clientId, int gridX, int gridY) {
        this.clientId = clientId;
        this.gridX = gridX;
        this.gridY = gridY;
        this.phase = new Random().nextDouble() * 2 * Math.PI;  // Zufällige Startphase zwischen 0 und 2π
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
               // System.out.println("Client " + clientId + " sent phase: " + phase);

                // Wartezeit entsprechend der SLEEP_TIME
                Thread.sleep(SLEEP_TIME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double updatePhase(int[] neighborStates, double currentPhase) {
        // Regelmäßige Phasenänderung
        currentPhase += (2 * Math.PI / PERIOD) * TIME_STEP;
        if (currentPhase >= 2 * Math.PI) {
            currentPhase -= 2 * Math.PI;
        }

        // Synchronisation mit Nachbarn
        double phaseAdjustment = 0.0;
        for (int state : neighborStates) {
            double neighborPhase = stateToPhase(state);
            phaseAdjustment += Math.sin(neighborPhase - currentPhase);
            //System.out.println("Nachbar Zustand: " + state + ", Phase: " + neighborPhase);
        }

        double adjustment = ADJUSTMENT_FACTOR * phaseAdjustment / neighborStates.length;
        currentPhase += adjustment;

        // Sicherstellen, dass die Phase zwischen 0 und 2π bleibt
        if (currentPhase < 0) {
            currentPhase += 2 * Math.PI;
        } else if (currentPhase >= 2 * Math.PI) {
            currentPhase -= 2 * Math.PI;
        }

       // System.out.println("Client " + clientId + " aktuelle Phase: " + currentPhase + ", Anpassung: " + adjustment);
        return currentPhase;
    }

    private int phaseToState(double phase) {
        return (int) ((Math.sin(phase) * 127.5) + 127.5); // Phase auf Bereich [0, 255] skalieren
    }

    private double stateToPhase(int state) {
        return (state / 127.5 - 1.0) * Math.PI; // Zustand zurück auf Bereich [0.0, 2π] skalieren
    }
}
