package org.example.domain.generator;

import org.example.domain.model.Maze;

import java.util.*;

public class EllerMazeGenerator {

    private final Random random;

    public EllerMazeGenerator() {
        this(new Random());
    }

    public EllerMazeGenerator(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random must not be null");
        }
        this.random = random;
    }

    public Maze generate(int rows, int cols) {
        Maze maze = fullyWalled(rows, cols);

        int[] current = new int[cols];
        int[] next = new int[cols];
        int nextSetId = 1;

        for (int j = 0; j < cols; j++) {
            current[j] = nextSetId++;
        }

        for (int i = 0; i < rows; i++) {

            // Горизонтальные соединения внутри строки
            for (int j = 0; j < cols - 1; j++) {
                if (current[j] != current[j + 1] && random.nextBoolean()) {
                    maze.setRightWall(i, j, false);
                    mergeSets(current, current[j], current[j + 1]);
                }
            }

            // Соединяем в одно множество
            if (i == rows - 1) {
                for (int j = 0; j < cols - 1; j++) {
                    if (current[j] != current[j + 1]) {
                        maze.setRightWall(i, j, false);
                        mergeSets(current, current[j], current[j + 1]);
                    }
                }
                break;
            }

            // Зануляем для новых спусков
            Arrays.fill(next, 0);

            // Мапа где ключ - номер множества, значение - список индексов ячеек
            Map<Integer, List<Integer>> cellsBySet = new HashMap<>();

            // Заполняем мапу
            for (int j = 0; j < cols; j++) {
                int key = current[j];
                List<Integer> positions = cellsBySet.get(key);
                if (positions == null) {
                    positions = new ArrayList<>();
                    cellsBySet.put(key, positions);
                }
                positions.add(j);
            }

            // Считываем каждое множество и добавляем 1 спуск
            for (List<Integer> positions : cellsBySet.values()) {
                int j = positions.get(random.nextInt(positions.size()));
                next[j] = current[j];
                maze.setBottomWall(i, j, false);
            }

            // Случайные спуски
            for (int j = 0; j < cols; j++) {
                if (next[j] != 0) continue;
                if (random.nextBoolean()) {
                    next[j] = current[j];
                    maze.setBottomWall(i, j, false);
                }
            }

            // Клетки без спуска получают новые номера
            for (int j = 0; j < cols; j++) {
                if (next[j] == 0) {
                    next[j] = nextSetId++;
                }
            }

            //переносим next в current
            System.arraycopy(next, 0, current, 0, cols);
        }

        return maze;
    }

    private static Maze fullyWalled(int rows, int cols) {
        Maze maze = new Maze(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                maze.setRightWall(i, j, true);
                maze.setBottomWall(i, j, true);
            }
        }
        return maze;
    }

    private void mergeSets(int[] row, int from, int to) {
        for (int j = 0; j < row.length; j++) {
            if (row[j] == from) row[j] = to;
        }
    }

}