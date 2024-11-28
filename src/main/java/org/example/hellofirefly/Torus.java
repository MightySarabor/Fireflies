package org.example.hellofirefly;

public class Torus {
    private Firefly[][] grid;
    private int size;

    public Torus(int size, double syncRate) {
        this.size = size;
        grid = new Firefly[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = new Firefly(syncRate);
            }
        }

        initializeNeighbors();
    }

    private void initializeNeighbors() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Firefly current = grid[i][j];
                // Nachbarn im Torus definieren
                current.addNeighbor(grid[(i - 1 + size) % size][j]);
                current.addNeighbor(grid[(i + 1) % size][j]);
                current.addNeighbor(grid[i][(j - 1 + size) % size]);
                current.addNeighbor(grid[i][(j + 1) % size]);
            }
        }
    }

    public void update() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j].updatePhase();
            }
        }
    }

    public Firefly[][] getGrid() {
        return grid;
    }
}
