package org.example.domain.generator;

import org.example.domain.model.Cave;

import java.util.Random;

public class CaveGenerator {
    public static final int MIN_LIMIT = 0;
    public static final int MAX_LIMIT = 7;

    private final Random random;

    public CaveGenerator() {
        this(new Random());
    }

    public CaveGenerator(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random must not be null");
        }
        this.random = random;
    }

    private void init(Cave cave, double aliveChance) {
        for (int i = 0; i < cave.getRows(); i++) {
            for (int j = 0; j < cave.getCols(); j++) {
                cave.setCell(i, j, random.nextDouble() < aliveChance);
            }
        }
    }

    public Cave generate(int rows, int cols, double aliveChance) {
        if (aliveChance < 0.0 || aliveChance > 1.0) {
            throw new IllegalArgumentException("AliveChance must be in [0.0, 1.0]");
        }

        Cave cave = new Cave(rows, cols);
        init(cave, aliveChance);
        return cave;
    }

    public Cave step(Cave cave, int birthLimit, int deathLimit) {
        if (cave == null) {
            throw new IllegalArgumentException("Cave must not be null");
        }
        if (birthLimit < MIN_LIMIT || birthLimit > MAX_LIMIT) {
            throw new IllegalArgumentException("BirthLimit must be in [" + MIN_LIMIT + ", " + MAX_LIMIT + "]");
        }
        if (deathLimit < MIN_LIMIT || deathLimit > MAX_LIMIT) {
            throw new IllegalArgumentException("DeathLimit must be in [" + MIN_LIMIT + ", " + MAX_LIMIT + "]");
        }

        int rows = cave.getRows();
        int cols = cave.getCols();
        Cave next = new Cave(rows, cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int aliveNeighbors = countAliveNeighbours(cave, i, j);

                if (cave.isAlive(i, j)) {
                    next.setCell(i, j, (aliveNeighbors >= deathLimit));
                } else {
                    next.setCell(i, j, aliveNeighbors > birthLimit);
                }
            }
        }

        return next;
    }

    private int countAliveNeighbours(Cave cave, int i, int j) {
        int count = 0;

        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                if (di == 0 && dj == 0) {
                    continue;
                }

                // Координаты соседней клетки
                int ni = i + di;
                int nj = j + dj;

                // Обрабатываем за границей
                if (ni < 0 || ni >= cave.getRows() || nj < 0 || nj >= cave.getCols()) {
                    count++;
                } else if (cave.isAlive(ni, nj)) {
                    count++;
                }
            }
        }

        return count;
    }
}