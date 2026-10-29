package org.example.domain.model;

import lombok.Getter;

public class Maze {
    public static final int MAX_SIZE = 50;
    public static final int MIN_SIZE = 2;

    @Getter
    private final int rows;
    @Getter
    private final int cols;
    private final boolean[][] horizontalWalls;
    private final boolean[][] verticalWalls;

    public Maze(int rows, int cols) {
        if (rows < MIN_SIZE || cols < MIN_SIZE || rows > MAX_SIZE || cols > MAX_SIZE){
            throw new IllegalArgumentException("Size of maze is incorrect");
        }
        this.rows = rows;
        this.cols = cols;
        this.horizontalWalls = new boolean[rows][cols];
        this.verticalWalls = new boolean[rows][cols];
    }

    public boolean hasRightWall(int i, int j) {
        return verticalWalls[i][j];
    }

    public boolean hasBottomWall(int i, int j) {
        return horizontalWalls[i][j];
    }

    public boolean hasLeftWall(int i, int j) {
        return j == 0 || verticalWalls[i][j - 1];
    }

    public boolean hasTopWall(int i, int j) {
        return i == 0 || horizontalWalls[i - 1][j];
    }

    public void setRightWall(int i, int j, boolean value) {
        verticalWalls[i][j] = value;
    }

    public void setBottomWall(int i, int j, boolean value) {
        horizontalWalls[i][j] = value;
    }
}