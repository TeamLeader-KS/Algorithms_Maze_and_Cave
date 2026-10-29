package org.example.domain.generator;

import org.example.domain.model.Cave;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CaveGeneratorTest {

    // ==================== 0. КОНСТРУКТОРЫ ====================

    @Test
    @DisplayName("CaveGenerator: конструктор без параметров работает")
    void constructor_noArgs_works() {
        CaveGenerator gen = new CaveGenerator();
        assertNotNull(gen);

        Cave cave = gen.generate(10, 10, 0.45);
        assertEquals(10, cave.getRows());
        assertEquals(10, cave.getCols());
    }

    @Test
    @DisplayName("CaveGenerator: конструктор с Random работает")
    void constructor_withRandom_works() {
        CaveGenerator gen = new CaveGenerator(new Random(42));
        assertNotNull(gen);

        Cave cave = gen.generate(10, 10, 0.45);
        assertEquals(10, cave.getRows());
        assertEquals(10, cave.getCols());
    }

    @Test
    @DisplayName("CaveGenerator: null Random -> исключение")
    void constructor_nullRandom_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaveGenerator((Random) null));
    }

    // ==================== 1. GENERATE: РАЗМЕРЫ ====================

    @Test
    @DisplayName("generate: размеры пещеры совпадают с заданными")
    void generate_shouldHaveCorrectSize() {
        Cave cave = new CaveGenerator(new Random(42)).generate(20, 30, 0.45);
        assertEquals(20, cave.getRows());
        assertEquals(30, cave.getCols());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10, 25, 50})
    @DisplayName("generate: разные размеры генерируются корректно")
    void generate_shouldWorkForVariousSizes(int size) {
        Cave cave = new CaveGenerator(new Random(42)).generate(size, size, 0.45);
        assertEquals(size, cave.getRows());
        assertEquals(size, cave.getCols());
    }

    // ==================== 2. GENERATE: ИНИЦИАЛИЗАЦИЯ ====================

    @Test
    @DisplayName("generate: aliveChance = 0 -> все клетки мёртвые")
    void generate_zeroChance_allDead() {
        Cave cave = new CaveGenerator(new Random(42)).generate(10, 10, 0.0);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertFalse(cave.isAlive(i, j),
                        "Cell (" + i + "," + j + ") should be dead");
            }
        }
    }

    @Test
    @DisplayName("generate: aliveChance = 1 -> все клетки живые")
    void generate_fullChance_allAlive() {
        Cave cave = new CaveGenerator(new Random(42)).generate(10, 10, 1.0);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertTrue(cave.isAlive(i, j),
                        "Cell (" + i + "," + j + ") should be alive");
            }
        }
    }

    @Test
    @DisplayName("generate: aliveChance = 0.45 -> примерно 45% живых")
    void generate_partialChance_about45Percent() {
        Cave cave = new CaveGenerator(new Random(42)).generate(50, 50, 0.45);

        int alive = 0;
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                if (cave.isAlive(i, j)) alive++;
            }
        }
        double ratio = alive / 2500.0;
        assertTrue(ratio > 0.40 && ratio < 0.50,
                "Alive ratio should be ~0.45, but was " + ratio);
    }

    // ==================== 3. GENERATE: ДЕТЕРМИНИРОВАННОСТЬ ====================

    @Test
    @DisplayName("generate: один seed -> одинаковые пещеры")
    void generate_sameSeed_sameCave() {
        Cave a = new CaveGenerator(new Random(42)).generate(20, 20, 0.45);
        Cave b = new CaveGenerator(new Random(42)).generate(20, 20, 0.45);
        assertCavesEqual(a, b);
    }

    @Test
    @DisplayName("generate: разные seeds -> разные пещеры")
    void generate_differentSeeds_differentCaves() {
        Cave a = new CaveGenerator(new Random(1)).generate(20, 20, 0.45);
        Cave b = new CaveGenerator(new Random(2)).generate(20, 20, 0.45);
        assertFalse(cavesEqual(a, b));
    }

    // ==================== 4. GENERATE: ВАЛИДАЦИЯ ====================

    @Test
    @DisplayName("generate: rows = 0 -> исключение")
    void generate_zeroRows_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaveGenerator(new Random(42)).generate(0, 10, 0.45));
    }

    @Test
    @DisplayName("generate: cols = 0 -> исключение")
    void generate_zeroCols_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaveGenerator(new Random(42)).generate(10, 0, 0.45));
    }

    @Test
    @DisplayName("generate: rows > 50 -> исключение")
    void generate_tooManyRows_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaveGenerator(new Random(42)).generate(51, 10, 0.45));
    }

    @Test
    @DisplayName("generate: cols > 50 -> исключение")
    void generate_tooManyCols_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaveGenerator(new Random(42)).generate(10, 51, 0.45));
    }

    @Test
    @DisplayName("generate: aliveChance < 0 -> исключение")
    void generate_negativeChance_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaveGenerator(new Random(42)).generate(10, 10, -0.1));
    }

    @Test
    @DisplayName("generate: aliveChance > 1 -> исключение")
    void generate_chanceAboveOne_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaveGenerator(new Random(42)).generate(10, 10, 1.1));
    }

    // ==================== 5. STEP: РАЗМЕРЫ И ДЕТЕРМИНИРОВАННОСТЬ ====================

    @Test
    @DisplayName("step: размеры не меняются")
    void step_keepsSameSize() {
        CaveGenerator gen = new CaveGenerator(new Random(42));
        Cave cave = gen.generate(15, 20, 0.45);
        Cave next = gen.step(cave, 4, 4);

        assertEquals(15, next.getRows());
        assertEquals(20, next.getCols());
    }

    @Test
    @DisplayName("step: один seed -> одинаковый результат")
    void step_sameSeed_sameResult() {
        CaveGenerator gen1 = new CaveGenerator(new Random(42));
        CaveGenerator gen2 = new CaveGenerator(new Random(42));

        Cave cave1 = gen1.generate(20, 20, 0.45);
        Cave cave2 = gen2.generate(20, 20, 0.45);

        Cave next1 = gen1.step(cave1, 4, 4);
        Cave next2 = gen2.step(cave2, 4, 4);

        assertCavesEqual(next1, next2);
    }

    @Test
    @DisplayName("step: одинаковый вход -> одинаковый выход (детерминированность)")
    void step_deterministic() {
        CaveGenerator gen = new CaveGenerator(new Random(42));
        Cave cave = gen.generate(20, 20, 0.45);

        Cave next1 = gen.step(cave, 4, 4);
        Cave next2 = gen.step(cave, 4, 4);

        assertCavesEqual(next1, next2);
    }

    // ==================== 6. STEP: ПРАВИЛА ====================

    @Test
    @DisplayName("step: мёртвая клетка с 8 живыми соседями оживает")
    void step_deadCellWithManyNeighbors_bornAlive() {
        Cave cave = new Cave(3, 3);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cave.setCell(i, j, true);
            }
        }
        cave.setCell(1, 1, false);

        CaveGenerator gen = new CaveGenerator(new Random(42));
        Cave next = gen.step(cave, 3, 4);

        assertTrue(next.isAlive(1, 1), "Dead cell with 8 alive neighbors should be born");
    }

    @Test
    @DisplayName("step: живая клетка с 0 живыми соседями умирает")
    void step_aliveCellWithNoNeighbors_dies() {
        Cave cave = new Cave(3, 3);
        cave.setCell(1, 1, true);

        CaveGenerator gen = new CaveGenerator(new Random(42));
        Cave next = gen.step(cave, 4, 4);

        assertFalse(next.isAlive(1, 1), "Alive cell with 0 neighbors should die");
    }

    // ==================== 7. STEP: ВАЛИДАЦИЯ ====================

    @Test
    @DisplayName("step: null cave -> исключение")
    void step_nullCave_throws() {
        CaveGenerator gen = new CaveGenerator(new Random(42));
        assertThrows(IllegalArgumentException.class,
                () -> gen.step(null, 4, 4));
    }

    @Test
    @DisplayName("step: birthLimit вне [0,7] -> исключение")
    void step_birthLimitOutOfRange_throws() {
        CaveGenerator gen = new CaveGenerator(new Random(42));
        Cave cave = gen.generate(10, 10, 0.45);

        assertThrows(IllegalArgumentException.class,
                () -> gen.step(cave, -1, 4));
        assertThrows(IllegalArgumentException.class,
                () -> gen.step(cave, 8, 4));
    }

    @Test
    @DisplayName("step: deathLimit вне [0,7] -> исключение")
    void step_deathLimitOutOfRange_throws() {
        CaveGenerator gen = new CaveGenerator(new Random(42));
        Cave cave = gen.generate(10, 10, 0.45);

        assertThrows(IllegalArgumentException.class,
                () -> gen.step(cave, 4, -1));
        assertThrows(IllegalArgumentException.class,
                () -> gen.step(cave, 4, 8));
    }

    // ==================== 8. ГРАНИЧНЫЕ СЛУЧАИ ====================

    @Test
    @DisplayName("1x1: одна клетка, шаг работает")
    void generate_singleCell() {
        CaveGenerator gen = new CaveGenerator(new Random(42));
        Cave cave = gen.generate(1, 1, 1.0);
        Cave next = gen.step(cave, 4, 4);

        assertEquals(1, next.getRows());
        assertEquals(1, next.getCols());
    }

    @Test
    @DisplayName("50x50: максимальный размер работает")
    void generate_maxSize() {
        CaveGenerator gen = new CaveGenerator(new Random(42));
        Cave cave = gen.generate(50, 50, 0.45);
        Cave next = gen.step(cave, 4, 4);

        assertEquals(50, next.getRows());
        assertEquals(50, next.getCols());
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ ====================

    private boolean cavesEqual(Cave a, Cave b) {
        if (a.getRows() != b.getRows() || a.getCols() != b.getCols()) return false;
        for (int i = 0; i < a.getRows(); i++) {
            for (int j = 0; j < a.getCols(); j++) {
                if (a.isAlive(i, j) != b.isAlive(i, j)) return false;
            }
        }
        return true;
    }

    private void assertCavesEqual(Cave a, Cave b) {
        assertEquals(a.getRows(), b.getRows());
        assertEquals(a.getCols(), b.getCols());
        for (int i = 0; i < a.getRows(); i++) {
            for (int j = 0; j < a.getCols(); j++) {
                assertEquals(a.isAlive(i, j), b.isAlive(i, j),
                        "Cell (" + i + "," + j + ") differs");
            }
        }
    }
}