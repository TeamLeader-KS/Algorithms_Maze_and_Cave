package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import org.example.controller.input.InputControl;
import org.example.controller.input.PlayerCommands;
import org.example.domain.model.Cave;
import org.example.domain.model.Maze;
import org.example.domain.service.CaveService;
import org.example.domain.service.MazeService;
import org.example.view.GameField;
import org.example.view.GreetingsBackground;
import org.example.view.SceneManager;
import java.util.List;

import javafx.animation.Animation;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;


// JavaFX умеет сама вызывает обработчик при каждом нажатии клавиши.

public class ApplicationController {

    private static final Path MAZE_SAVE_DIR = Path.of("data", "mazeSave");
    private static final Path CAVE_SAVE_DIR = Path.of("data", "caveSave");

    private static final String HINT_GENERATE_MAZE = "Сгенерируйте лабиринт";

    private static final int[] SPEED_MILLIS = {
            2000,   // -3
            1500,   // -2
            1250,   // -1
            1000,   //  0
            500,    // +1
            250,    // +2
            100     // +3
    };

    private Timeline caveTimeline;

    // пока размеры и шанс заданы константами, потом будут браться из полей ввода
//    private static final int MAZE_ROWS = 15;
//    private static final int MAZE_COLS = 15;
    private static final int CAVE_ROWS = 50;
    private static final int CAVE_COLS = 50;
    private static final double CAVE_ALIVE_CHANCE = 0.45;

    private GreetingsBackground greetings;
    private GameField view;


    private final MazeService mazeService;
    private final CaveService caveService;
    private final InputControl inputControl = new InputControl();

    private Maze maze;
    private Cave cave;

    private final EventHandler<KeyEvent> keyHandler = event -> {
        PlayerCommands command = inputControl.toCommand(event);
        if (command != PlayerCommands.NONE) {
            handleCommand(command);
            event.consume();   // чтобы TAB / стрелки / пробел не трогали фокус кнопок
        }
    };

    private enum PathStage { NONE, PICK_START, PICK_FINISH }
    private PathStage pathStage = PathStage.NONE;
    private int startRow = -1, startCol = -1;
    private int finishRow = -1, finishCol = -1;

    public ApplicationController(GameField view, MazeService mazeService, CaveService caveService) {
        this.mazeService = mazeService;
        this.caveService = caveService;
    }

    public void start() {
        greetings = new GreetingsBackground();
        SceneManager.switchTo(greetings);
        attach(SceneManager.getStage().getScene());
    }

