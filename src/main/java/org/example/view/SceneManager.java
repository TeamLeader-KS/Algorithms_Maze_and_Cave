package org.example.view;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage stage;
    private static Scene scene;
    private static StackPane rootStack;

    public static void init(Stage primaryStage) {
        stage = primaryStage;

        rootStack = new StackPane();
        scene = new Scene(rootStack, 1000, 900); // 1 ширина - 2 высота

        stage.setScene(scene);
        stage.setAlwaysOnTop(true); // запуск поверх всех окон
        stage.setResizable(false);  // запрет на изменение размера окна
    }

    public static void switchTo(Node content) {
        rootStack.getChildren().setAll(content);
        stage.show();
    }

    public static void showOverlay(Node overlay) {
        if (!rootStack.getChildren().contains(overlay)) {
            rootStack.getChildren().add(overlay);
        }
        overlay.toFront();
    }

    public static void hideOverlay(Node overlay) {
        rootStack.getChildren().remove(overlay);
    }

    public static Stage getStage() {
        return stage;
    }
}