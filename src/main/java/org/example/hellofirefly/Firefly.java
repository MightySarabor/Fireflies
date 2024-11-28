package org.example.hellofirefly;

import java.util.ArrayList;
import java.util.List;

public class Firefly {
    private double phase; // Phase im Zyklus, z.B. zwischen 0 und 1
    private double syncRate; // Synchronisationsrate, beeinflusst die Phase der Nachbarn
    private List<Firefly> neighbors; // Nachbarn im Torus
    private boolean isFlashing; // Gibt an, ob das Glühwürmchen aktuell aufleuchtet
    private static final double CYCLE_INCREMENT = 0.005; // Erhöhte Schrittweite für noch schnelleres Update
    private static final double FLASH_DURATION = 0.01; // Kürzere Leuchtdauer

    public Firefly(double syncRate) {
        this.phase = Math.random(); // Zufällige Startphase
        this.syncRate = syncRate;
        this.neighbors = new ArrayList<>();
        this.isFlashing = false;
    }

    public void addNeighbor(Firefly neighbor) {
        this.neighbors.add(neighbor);
    }

    public void updatePhase() {
        // Beeinflussung durch Nachbarn nach dem Kuramoto-Modell
        for (Firefly neighbor : neighbors) {
            this.phase += syncRate * (neighbor.phase - this.phase);
        }

        // Schnellerer Zyklus für beschleunigte Synchronisation
        this.phase = (this.phase + CYCLE_INCREMENT) % 1.0;

        // Das Glühwürmchen leuchtet nur, wenn die Phase nahe 1.0 ist, für eine kürzere Zeitdauer
        isFlashing = this.phase >= (1.0 - FLASH_DURATION) && this.phase <= 1.0;
    }

    public boolean isFlashing() {
        return isFlashing;
    }

    public double getPhase() {
        return phase;
    }
}
