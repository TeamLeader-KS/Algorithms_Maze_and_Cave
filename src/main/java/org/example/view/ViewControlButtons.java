package org.example.view;

import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class ViewControlButtons {
    public static final Color ACCENT = Color.web("#00b8a9");
    private static final double CONTROL_BUTTON_SIZE = 150;

    private static final String STYLE_NORMAL =
            "-fx-background-color: rgba(0,184,169,0.05);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: rgba(0,184,169,0.5);" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;";

    private static final String STYLE_HOVER =
            "-fx-background-color: rgba(0,184,169,0.2);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: #00FFFF;" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;";

    private static final String STYLE_ACTIVE =
            "-fx-background-color: rgba(0,184,211,0.35);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: rgba(0,184,211,0.7);" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;";

    private static final String STYLE_BLOCK =
            "-fx-background-color: rgba(200,10,10,0.35);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: rgba(200,10,10,0.7);" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;";

    private ViewControlButtons() {}

    public static Button createIconButton(String text) {
        Button button = new Button(text);
        button.setTextFill(ACCENT);
        button.setFont(Font.font("System", FontWeight.BOLD, 14));
        button.setTextAlignment(TextAlignment.CENTER);

        button.setPrefSize(CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        button.setMinSize(CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        button.setMaxSize(CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        button.setStyle(STYLE_NORMAL);

        installHover(button);
        return button;
    }

    private static final java.util.Set<Button> activeButtons =
            java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    public static void installHover(Button button) {
        button.setOnMouseEntered(e -> {
            if (!activeButtons.contains(button)) {
                button.setStyle(STYLE_HOVER);
            }
        });
        button.setOnMouseExited(e -> {
            if (!activeButtons.contains(button)) {
                button.setStyle(STYLE_NORMAL);
            }
        });
    }

    public static void setActive(Button button, boolean active) {
        if (active) {
            activeButtons.add(button);
            button.setStyle(STYLE_ACTIVE);
        } else {
            activeButtons.remove(button);
            button.setStyle(STYLE_NORMAL);
        }
    }

    public static void blockAutoPlayBack(Button button) {
        button.setStyle(STYLE_BLOCK);
        PauseTransition pauseTransition = new PauseTransition(Duration.millis(200));
        pauseTransition.setOnFinished(e -> {
            if (activeButtons.contains(button)) {
                button.setStyle(STYLE_ACTIVE);
            } else {
                button.setStyle(STYLE_NORMAL);
            }
        });
        pauseTransition.play();
    }
}
