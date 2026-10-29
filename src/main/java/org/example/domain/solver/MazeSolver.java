package org.example.domain.solver;

import org.example.domain.model.Maze;

import java.util.ArrayList;
import java.util.List;

public class MazeSolver {

    private final Maze maze;
    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public MazeSolver(Maze maze) {
        if (maze == null) {
            throw new IllegalArgumentException("Maze must not be null");
        }
        this.maze = maze;
    }

    public List<int[]> solve(int ai, int aj, int bi, int bj) {
        validateCoordinates(ai, aj);
        validateCoordinates(bi, bj);

        boolean[][] visited = new boolean[maze.getRows()][maze.getCols()];
        List<int[]> path = new ArrayList<>();

        if (dfs(ai, aj, bi, bj, visited, path)) {
            return path;
        }
        return null;
    }

    private boolean dfs(int i, int j, int ei, int ej, boolean[][] visited, List<int[]> path) {
        // Помечаем клетку как посещенную, добавляем в путь
        visited[i][j] = true;
        path.add(new int[]{i, j});

        // Если мы дошли до искомой клетки, возвращаем true
        if (i == ei && j == ej) {
            return true;
        }

        // Пробуем двигаться в каждую соседнюю клетку
        for (int[] dir : DIRS) {
            // Координаты соседа
            int ni = i + dir[0];
            int nj = j + dir[1];

            // Проверяем что сосед не вышел за границы
            if (ni < 0 || ni >= maze.getRows() || nj < 0 || nj >= maze.getCols()) {
                continue;
            }

            // Проверяем, что сосед не посещен
            if (visited[ni][nj]) {
                continue;
            }

            // Проверяем есть ли проход
            if (!canMove(i, j, ni, nj)) {
                continue;
            }

            // Идём в соседа. Если оттуда пришёл true, то путь найден,
            // пробрасываем true наверх, ничего не трогая в path
            if (dfs(ni, nj, ei, ej, visited, path)) {
                return true;
            }
        }

        // Тупик - удаляем клетку из пути, а родитель пробует в другого соседа
        path.removeLast();
        return false;
    }

    private boolean canMove(int ai, int aj, int bi, int bj) {
        int di = bi - ai;
        int dj = bj - aj;

        // проверяем стену в нужном направлении
        if (dj == 1)  return !maze.hasRightWall(ai, aj);
        if (dj == -1) return !maze.hasLeftWall(ai, aj);
        if (di == 1)  return !maze.hasBottomWall(ai, aj);
        return !maze.hasTopWall(ai, aj);
    }

    private void validateCoordinates(int i, int j) {
        if (i < 0 || i >= maze.getRows() || j < 0 || j >= maze.getCols()) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
    }
}
