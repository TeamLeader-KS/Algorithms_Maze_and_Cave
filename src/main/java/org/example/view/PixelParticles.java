package org.example.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PixelParticles extends Pane {

    private static final int COUNT = 30;          // количество частиц
    private static final double SIZE = 8;         // размер пикселя
    private static final double SPEED = 1.5;      // шагов за кадр
    private static final Color COLOR = Color.web("#00FFFF");

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private Timeline timeline;

    public PixelParticles() {
        setMouseTransparent(true);
        setPickOnBounds(false);

        for (int i = 0; i < COUNT; i++) {
            Particle p = createParticle();
            particles.add(p);
            getChildren().add(p.rect);
        }

        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private Particle createParticle() {
        Rectangle rect = new Rectangle(SIZE, SIZE);
        rect.setFill(COLOR);
        rect.setOpacity(0.15 + random.nextDouble() * 0.5);  // разные прозрачности

        Particle p = new Particle();
        p.rect = rect;
        p.x = random.nextDouble() * 900;
        p.y = random.nextDouble() * 700;
        p.dx = random.nextBoolean() ? SPEED : -SPEED;
        p.dy = random.nextBoolean() ? SPEED : -SPEED;
        p.stepsLeft = 20 + random.nextInt(80);   // сколько шагов до поворота

        rect.setTranslateX(p.x);
        rect.setTranslateY(p.y);
        return p;
    }

    private void update() {
        double w = getWidth();
        double h = getHeight();
        if (w == 0 || h == 0) return;

        for (Particle p : particles) {
            // движение
            p.x += p.dx;
            p.y += p.dy;
            p.stepsLeft--;

            // поворот под прямым углом
            if (p.stepsLeft <= 0) {
                if (random.nextBoolean()) {
                    p.dx = -p.dx;   // поворот по X
                } else {
                    p.dy = -p.dy;   // поворот по Y
                }
                p.stepsLeft = 20 + random.nextInt(80);
            }

            // отражение от границ
            if (p.x < 0 || p.x > w - SIZE) {
                p.dx = -p.dx;
                p.x = Math.max(0, Math.min(w - SIZE, p.x));
            }
            if (p.y < 0 || p.y > h - SIZE) {
                p.dy = -p.dy;
                p.y = Math.max(0, Math.min(h - SIZE, p.y));
            }

            p.rect.setTranslateX(p.x);
            p.rect.setTranslateY(p.y);
        }
    }

    private static class Particle {
        Rectangle rect;
        double x, y;
        double dx, dy;
        int stepsLeft;
    }
}