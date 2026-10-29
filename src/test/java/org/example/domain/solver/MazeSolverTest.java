package org.example.domain.solver;

import org.example.domain.generator.EllerMazeGenerator;
import org.example.domain.model.Maze;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MazeSolverTest {

    // ==================== 1. ПРОСТЫЕ СЛУЧАИ ====================

    @Test
    @DisplayName("Начало = конец -> путь из одной клетки")
    void solve_startEqualsEnd() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(3, 3);
        List<int[]> path = new MazeSolver(maze).solve(1, 1, 1, 1);

        assertNotNull(path);
        assertEquals(1, path.size());
        assertArrayEquals(new int[]{1, 1}, path.get(0));
    }

    @Test
    @DisplayName("Соседние клетки без стены -> путь из двух клеток")
    void solve_adjacentCells() {
        // Лабиринт 2x1: две клетки друг над другом без стены
        Maze maze = new Maze(2, 2);
        maze.setBottomWall(0, 0, false);   // проход между (0,0) и (1,0)
        maze.setRightWall(0, 0, true);
        maze.setRightWall(1, 0, true);
        maze.setBottomWall(1, 0, true);

        List<int[]> path = new MazeSolver(maze).solve(0, 0, 1, 0);

        assertNotNull(path);
        assertEquals(2, path.size());
        assertArrayEquals(new int[]{0, 0}, path.get(0));
        assertArrayEquals(new int[]{1, 0}, path.get(1));
    }

    @Test
    @DisplayName("Соседние клетки со стеной -> путь не найден")
    void solve_adjacentCellsWithWall() {
        Maze maze = new Maze(2, 2);
        maze.setBottomWall(0, 0, true);    // стена между (0,0) и (1,0)
        maze.setRightWall(0, 0, true);
        maze.setRightWall(1, 0, true);
        maze.setBottomWall(1, 0, true);

        List<int[]> path = new MazeSolver(maze).solve(0, 0, 1, 0);

        assertNull(path);
    }

    // ==================== 2. КОРРЕКТНОСТЬ ПУТИ ====================

    @Test
    @DisplayName("Путь связный: каждая клетка — сосед предыдущей")
    void solve_pathShouldBeConnected() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 5);
        List<int[]> path = new MazeSolver(maze).solve(0, 0, 4, 4);

        assertNotNull(path);
        for (int k = 0; k < path.size() - 1; k++) {
            int[] a = path.get(k);
            int[] b = path.get(k + 1);
            int di = Math.abs(a[0] - b[0]);
            int dj = Math.abs(a[1] - b[1]);
            assertEquals(1, di + dj,
                    "Cells (" + a[0] + "," + a[1] + ") and (" + b[0] + "," + b[1] +
                            ") are not neighbors");
        }
    }

    @Test
    @DisplayName("Путь без стен: между соседними клетками нет стены")
    void solve_pathShouldNotCrossWalls() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 5);
        List<int[]> path = new MazeSolver(maze).solve(0, 0, 4, 4);

        assertNotNull(path);
        for (int k = 0; k < path.size() - 1; k++) {
            int[] a = path.get(k);
            int[] b = path.get(k + 1);
            assertTrue(canMove(maze, a[0], a[1], b[0], b[1]),
                    "Wall between (" + a[0] + "," + a[1] + ") and (" + b[0] + "," + b[1] + ")");
        }
    }

    @Test
    @DisplayName("Путь начинается в start и заканчивается в end")
    void solve_pathStartsAndEndsCorrectly() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 5);
        List<int[]> path = new MazeSolver(maze).solve(0, 0, 4, 4);

        assertNotNull(path);
        assertArrayEquals(new int[]{0, 0}, path.get(0));
        assertArrayEquals(new int[]{4, 4}, path.get(path.size() - 1));
    }

    // ==================== 3. ИДЕАЛЬНЫЙ ЛАБИРИНТ ====================

    @Test
    @DisplayName("Идеальный лабиринт: путь всегда существует")
    void solve_idealMazeAlwaysHasPath() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(10, 10);

        for (int si = 0; si < 10; si++) {
            for (int sj = 0; sj < 10; sj++) {
                for (int ei = 0; ei < 10; ei++) {
                    for (int ej = 0; ej < 10; ej++) {
                        List<int[]> path = new MazeSolver(maze).solve(si, sj, ei, ej);
                        assertNotNull(path,
                                "No path from (" + si + "," + sj + ") to (" + ei + "," + ej + ")");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Идеальный лабиринт: путь единственный (обратимость)")
    void solve_pathIsReversible() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(10, 10);

        List<int[]> forward = new MazeSolver(maze).solve(0, 0, 9, 9);
        List<int[]> backward = new MazeSolver(maze).solve(9, 9, 0, 0);

        assertNotNull(forward);
        assertNotNull(backward);
        assertEquals(forward.size(), backward.size());

        for (int k = 0; k < forward.size(); k++) {
            int[] f = forward.get(k);
            int[] b = backward.get(backward.size() - 1 - k);
            assertArrayEquals(f, b,
                    "Forward path and reversed backward path differ at position " + k);
        }
    }

    // ==================== 4. ВАЛИДАЦИЯ ====================

    @Test
    @DisplayName("null maze -> исключение в конструкторе")
    void constructor_nullMazeShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(null));
    }

    @Test
    @DisplayName("Начало за границей -> исключение")
    void solve_startOutOfBoundsShouldThrow() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 5);

        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(maze).solve(-1, 0, 4, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(maze).solve(0, -1, 4, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(maze).solve(5, 0, 4, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(maze).solve(0, 5, 4, 4));
    }

    @Test
    @DisplayName("Конец за границей -> исключение")
    void solve_endOutOfBoundsShouldThrow() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 5);

        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(maze).solve(0, 0, -1, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(maze).solve(0, 0, 4, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(maze).solve(0, 0, 5, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new MazeSolver(maze).solve(0, 0, 4, 5));
    }

    // ==================== 5. ГРАНИЧНЫЕ СЛУЧАИ ====================

    @Test
    @DisplayName("2x2 — путь от угла до угла")
    void solve_smallSquareMaze() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(2, 2);
        List<int[]> path = new MazeSolver(maze).solve(0, 0, 1, 1);

        assertNotNull(path);
        assertArrayEquals(new int[]{0, 0}, path.get(0));
        assertArrayEquals(new int[]{1, 1}, path.get(path.size() - 1));
    }

    @Test
    @DisplayName("2x5 — путь вдоль строки")
    void solve_singleRowMaze() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(2, 5);
        List<int[]> path = new MazeSolver(maze).solve(0, 0, 0, 4);

        assertNotNull(path);
        assertArrayEquals(new int[]{0, 0}, path.get(0));
        assertArrayEquals(new int[]{0, 4}, path.get(path.size() - 1));
    }

    @Test
    @DisplayName("5x2 — путь вдоль столбца")
    void solve_singleColumnMaze() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 2);
        List<int[]> path = new MazeSolver(maze).solve(0, 0, 4, 0);

        assertNotNull(path);
        assertArrayEquals(new int[]{0, 0}, path.get(0));
        assertArrayEquals(new int[]{4, 0}, path.get(path.size() - 1));
    }

    @Test
    @DisplayName("50x50 — максимальный размер")
    void solve_maxSizeMaze() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(50, 50);
        List<int[]> path = new MazeSolver(maze).solve(0, 0, 49, 49);

        assertNotNull(path);
        assertArrayEquals(new int[]{0, 0}, path.get(0));
        assertArrayEquals(new int[]{49, 49}, path.get(path.size() - 1));
    }

    // ==================== 6. ВСПОМОГАТЕЛЬНЫЕ ====================

    /** Свой canMove — тест не зависит от MazeSolver. */
    private boolean canMove(Maze maze, int i, int j, int ni, int nj) {
        if (ni < 0 || ni >= maze.getRows() || nj < 0 || nj >= maze.getCols()) return false;
        if (ni == i && nj == j + 1) return !maze.hasRightWall(i, j);
        if (ni == i && nj == j - 1) return !maze.hasLeftWall(i, j);
        if (ni == i + 1 && nj == j) return !maze.hasBottomWall(i, j);
        if (ni == i - 1 && nj == j) return !maze.hasTopWall(i, j);
        return false;
    }
}