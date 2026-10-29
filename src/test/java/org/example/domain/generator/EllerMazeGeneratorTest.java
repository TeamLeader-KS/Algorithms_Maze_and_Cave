package org.example.domain.generator;

import org.example.domain.model.Maze;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class EllerMazeGeneratorTest {

    // ==================== 0. КОНСТРУКТОРЫ ====================

    @Test
    @DisplayName("EllerMazeGenerator: конструктор без параметров работает")
    void constructor_noArgs_works() {
        EllerMazeGenerator gen = new EllerMazeGenerator();
        assertNotNull(gen);

        Maze maze = gen.generate(5, 5);
        assertEquals(5, maze.getRows());
        assertEquals(5, maze.getCols());
    }

    @Test
    @DisplayName("EllerMazeGenerator: конструктор с Random работает")
    void constructor_withRandom_works() {
        EllerMazeGenerator gen = new EllerMazeGenerator(new Random(42));
        assertNotNull(gen);

        Maze maze = gen.generate(5, 5);
        assertEquals(5, maze.getRows());
        assertEquals(5, maze.getCols());
    }

    @Test
    @DisplayName("EllerMazeGenerator: null Random -> исключение")
    void constructor_nullRandom_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new EllerMazeGenerator((Random) null));
    }

    // ==================== 1. РАЗМЕРЫ ====================

    @Test
    @DisplayName("Размеры лабиринта совпадают с заданными")
    void generate_shouldHaveCorrectSize() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 7);
        assertEquals(5, maze.getRows());
        assertEquals(7, maze.getCols());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10, 25, 50})
    @DisplayName("Разные размеры генерируются корректно")
    void generate_shouldWorkForVariousSizes(int size) {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(size, size);
        assertEquals(size, maze.getRows());
        assertEquals(size, maze.getCols());
    }

    // ==================== 2. ГРАНИЦЫ ====================

    @Test
    @DisplayName("Правая граница всегда закрыта")
    void generate_rightBorderShouldBeClosed() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 5);
        int cols = maze.getCols();
        for (int i = 0; i < maze.getRows(); i++) {
            assertTrue(maze.hasRightWall(i, cols - 1),
                    "Right border missing at row " + i);
        }
    }

    @Test
    @DisplayName("Нижняя граница всегда закрыта")
    void generate_bottomBorderShouldBeClosed() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 5);
        int rows = maze.getRows();
        for (int j = 0; j < maze.getCols(); j++) {
            assertTrue(maze.hasBottomWall(rows - 1, j),
                    "Bottom border missing at col " + j);
        }
    }

    // ==================== 3. СВЯЗНОСТЬ ====================

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 5, 10, 20, 50})
    @DisplayName("Лабиринт связный — BFS посещает все клетки")
    void generate_mazeShouldBeConnected(int size) {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(size, size);
        assertTrue(isConnected(maze),
                "Maze of size " + size + "x" + size + " is not connected");
    }

    @Test
    @DisplayName("Лабиринт 5x7 связный")
    void generate_rectangularMazeConnected() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 7);
        assertTrue(isConnected(maze));
    }

    // ==================== 4. ОТСУТСТВИЕ ПЕТЕЛЬ ====================

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 5, 10, 20, 50})
    @DisplayName("Число проходов = rows*cols - 1 (свойство дерева)")
    void generate_shouldHaveNoLoops(int size) {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(size, size);
        int passages = countPassages(maze);
        int expected = size * size - 1;
        assertEquals(expected, passages,
                "Maze of size " + size + "x" + size + " should have " +
                        expected + " passages, but has " + passages);
    }

    // ==================== 5. ДЕТЕРМИНИРОВАННОСТЬ ====================

    @Test
    @DisplayName("Один seed -> одинаковые лабиринты")
    void generate_sameSeedShouldProduceSameMaze() {
        Maze a = new EllerMazeGenerator(new Random(42)).generate(10, 10);
        Maze b = new EllerMazeGenerator(new Random(42)).generate(10, 10);
        assertMazesEqual(a, b);
    }

    @Test
    @DisplayName("Разные seeds -> разные лабиринты")
    void generate_differentSeedsShouldProduceDifferentMazes() {
        Maze a = new EllerMazeGenerator(new Random(1)).generate(10, 10);
        Maze b = new EllerMazeGenerator(new Random(2)).generate(10, 10);
        assertFalse(mazesEqual(a, b));
    }

    // ==================== 6. ВАЛИДАЦИЯ ====================

    @Test
    @DisplayName("rows = 0 -> исключение")
    void generate_zeroRowsShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new EllerMazeGenerator(new Random(42)).generate(0, 5));
    }

    @Test
    @DisplayName("cols = 0 -> исключение")
    void generate_zeroColsShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new EllerMazeGenerator(new Random(42)).generate(5, 0));
    }

    @Test
    @DisplayName("rows > 50 -> исключение")
    void generate_tooManyRowsShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new EllerMazeGenerator(new Random(42)).generate(51, 5));
    }

    @Test
    @DisplayName("cols > 50 -> исключение")
    void generate_tooManyColsShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new EllerMazeGenerator(new Random(42)).generate(5, 51));
    }

    @Test
    @DisplayName("Отрицательные размеры -> исключение")
    void generate_negativeSizeShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new EllerMazeGenerator(new Random(42)).generate(-1, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new EllerMazeGenerator(new Random(42)).generate(5, -1));
    }

    // ==================== 7. ГРАНИЧНЫЕ СЛУЧАИ ====================

    @Test
    @DisplayName("2x2 — маленький лабиринт, границы закрыты")
    void generate_smallSquareMaze() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(2, 2);
        assertEquals(2, maze.getRows());
        assertEquals(2, maze.getCols());
        assertTrue(maze.hasRightWall(0, 1));
        assertTrue(maze.hasRightWall(1, 1));
        assertTrue(maze.hasBottomWall(1, 0));
        assertTrue(maze.hasBottomWall(1, 1));
        assertTrue(isConnected(maze));
        assertEquals(3, countPassages(maze));   // rows*cols - 1 = 4 - 1 = 3
    }

    @Test
    @DisplayName("2x5 — все клетки соединены")
    void generate_singleRowMaze() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(2, 5);
        assertTrue(isConnected(maze));
        assertEquals(2 * 5 - 1, countPassages(maze));   // 9
    }

    @Test
    @DisplayName("5x2 — все клетки соединены")
    void generate_singleColumnMaze() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(5, 2);
        assertTrue(isConnected(maze));
        assertEquals(5 * 2 - 1, countPassages(maze));   // 9
    }

    @Test
    @DisplayName("50x50 — максимальный размер работает")
    void generate_maxSizeMaze() {
        Maze maze = new EllerMazeGenerator(new Random(42)).generate(50, 50);
        assertTrue(isConnected(maze));
        assertEquals(50 * 50 - 1, countPassages(maze));
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ ====================

    /** BFS от (0,0) — проверяет, что все клетки достижимы. */
    private boolean isConnected(Maze maze) {
        int rows = maze.getRows();
        int cols = maze.getCols();
        boolean[][] visited = new boolean[rows][cols];

        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{0, 0});
        visited[0][0] = true;

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int visitedCount = 1;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int i = cur[0], j = cur[1];

            for (int[] d : dirs) {
                int ni = i + d[0];
                int nj = j + d[1];

                if (ni < 0 || ni >= rows || nj < 0 || nj >= cols) continue;
                if (visited[ni][nj]) continue;
                if (!canMove(maze, i, j, ni, nj)) continue;

                visited[ni][nj] = true;
                visitedCount++;
                queue.add(new int[]{ni, nj});
            }
        }

        return visitedCount == rows * cols;
    }

    /** Свой canMove — тест НЕ зависит от MazeSolver. */
    private boolean canMove(Maze maze, int i, int j, int ni, int nj) {
        if (ni < 0 || ni >= maze.getRows() || nj < 0 || nj >= maze.getCols()) return false;
        if (ni == i && nj == j + 1) return !maze.hasRightWall(i, j);
        if (ni == i && nj == j - 1) return !maze.hasLeftWall(i, j);
        if (ni == i + 1 && nj == j) return !maze.hasBottomWall(i, j);
        if (ni == i - 1 && nj == j) return !maze.hasTopWall(i, j);
        return false;
    }

    /** Считает количество проходов (снятых стен). */
    private int countPassages(Maze maze) {
        int rows = maze.getRows();
        int cols = maze.getCols();
        int count = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (j < cols - 1 && !maze.hasRightWall(i, j)) count++;
                if (i < rows - 1 && !maze.hasBottomWall(i, j)) count++;
            }
        }
        return count;
    }

    /** Сравнивает два лабиринта по всем стенам. */
    private boolean mazesEqual(Maze a, Maze b) {
        if (a.getRows() != b.getRows() || a.getCols() != b.getCols()) return false;
        for (int i = 0; i < a.getRows(); i++) {
            for (int j = 0; j < a.getCols(); j++) {
                if (a.hasRightWall(i, j) != b.hasRightWall(i, j)) return false;
                if (a.hasBottomWall(i, j) != b.hasBottomWall(i, j)) return false;
            }
        }
        return true;
    }

    /** Проверяет, что два лабиринта идентичны. */
    private void assertMazesEqual(Maze a, Maze b) {
        assertEquals(a.getRows(), b.getRows());
        assertEquals(a.getCols(), b.getCols());
        for (int i = 0; i < a.getRows(); i++) {
            for (int j = 0; j < a.getCols(); j++) {
                assertEquals(a.hasRightWall(i, j), b.hasRightWall(i, j),
                        "Right wall differs at (" + i + "," + j + ")");
                assertEquals(a.hasBottomWall(i, j), b.hasBottomWall(i, j),
                        "Bottom wall differs at (" + i + "," + j + ")");
            }
        }
    }
}