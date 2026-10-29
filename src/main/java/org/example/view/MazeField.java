package org.example.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import org.example.domain.model.Maze;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.util.function.BiConsumer;

import java.util.List;

public class MazeField extends Pane {

    private static final double WALL_WIDTH = 2;
    private static final double PATH_WIDTH = 2;
    private static final Color WALL_COLOR = Color.web("#00e5d0");
    private static final Color PATH_COLOR = Color.web("#00a4ff");
    private static final Color BACKGROUND_COLOR = Color.web("#00ffd0");
    private static final Color LETTERS_COLOR = Color.web("#00a4ff");

    private final double fieldWidth;
    private final double fieldHeight;
    private final Canvas canvas;

    private Maze maze;
    private List<int[]> path;

    // выбор пути
    private boolean interactive = false;
    private int[] startCell;    // {row, col} или null
    private int[] finishCell;   // {row, col} или null
    private BiConsumer<Integer, Integer> onCellClicked;

    public MazeField(double width, double height) {
        this.fieldWidth = width;
        this.fieldHeight = height;

        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);

        canvas = new Canvas(width, height);
        getChildren().add(canvas);

        setOnMouseClicked(e -> {
            if (!interactive || maze == null || onCellClicked == null) return;
            double[] cell = toCell(e.getX(), e.getY());
            int col = (int) cell[0];
            int row = (int) cell[1];
            if (row < 0 || col < 0 || row >= maze.getRows() || col >= maze.getCols()) return;
            onCellClicked.accept(row, col);
        });
    }

    // ===== API для контроллера =====

    public void setMaze(Maze maze) {
        this.maze = maze;
        this.path = null;
        this.startCell = null;
        this.finishCell = null;
        render();
    }

    public void setPath(List<int[]> path) {
        this.path = path;
        render();
    }

    public Maze getMaze() { return maze; }

    public void setInteractive(boolean interactive) { this.interactive = interactive; }

    public void setOnCellClicked(BiConsumer<Integer, Integer> handler) {
        this.onCellClicked = handler;
    }

    public void setStart(int row, int col) {
        this.startCell = new int[]{row, col};
        render();
    }

    public void setFinish(int row, int col) {
        this.finishCell = new int[]{row, col};
        render();
    }

    public void clearSelection() {
        startCell = null;
        finishCell = null;
        path = null;
        render();
    }

    // ===== геометрия =====

    private static final double PAD = WALL_WIDTH / 2;

    private double cellSize() {
        if (maze == null) return 0;
        double innerW = fieldWidth - WALL_WIDTH;
        double innerH = fieldHeight - WALL_WIDTH;
        return Math.min(innerW / maze.getCols(), innerH / maze.getRows());
    }

    private double offsetX() {
        if (maze == null) return PAD;
        double innerW = fieldWidth - WALL_WIDTH;
        return PAD + (innerW - cellSize() * maze.getCols()) / 2;
    }

    private double offsetY() {
        if (maze == null) return PAD;
        double innerH = fieldHeight - WALL_WIDTH;
        return PAD + (innerH - cellSize() * maze.getRows()) / 2;
    }

    private double[] toCell(double x, double y) {
        double size = cellSize();
        double col = (x - offsetX()) / size;
        double row = (y - offsetY()) / size;
        return new double[]{col, row};
    }

    // ===== отрисовка =====

    private void render() {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, fieldWidth, fieldHeight);

        if (maze == null) return;

        int rows = maze.getRows();
        int cols = maze.getCols();

        double innerWidth = fieldWidth - WALL_WIDTH;
        double innerHeight = fieldHeight - WALL_WIDTH;

        double cellSize = Math.min(innerWidth / cols, innerHeight / rows);

        double mazeWidth = cellSize * cols;   // реальная ширина лабиринта
        double mazeHeight = cellSize * rows;   // реальная высота лабиринта

        double offsetX = PAD + (innerWidth - mazeWidth) / 2;
        double offsetY = PAD + (innerHeight - mazeHeight) / 2;

        // подсветка старта / финиша

        if (startCell != null) {
            graphicsContext.setFill(BACKGROUND_COLOR.deriveColor(0, 1, 1, 0.25));
            graphicsContext.fillRect(
                    offsetX + startCell[1] * cellSize,
                    offsetY + startCell[0] * cellSize,
                    cellSize, cellSize);
        }
        if (finishCell != null) {
            graphicsContext.setFill(BACKGROUND_COLOR.deriveColor(0, 1, 1, 0.25));
            graphicsContext.fillRect(
                    offsetX + finishCell[1] * cellSize,
                    offsetY + finishCell[0] * cellSize,
                    cellSize, cellSize);
        }

        // путь

        if (path != null && path.size() > 1) {
            graphicsContext.setStroke(PATH_COLOR);
            graphicsContext.setLineWidth(PATH_WIDTH);
            graphicsContext.setLineCap(StrokeLineCap.ROUND);
            for (int k = 0; k < path.size() - 1; k++) {
                int[] a = path.get(k);
                int[] b = path.get(k + 1);
                graphicsContext.strokeLine(
                        offsetX + (a[1] + 0.5) * cellSize,
                        offsetY + (a[0] + 0.5) * cellSize,
                        offsetX + (b[1] + 0.5) * cellSize,
                        offsetY + (b[0] + 0.5) * cellSize);
            }
        }

        // стены

        graphicsContext.setStroke(WALL_COLOR);
        graphicsContext.setLineWidth(WALL_WIDTH);
        graphicsContext.setLineCap(StrokeLineCap.SQUARE);

        graphicsContext.strokeRect(offsetX, offsetY, mazeWidth, mazeHeight);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = offsetX + j * cellSize;
                double y = offsetY + i * cellSize;
                if (j < cols - 1 && maze.hasRightWall(i, j)) {
                    graphicsContext.strokeLine(x + cellSize, y, x + cellSize, y + cellSize);
                }
                if (i < rows - 1 && maze.hasBottomWall(i, j)) {
                    graphicsContext.strokeLine(x, y + cellSize, x + cellSize, y + cellSize);
                }
            }
        }

        // буквы S и F поверх всего

        graphicsContext.setFont(Font.font("System", FontWeight.BOLD, cellSize * 0.7));
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setTextBaseline(VPos.CENTER);

        if (startCell != null) {
            graphicsContext.setFill(LETTERS_COLOR);
            graphicsContext.fillText("S",
                    offsetX + (startCell[1] + 0.5) * cellSize,
                    offsetY + (startCell[0] + 0.5) * cellSize);
        }
        if (finishCell != null) {
            graphicsContext.setFill(LETTERS_COLOR);
            graphicsContext.fillText("F",
                    offsetX + (finishCell[1] + 0.5) * cellSize,
                    offsetY + (finishCell[0] + 0.5) * cellSize);
        }
    }
}