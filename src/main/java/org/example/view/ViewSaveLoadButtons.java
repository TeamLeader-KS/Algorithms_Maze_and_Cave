package org.example.view;

import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class ViewSaveLoadButtons {
    public static final Color ACCENT = Color.web("#00b8a9");

    private static final double SAVE_LOAD_BUTTON_SIZE_X = 125;
    private static final double SAVE_LOAD_BUTTON_SIZE_Y = 50;

    private ViewSaveLoadButtons() {}

    // обычное состояние
    private static final String STYLE_NORMAL =
            "-fx-background-color: rgba(0,184,169,0.05);" +
                    "-fx-background-insets: 0;" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: rgba(0,184,169,0.5);" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;";

    // при наведении — яркая рамка + свечение
    private static final String STYLE_HOVER =
            "-fx-background-color: rgba(0,184,169,0.2);" +
                    "-fx-background-radius: 12;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-color: #00FFFF;" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;";

    // «активное» состояние (например, если кнопка что-то подтвердила)
    private static final String STYLE_ACTIVE =
            "-fx-background-color: rgba(0,184,211,0.35);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: rgba(0,184,211,0.7);" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;";

    public static Button createIconButton(String text) {
        Button button = new Button(text);
        button.setTextFill(ACCENT);
        button.setFont(Font.font("System", FontWeight.BOLD, 14));
        button.setTextAlignment(TextAlignment.CENTER);

        button.setPrefSize(SAVE_LOAD_BUTTON_SIZE_X, SAVE_LOAD_BUTTON_SIZE_Y);
        button.setMinSize(SAVE_LOAD_BUTTON_SIZE_X, SAVE_LOAD_BUTTON_SIZE_Y);
        button.setMaxSize(SAVE_LOAD_BUTTON_SIZE_X, SAVE_LOAD_BUTTON_SIZE_Y);

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
}