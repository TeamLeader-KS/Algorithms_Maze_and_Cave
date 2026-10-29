package org.example.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.example.domain.model.Cave;

public class CaveField extends Pane {

    private static final Color ALIVE_COLOR = Color.web("#00e5d0");

    private final double fieldWidth;
    private final double fieldHeight;
    private final Canvas canvas;

    private Cave cave;

    public CaveField(double width, double height) {
        this.fieldWidth = width;
        this.fieldHeight = height;

        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);

        canvas = new Canvas(width, height);
        getChildren().add(canvas);
    }

    public void setCave(Cave cave) {
        this.cave = cave;
        render();
    }

    public Cave getCave() {
        return cave;
    }

    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, fieldWidth, fieldHeight);

        if (cave == null) {
            return;
        }

        int rows = cave.getRows();
        int cols = cave.getCols();
        double cellW = fieldWidth / cols;
        double cellH = fieldHeight / rows;

        gc.setFill(ALIVE_COLOR);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cave.isAlive(i, j)) {
                    // округляем границы, чтобы между клетками не было щелей
                    double x0 = Math.round(j * cellW);
                    double x1 = Math.round((j + 1) * cellW);
                    double y0 = Math.round(i * cellH);
                    double y1 = Math.round((i + 1) * cellH);
                    gc.fillRect(x0, y0, x1 - x0, y1 - y0);
                }
            }
        }
    }
}