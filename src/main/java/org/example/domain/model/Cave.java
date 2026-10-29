package org.example.domain.model;

import lombok.Getter;

public class Cave {
    public static final int MAX_SIZE = 50;

    @Getter
    private final int rows;
    @Getter
    private final int cols;
    private final boolean[][] cells;

    public Cave(int rows, int cols) {
        if (rows <= 0 || cols <= 0 || rows > MAX_SIZE || cols > MAX_SIZE) {
            throw new IllegalArgumentException("Size of cave is incorrect");
        }
        this.rows = rows;
        this.cols = cols;
        this.cells = new boolean[rows][cols];
    }

    public void setCell(int i, int j, boolean b) {
        cells[i][j] = b;
    }

    public boolean isAlive(int i, int j) {
        return cells[i][j];
    }
}