    // Начать слушать клавиатуру.
    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
    }

    // Перестать слушать клавиатуру.
    public void detach(Scene scene) {
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
    }

    public void handleCommand(PlayerCommands command) {
        if (view == null && command != PlayerCommands.USE_SPACE && command != PlayerCommands.USE_EXIT) {
            return;
        }

        switch (command) {
            case USE_SPACE -> startApplication();
            case USE_TAB -> view.toggleTab();
            case USE_CLEAR -> clear();

            case MOVE_LEFT -> {
                if (!view.isMazeTabActive()) view.changeActiveSlider(-1);
            }
            case MOVE_RIGHT -> {
                if (!view.isMazeTabActive()) view.changeActiveSlider(+1);
            }
            case MOVE_UP -> {
                if (!view.isMazeTabActive()) view.switchSlider(-1);
            }
            case MOVE_DOWN -> {
                if (!view.isMazeTabActive()) view.switchSlider(+1);
            }

            case USE_CONTROL_1 -> controlFirst();
            case USE_CONTROL_2 -> controlSecond();
            case USE_CONTROL_3 -> controlThird();
            case USE_EXIT -> SceneManager.getStage().close();

            default -> { }
        }
    }

    private void startApplication() {
        if (view != null) return;

        greetings.stop();
        view = new GameField();
        view.setOnClear(this::clear);
        // лабиринт
        view.setOnGenerationMaze(this::generateMaze);
        view.setOnChoosingThePath(this::choosingThePath);
        view.getMazeField().setOnCellClicked(this::onMazeCellClicked);
        view.setOnSolutionMaze(this::solutionMaze);
        // пещера
        view.setOnGenerationCave(this::generateCave);
        view.setOnAutoPlayBack(this::autoPlayBack);
        view.setOnStepByStepPlayback(this::stepByStepPlayback);
        SceneManager.switchTo(view);

        view.setOnSave(this::save);
        view.setOnLoad(this::load);


        view.showHint(HINT_GENERATE_MAZE);
    }

    private void controlFirst() {
        if (view.isMazeTabActive()) {
            generateMaze();
        } else {
            generateCave();
        }
    }

    private void controlSecond() {
        if (view.isMazeTabActive()) {
            choosingThePath();
        } else {
            autoPlayBack();
        }
    }

    private void controlThird() {
        if (view.isMazeTabActive()) {
            solutionMaze();
        } else {
            stepByStepPlayback();
        }
    }

    private void generateMaze() {
        Integer rows = view.getMazeRows();
        Integer cols = view.getMazeCols();

        if (rows == null || cols == null) {
            view.showHint("Нужно задать РАЗМЕРЫ лабиринта!");
            return;
        }

        maze = mazeService.generate(rows, cols);
        view.getMazeField().setMaze(maze);
        if (maze != null) {
            view.showHint("Перейдите к выбору пути");
        }
    }

    private void choosingThePath() {
        if (maze == null) {
            view.showHint("Сначала сгенерируйте лабиринт!");
            return;
        }

        pathStage = PathStage.PICK_START;
        startRow = startCol = finishRow = finishCol = -1;

        view.getMazeField().clearSelection();
        view.getMazeField().setInteractive(true);
        view.showHint("Выберите НАЧАЛО пути");
    }

    private void onMazeCellClicked(int row, int col) {
        if (maze == null) return;

        switch (pathStage) {
            case PICK_START -> {
                startRow = row;
                startCol = col;
                view.getMazeField().setStart(row, col);

                pathStage = PathStage.PICK_FINISH;
                view.showHint("Выберите КОНЕЦ  пути");
            }
            case PICK_FINISH -> {
                if (row == startRow && col == startCol) {
                    view.showHint("Финиш не может совпадать со стартом!");
                    return;
                }
                finishRow = row;
                finishCol = col;
                view.getMazeField().setFinish(row, col);

                pathStage = PathStage.NONE;
                view.getMazeField().setInteractive(false);
                view.showHint("Нажмите «Решить лабиринт!»");
            }
            case NONE -> { }
        }
    }

    private void solutionMaze() {
        if (maze == null) {
            view.showHint("Сначала сгенерируйте лабиринт!");
            return;
        }

        boolean hasStart = startRow >= 0 && startCol >= 0;
        boolean hasFinish = finishRow >= 0 && finishCol >= 0;

        if (!hasStart && !hasFinish) {
            view.showHint("Сначала выберите начало и конец пути!");
            return;
        }
        if (hasStart && !hasFinish) {
            view.showHint("Сначала выберите конец пути!");
            return;
        }

        List<int[]> path = mazeService.solve(maze, startRow, startCol, finishRow, finishCol);

        if (path == null || path.isEmpty()) {
            view.showHint("Путь не найден!");
            return;
        }

        view.getMazeField().setPath(path);
        view.showHint("Путь найден!");
    }

    private void generateCave() {
        cave = caveService.generate(CAVE_ROWS, CAVE_COLS, CAVE_ALIVE_CHANCE);
        view.getCaveField().setCave(cave);


        cave = caveService.generate(CAVE_ROWS, CAVE_COLS, CAVE_ALIVE_CHANCE);
        view.getCaveField().setCave(cave);
    }

        private void autoPlayBack() {
            if (cave == null) {
                view.blockAutoPlay();
                return;
            }

            // ПАУЗА: если играет — остановить и выключить кнопку
            if (caveTimeline != null && caveTimeline.getStatus() == Animation.Status.RUNNING) {
                caveTimeline.stop();
                caveTimeline = null;
                view.toggleAutoPlayColor();
                return;
            }

            view.toggleAutoPlayColor();


            // СКОРОСТЬ
            int index = (int) view.getGameSpeed() + 3;
            double millis = SPEED_MILLIS[index];
            System.out.println("SPEED=" + view.getGameSpeed() + " index=" + index + " millis=" + millis);

        caveTimeline = new Timeline(new KeyFrame(Duration.millis(millis), e -> {
            cave = caveService.step(cave, view.getBirthLimit(), view.getDeathLimit());
            view.getCaveField().setCave(cave);
        }));
        caveTimeline.setCycleCount(Animation.INDEFINITE);
        caveTimeline.play();
    }

    private void stepByStepPlayback() {
        if (cave == null) {
            return;
        }
        cave = caveService.step(cave, view.getBirthLimit(), view.getDeathLimit());
        view.getCaveField().setCave(cave);
    }

    private void stopAutoPlayBack() {
        if (view.isAutoPlayOn()) autoPlayBack();
    }


    private void clear() {
        pathStage = PathStage.NONE;
        startRow = startCol = finishRow = finishCol = -1;

        if (view.isMazeTabActive()) {
            maze = null;
            view.getMazeField().setInteractive(false);
            view.getMazeField().clearSelection();
            view.getMazeField().setMaze(null);
            view.showHint(HINT_GENERATE_MAZE);
        } else {
            stopAutoPlayBack();
            cave = null;
            view.getCaveField().setCave(null);
        }
    }

    private void save() {
        if (view.isMazeTabActive()) {
            saveMaze();
        } else {
            saveCave();
        }
    }

    private void load() {
        if (view.isMazeTabActive()) {
            loadMaze();
        } else {
            loadCave();
        }
    }

    // ===== лабиринт =====

    private void saveMaze() {
        if (maze == null) {
            showError("Нечего сохранять: сначала сгенерируйте или загрузите лабиринт.");
            return;
        }
        File file = chooseFile("Сохранить лабиринт", MAZE_SAVE_DIR, "maze.txt", true);
        if (file == null) return;   // окно закрыли без выбора

        try {
            mazeService.save(maze, file.toPath());
        } catch (UncheckedIOException e) {
            showError("Не удалось сохранить файл:\n" + e.getCause().getMessage());
        }
    }

    private void loadMaze() {
        File file = chooseFile("Загрузить лабиринт", MAZE_SAVE_DIR, null, false);
        if (file == null) return;

        try {
            maze = mazeService.load(file.toPath());
            view.getMazeField().setMaze(maze);
        } catch (UncheckedIOException e) {
            showError("Не удалось загрузить лабиринт:\n" + e.getCause().getMessage());
        }
    }

    // ===== пещера =====

    private void saveCave() {
        if (cave == null) {
            showError("Нечего сохранять: сначала сгенерируйте или загрузите пещеру.");
            return;
        }
        File file = chooseFile("Сохранить пещеру", CAVE_SAVE_DIR, "cave.txt", true);
        if (file == null) return;

        try {
            caveService.save(cave, file.toPath());
        } catch (UncheckedIOException e) {
            showError("Не удалось сохранить файл:\n" + e.getCause().getMessage());
        }
    }

    private void loadCave() {
        File file = chooseFile("Загрузить пещеру", CAVE_SAVE_DIR, null, false);
        if (file == null) return;

        try {
            cave = caveService.load(file.toPath());
            view.getCaveField().setCave(cave);
        } catch (UncheckedIOException e) {
            showError("Не удалось загрузить пещеру:\n" + e.getCause().getMessage());
        }
    }

    // ===== общие помощники =====
    
    private File chooseFile(String title, Path dir, String defaultName, boolean forSave) {
        try {
            Files.createDirectories(dir);   // если папки нет — создаём
        } catch (IOException ignored) {
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (Files.isDirectory(dir)) {
            chooser.setInitialDirectory(dir.toAbsolutePath().toFile());
        }
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовые файлы (*.txt)", "*.txt"));

        if (forSave) {
            chooser.setInitialFileName(defaultName);
            return chooser.showSaveDialog(SceneManager.getStage());
        }
        return chooser.showOpenDialog(SceneManager.getStage());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initOwner(SceneManager.getStage());
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private int activeSlider = 0;



}