package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.controller.ApplicationController;
import org.example.domain.service.CaveServiceImpl;
import org.example.domain.service.MazeServiceImpl;
import org.example.view.*;

import org.example.view.GameField;
import org.example.view.SceneManager;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            System.out.println("Ошибка установки кодировки UTF-8");
        }

        SceneManager.init(stage);
        stage.setTitle("Алгоритмы");

        ApplicationController controller = new ApplicationController( new GameField(),
                new MazeServiceImpl(), new CaveServiceImpl());
        controller.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}