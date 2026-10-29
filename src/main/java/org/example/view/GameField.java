package org.example.view;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Spinner;
import org.example.domain.model.Maze;

import javafx.application.Platform;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

public class GameField extends Pane {

    private MediaPlayer player;

    private int activeSlider = 0;


    private static final Color ACCENT = Color.web("#00b8a9");
    private static final Color DARK = Color.web("#0a0e14");

    private static final double FIELD_SIZE = 500;
    private static final double BUTTON_SIZE = 200;

    // ===== ПОЛОЖЕНИЕ КНОПКИ ВЫХОД =====
    private static final double EXIT_BUTTON_X = 800;
    private static final double EXIT_BUTTON_Y = 680;

    // ===== ПОЛОЖЕНИЕ КНОПКИ ОЧИСТИТЬ ПОЛЕ =====
    private static final double CLEAR_BUTTON_X = 800;
    private static final double CLEAR_BUTTON_Y = 505;

    // ===== ПОЛОЖЕНИЕ КНОПОК SAVE и LOAD =====
    private static final double SAVE_LOAD_BUTTON_Y = 70;
    private static final double SAVE_BUTTON_X = 350;
    private static final double LOAD_BUTTON_X = 525  ;

    // ===== ПОЛОЖЕНИЕ SPINNER =====
    private static final double SPINNERS_Y = 215;
    private static final double SPINNER_COLS_X = 525;
    private static final double SPINNER_ROWS_X = 395;

    private Spinner<Integer> mazeRowsSpinner;
    private Spinner<Integer> mazeColsSpinner;

    // ===== ПОЛОЖЕНИЕ КНОПОК CONTROL =====
    private static final double CONTROL_BUTTONS_X = 50;
    private static final double GENERATION_BUTTON_Y = 330;

    // ===== ПОЛОЖЕНИЕ КНОПОК CAVE =====
    private static final double CHOOSING_THE_PATH_BUTTON_Y = 505;
    private static final double SOLUTION_MAZE_BUTTON_Y = 680;

    // ===== ПОЛОЖЕНИЕ КНОПОК CAVE =====
    private static final double AUTOPLAY_BUTTON_Y = 505;
    private static final double STEP_BY_STEP_BUTTON_Y = 680;

    private static final double SPEED_SLIDER_X = 800;
    private static final double SPEED_SLIDER_Y = 310;

    // ===== НАСТРОЙКИ РАМКИ =====
    private static final double BORDER_WIDTH = 20;                            // толщина рамки
    private static final double FRAME_SIZE = FIELD_SIZE + 2 * BORDER_WIDTH;   // 540
    private static final double FRAME_OPACITY = 0.55;   // прозрачность: 1.0 = сплошная, 0.0 = невидимая
    private static final double GLOW_RADIUS = 50;       // размер свечения
    private static final double GLOW_SPREAD = 0.35;     // плотность свечения (0..1)

    // ===== ПОЛОЖЕНИЕ ПАНЕЛИ ПОДСКАЗКИ =====
    private static final double HINT_BOX_X = 800;
    private static final double HINT_BOX_Y = 330;   // над «Очистить поле» (505)
    private static final double HINT_BOX_SIZE = 150;

    private StackPane hintBox;
    private Label hintLabel;

    private MazeField mazeField;
    private CaveField caveField;
    private Text titleText;

    // кнопки переключения вкладок
    private Button buttonMaze;
    private Button buttonCave;

    // общая кнопки (видны всегда)
    private Button buttonExit;
    private Button clearField;
    private Runnable onClear;

    // общие кнопки load / save

    private Button buttonSave;
    private Button buttonLoad;
    private Runnable onSave;
    private Runnable onLoad;

    private Runnable onGenerationMaze;
    private Runnable onChoosingThePath;
    private Runnable onSolutionMaze;

    private Runnable onGenerationCave;
    private Runnable onAutoPlayBack;
    private Runnable onStepByStepPlayback;

    // кнопки MAZE
    private Button buttonGenerationMaze;
    private Button buttonChoosingThePath;
    private Button buttonSolutionMaze;

    // кнопки CAVE
    private Button buttonAutoPlayBack;
    private Button buttonStepByStepPlayback;
    private Button buttonGenerationCave;
    private Label labelSpeed;
    private Label labelBirth;
    private Label labelDeath;
    private Slider gameSpeed;
    private Slider birthLimit;
    private Slider deathLimit;



    // группы кнопок: показываем только ту, что относится к открытой вкладке
    private Group mazeControls;
    private Group caveControls;

    private final DropShadow glow;

    public GameField() {
        setupGameFieldBackground();

        // свечение для активной кнопки
        glow = new DropShadow();
        glow.setColor(ACCENT);
        glow.setRadius(20);
        glow.setSpread(0.6);

        // РАМКА
        Rectangle frame = new Rectangle(FRAME_SIZE, FRAME_SIZE);
        frame.setFill(Color.color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), FRAME_OPACITY));
        frame.setEffect(new DropShadow(BlurType.GAUSSIAN, ACCENT, GLOW_RADIUS, GLOW_SPREAD, 0, 0));

        // ПОЛЕ
        Rectangle fieldBg = new Rectangle(BORDER_WIDTH, BORDER_WIDTH, FIELD_SIZE, FIELD_SIZE);
        fieldBg.setFill(DARK);

        // содержимое: лабиринт и пещера лежат на тех же 500x500
        mazeField = new MazeField(FIELD_SIZE, FIELD_SIZE);
        caveField = new CaveField(FIELD_SIZE, FIELD_SIZE);
        mazeField.relocate(BORDER_WIDTH, BORDER_WIDTH);
        caveField.relocate(BORDER_WIDTH, BORDER_WIDTH);

        // 4. всё вместе в обычном Pane (он ничего не растягивает)
        Pane fieldContainer = new Pane(frame, fieldBg, mazeField, caveField);
        fieldContainer.setPrefSize(FRAME_SIZE, FRAME_SIZE);
        fieldContainer.setMinSize(FRAME_SIZE, FRAME_SIZE);
        fieldContainer.setMaxSize(FRAME_SIZE, FRAME_SIZE);

        // кнопки вкладок
        buttonMaze = setupButtonMaze();
        buttonCave = setupButtonCave();

        // панель с текстом
        titleText = new Text("ЛАБИРИНТ");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleText.setFill(ACCENT);

        StackPane infoPanel = new StackPane(titleText);
        infoPanel.setPrefSize(300, 60);
        infoPanel.setMinSize(300, 60);
        infoPanel.setMaxSize(300, 60);
        infoPanel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(0,184,169,0.4);" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );

        // верхняя панель
        HBox topBar = new HBox(20, buttonMaze, infoPanel, buttonCave);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(20));

        // общая колонка
        VBox root = new VBox(20, topBar, fieldContainer);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + toRgba(ACCENT, 0.7) + ";" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 2;"
        );

        root.setPrefWidth(950);
        root.setMinWidth(950);
        root.setMaxWidth(950);

        // центрируем root
        root.layoutXProperty().bind(
                widthProperty().subtract(root.widthProperty()).divide(2)
        );
        root.layoutYProperty().bind(
                heightProperty().subtract(root.heightProperty()).divide(2)
        );

        // кнопки управления
        buttonExit = setupButtonExit();
        clearField = setuoClearField();

        mazeControls = setupMazeControls();
        caveControls = setupCaveControls();

        buttonSave = setupButtonSave();
        buttonLoad = setupButtonLoad();

        hintBox = createHintBox();

        getChildren().addAll(root, buttonExit, clearField, mazeControls, caveControls, buttonSave, buttonLoad, hintBox);

        // по умолчанию — лабиринт
        showMaze();
    }


    // ===== КНОПКИ ВКЛАДОК =====

    private Button setupButtonMaze() {
        Button button = new Button();

        try {
            Image img = new Image(getClass().getResourceAsStream("/images/MazeButton.png"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(BUTTON_SIZE);
            iv.setFitHeight(BUTTON_SIZE);
            iv.setPreserveRatio(true);

            button.setGraphic(iv);
        } catch (Exception e) {
            System.err.println("Ошибка пути к MazeButton.png");
            button.setText("MAZE");
            button.setTextFill(ACCENT);
        }

        button.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;"
        );

        button.setOnAction(e -> showMaze());
        return button;
    }

    private Button setupButtonCave() {
        Button button = new Button();

        try {
            Image img = new Image(getClass().getResourceAsStream("/images/CaveButton.png"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(BUTTON_SIZE);
            iv.setFitHeight(BUTTON_SIZE);
            iv.setPreserveRatio(true);

            button.setGraphic(iv);
        } catch (Exception e) {
            System.err.println("Ошибка пути к CaveButton.png");
            button.setText("CAVE");
            button.setTextFill(ACCENT);
        }

        button.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;"
        );

        button.setOnAction(e -> showCave());
        return button;
    }

    // ===== ОБЩИЕ КНОПКИ СОХРАННЕНИЕ и ЗАГРУЗКА ПОЛЕ (видны на обеих вкладках) =====

    private Button setupButtonSave() {
        Button button = ViewSaveLoadButtons.createIconButton("Сохранить");
        button.setLayoutX(SAVE_BUTTON_X);
        button.setLayoutY(SAVE_LOAD_BUTTON_Y);
        button.setOnAction(e -> {
            if (onSave != null) onSave.run();
            System.out.println("Поле сохраннено!");
        });
        return button;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    private Button setupButtonLoad() {
        Button button = ViewSaveLoadButtons.createIconButton("Загрузить");
        button.setLayoutX(LOAD_BUTTON_X);
        button.setLayoutY(SAVE_LOAD_BUTTON_Y);
        button.setOnAction(e -> {
            if (onLoad != null) onLoad.run();
            System.out.println("Поле Загружено!");
        });
        return button;
    }

    public void setOnLoad(Runnable onLoad) {
        this.onLoad = onLoad;
    }

    // ===== ОБЩИЕ КНОПКИ ВЫХОД и ОЧИСТИТЬ ПОЛЕ (видны на обеих вкладках) =====

    private Button setupButtonExit() {
        Button button = ViewControlButtons.createIconButton("Выход");
        button.setLayoutX(EXIT_BUTTON_X);
        button.setLayoutY(EXIT_BUTTON_Y);
        button.setOnAction(e -> SceneManager.getStage().close());
        return button;
    }

    public Button setuoClearField() {
        Button button = ViewControlButtons.createIconButton("Очистить\nполе");
        button.setLayoutX(CLEAR_BUTTON_X);
        button.setLayoutY(CLEAR_BUTTON_Y);
        button.setOnAction(e -> {
            if (onClear != null) onClear.run();
        });
        return button;
    }
    public void setOnClear(Runnable onClear) {
        this.onClear = onClear;
    }


    // ===== КНОПКИ ЛАБИРИНТА =====

    private Group setupMazeControls() {
        buttonGenerationMaze = setupButtonGenerationMaze();
        buttonChoosingThePath = setupButtonChoosingThePath();
        buttonSolutionMaze = setupButtonSolutionMaze();

        mazeRowsSpinner = createSizeSpinner(15);
        mazeColsSpinner = createSizeSpinner(15);

        Label rowsLabel = new Label("Строки:");
        Label colsLabel = new Label("Столбцы:");
        rowsLabel.setTextFill(ACCENT);
        colsLabel.setTextFill(ACCENT);
        rowsLabel.setAlignment(Pos.CENTER);
        colsLabel.setAlignment(Pos.CENTER);

        VBox rowsBox = new VBox(5, rowsLabel, mazeRowsSpinner);
        rowsBox.setAlignment(Pos.CENTER);
        rowsBox.setLayoutX(SPINNER_ROWS_X);
        rowsBox.setLayoutY(SPINNERS_Y);

        VBox colsBox = new VBox(5, colsLabel, mazeColsSpinner);
        colsBox.setAlignment(Pos.CENTER);
        colsBox.setLayoutX(SPINNER_COLS_X);
        colsBox.setLayoutY(SPINNERS_Y);

        return new Group(buttonGenerationMaze, buttonChoosingThePath, buttonSolutionMaze, rowsBox, colsBox);
    }

    private Button setupButtonGenerationMaze() {
        Button button = ViewControlButtons.createIconButton("Генерация\nЛабиринта");
        button.setLayoutX(CONTROL_BUTTONS_X);
        button.setLayoutY(GENERATION_BUTTON_Y);
        button.setOnAction(e -> {
            if(onGenerationMaze != null) onGenerationMaze.run();
            System.out.println("Генерация лабиринта!");
        });
        return button;
    }

    public void setOnGenerationMaze(Runnable onGenerationMaze) {
        this.onGenerationMaze = onGenerationMaze;
    }

    private Button setupButtonChoosingThePath() {
        Button button = ViewControlButtons.createIconButton("Выбор\nпути");
        button.setLayoutX(CONTROL_BUTTONS_X);
        button.setLayoutY(CHOOSING_THE_PATH_BUTTON_Y);
        button.setOnAction(e -> {
            if (onChoosingThePath != null) onChoosingThePath.run();
            System.out.println("Выбор пути!");
        });
        return button;
    }

    public void setOnChoosingThePath(Runnable onChoosingThePath) {
        this.onChoosingThePath = onChoosingThePath;
    }

    private Button setupButtonSolutionMaze() {
        Button button = ViewControlButtons.createIconButton("Решить\nлабиринт!");
        button.setLayoutX(CONTROL_BUTTONS_X);
        button.setLayoutY(SOLUTION_MAZE_BUTTON_Y);
        button.setOnAction(e -> {
            if (onSolutionMaze != null) onSolutionMaze.run();
            System.out.println("Решение лабиринта!");
        });
        return button;
    }

    public void setOnSolutionMaze(Runnable onSolutionMaze) {
        this.onSolutionMaze = onSolutionMaze;
    }


    // ===== КНОПКИ ПЕЩЕРЫ =====

    private Group setupCaveControls() {
        buttonAutoPlayBack = setupAutoPlayBack();
        buttonStepByStepPlayback = setupStepByStepPlayback();
        buttonGenerationCave = setupGenerationCave();


        // Слайдер скорости
        labelSpeed = new Label("Скорость: 1");
        labelSpeed.setFont(Font.font("System", FontWeight.BOLD, 14));
        labelSpeed.setTextFill(ACCENT);
        labelSpeed.setMaxWidth(Double.MAX_VALUE);
        labelSpeed.setAlignment(Pos.CENTER);

        gameSpeed = setupSpeedSlider();

        VBox speedBox = new VBox(8, labelSpeed, gameSpeed);
        speedBox.setPrefWidth(155);
        speedBox.setMinWidth(155);
        speedBox.setMaxWidth(155);
        speedBox.setLayoutX(SPEED_SLIDER_X);
        speedBox.setLayoutY(SPEED_SLIDER_Y);
        labelSpeed.setAlignment(Pos.CENTER);

        // Слайдер рождения
        labelBirth = new Label("пределы рождения: 0");
        labelBirth.setFont(Font.font("System", FontWeight.BOLD, 14));
        labelBirth.setTextFill(ACCENT);
        labelBirth.setPrefWidth(155);
        labelBirth.setMinWidth(155);
        labelBirth.setMaxWidth(155);
        labelBirth.setAlignment(Pos.CENTER);

        birthLimit = setupBirthLimit();

        VBox birthBox = new VBox(8, labelBirth, birthLimit);
        birthBox.setPrefWidth(155);
        birthBox.setMinWidth(155);
        birthBox.setMaxWidth(155);
        birthBox.setLayoutX(SPEED_SLIDER_X);
        birthBox.setLayoutY(SPEED_SLIDER_Y + 60);

        // Слайдер смерти
        labelDeath = new Label("пределы смерти: 0");
        labelDeath.setFont(Font.font("System", FontWeight.BOLD, 14));
        labelDeath.setTextFill(ACCENT);
        labelDeath.setPrefWidth(155);
        labelDeath.setMinWidth(155);
        labelDeath.setMaxWidth(155);
        labelDeath.setAlignment(Pos.CENTER);

        deathLimit = setupDeathLimit();

        VBox deathBox = new VBox(8, labelDeath, deathLimit);
        deathBox.setPrefWidth(155);
        deathBox.setMinWidth(155);
        deathBox.setMaxWidth(155);
        deathBox.setLayoutX(SPEED_SLIDER_X);
        deathBox.setLayoutY(SPEED_SLIDER_Y + 120);
        labelDeath.setAlignment(Pos.CENTER );

        updateSliderHighlight();

        return new Group(buttonAutoPlayBack, buttonStepByStepPlayback, buttonGenerationCave, speedBox, birthBox, deathBox);
    }

    private Button setupGenerationCave() {
        Button button = ViewControlButtons.createIconButton("Генерация\nПещеры");
        button.setLayoutX(CONTROL_BUTTONS_X);
        button.setLayoutY(GENERATION_BUTTON_Y);
        button.setOnAction(e -> {
            if (onGenerationCave != null) onGenerationCave.run();
                System.out.println("Генерация Пещеры");
        });
        return button;
    }

    public void setOnGenerationCave(Runnable onGenerationCave) {
        this.onGenerationCave = onGenerationCave;
    }


    private boolean autoPlayOn;

    private Button setupAutoPlayBack() {
        Button button = ViewControlButtons.createIconButton("Автоматическая\nОтрисовка");
        button.setLayoutX(CONTROL_BUTTONS_X);
        button.setLayoutY(AUTOPLAY_BUTTON_Y);
        button.setOnAction(e -> {
            if (onAutoPlayBack != null) onAutoPlayBack.run();
        });
        return button;
    }

    public void toggleAutoPlayColor() {
        autoPlayOn = !autoPlayOn;
        ViewControlButtons.setActive(buttonAutoPlayBack, autoPlayOn);
    }

    public void setOnAutoPlayBack(Runnable onAutoPlayBack) {
        this.onAutoPlayBack = onAutoPlayBack;
    }

    private Button setupStepByStepPlayback() {
        Button button = ViewControlButtons.createIconButton("Ручная\nОтрисовка");
        button.setLayoutX(CONTROL_BUTTONS_X);
        button.setLayoutY(STEP_BY_STEP_BUTTON_Y);
        button.setOnAction(e -> {
            if (onStepByStepPlayback != null) onStepByStepPlayback.run();
            System.out.println("Ручная Отрисовка");
        });
        return button;
    }

    public void setOnStepByStepPlayback(Runnable onStepByStepPlayback) {
        this.onStepByStepPlayback = onStepByStepPlayback;
    }

    private Slider setupSpeedSlider() {
        Slider slider = new Slider(-3, 3, 0);
        slider.setShowTickMarks(true);  // Показывает риски (чёрточки) на слайдере.
        slider.setShowTickLabels(true); // Показывает цифры под рисками. true — под каждой крупной риской будет число.
        slider.setBlockIncrement(1);  // Шаг, на который сдвигается ползунок при нажатии стрелок на клавиатуре или клике по полосе.
        slider.setMajorTickUnit(1);   // Расстояние между крупными рисками
        slider.setMinorTickCount(0);    // Сколько мелких рисок между двумя крупными. По умолчанию — 0 (мелких рисок нет).
        slider.setSnapToTicks(true);    // Ползунок «прилипает» к ближайшей риске.

        slider.valueProperty().addListener((obs, oldV, newV) -> {
            labelSpeed.setText(String.format("Скорость: %d", newV.intValue()));
            System.out.println("SLIDER = " + newV);   // ← проверка
        });


        return slider;
    }


    private Slider setupBirthLimit() {
        Slider slider = new Slider(0, 7, 0);
        slider.setShowTickMarks(true);  // Показывает риски (чёрточки) на слайдере.
        slider.setShowTickLabels(true); // Показывает цифры под рисками. true — под каждой крупной риской будет число.
        slider.setBlockIncrement(1);  // Шаг, на который сдвигается ползунок при нажатии стрелок на клавиатуре или клике по полосе.
        slider.setMajorTickUnit(1);   // Расстояние между крупными рисками
        slider.setMinorTickCount(0);    // Сколько мелких рисок между двумя крупными. По умолчанию — 0 (мелких рисок нет).
        slider.setSnapToTicks(true);    // Ползунок «прилипает» к ближайшей риске.

        slider.valueProperty().addListener((obs, oldV, newV) ->
                labelBirth.setText(String.format("пределы рождения: %d", newV.intValue())));

        return slider;
    }

    private Slider setupDeathLimit() {
        Slider slider = new Slider(0, 7, 0);
        slider.setShowTickMarks(true);  // Показывает риски (чёрточки) на слайдере.
        slider.setShowTickLabels(true); // Показывает цифры под рисками. true — под каждой крупной риской будет число.
        slider.setBlockIncrement(1);  // Шаг, на который сдвигается ползунок при нажатии стрелок на клавиатуре или клике по полосе.
        slider.setMajorTickUnit(1);   // Расстояние между крупными рисками
        slider.setMinorTickCount(0);    // Сколько мелких рисок между двумя крупными. По умолчанию — 0 (мелких рисок нет).
        slider.setSnapToTicks(true);    // Ползунок «прилипает» к ближайшей риске.

        slider.valueProperty().addListener((obs, oldV, newV) ->
                labelDeath.setText(String.format("пределы смерти: %d", newV.intValue())));

        return slider;
    }


    public double getGameSpeed() {
        return gameSpeed != null ? gameSpeed.getValue() : 1.0;
//        System.out.println("SPEED = " + view.getGameSpeed() + " → millis = " + SPEED_MILLIS[(int) view.getGameSpeed() + 3]);
    }


    // ===== ФОН =====

    public void setupGameFieldBackground() {
        try {
            var videoUrl = getClass().getResource("/videos/Matrix_room.mp4");
            if (videoUrl == null) {
                System.err.println("Видео не найдено: /videos/Matrix_room.mp4");
                setStyle("-fx-background-color: black;");
                return;
            }

            Media media = new Media(videoUrl.toExternalForm());
            player = new MediaPlayer(media);
            media.setOnError(() -> System.err.println("Media: " + media.getError()));
            player.setOnError(() -> System.err.println("Player: " + player.getError()));
            player.setAutoPlay(true);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            player.setMute(true);

            MediaView mediaView = new MediaView(player);
            mediaView.fitWidthProperty().bind(this.widthProperty());
            mediaView.fitHeightProperty().bind(this.heightProperty());
            mediaView.setPreserveRatio(false);

            this.getChildren().add(mediaView);
        } catch (Throwable e) {
            System.err.println("Ошибка загрузки видео: " + e);
            setStyle("-fx-background-color: black;");
        }
    }

    // ===== ПЕРЕКЛЮЧЕНИЕ ВКЛАДОК =====

    private void showMaze() {
        mazeField.setVisible(true);
        caveField.setVisible(false);
        mazeControls.setVisible(true);
        caveControls.setVisible(false);
        titleText.setText("ЛАБИРИНТ");
        hintBox.setVisible(true);
//        showHint("");
        setActive(buttonMaze, true);
        setActive(buttonCave, false);
    }

    private void showCave() {
        mazeField.setVisible(false);
        caveField.setVisible(true);
        mazeControls.setVisible(false);
        caveControls.setVisible(true);
        titleText.setText("ПЕЩЕРА");
        hintBox.setVisible(false);
        setActive(buttonMaze, false);
        setActive(buttonCave, true);
    }

    public void toggleTab() {
        if (isMazeTabActive()) {
            showCave();
        } else {
            showMaze();
        }
    }

    // сдвиг на 1
    public void changeSpeed(int direction) {
        if (direction > 0) {
            gameSpeed.increment();
        } else {
            gameSpeed.decrement();
        }
    }


    public MazeField getMazeField() { return mazeField; }
    public CaveField getCaveField() { return caveField; }
    public boolean isMazeTabActive() { return mazeField.isVisible(); }

    private void setActive(Button btn, boolean active) {
        btn.setOpacity(active ? 1.0 : 0.45);
        btn.setEffect(active ? glow : null);
    }

    private static String toRgba(Color c, double alpha) {
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255),
                alpha);
    }

    public void showHint(String text) {
        hintLabel.setText(text == null ? "" : text);
    }

    private StackPane createHintBox() {
        hintLabel = new Label("");
        hintLabel.setTextFill(ACCENT);
        hintLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        hintLabel.setWrapText(true);
        hintLabel.setTextAlignment(TextAlignment.CENTER);
        hintLabel.setAlignment(Pos.CENTER);

        StackPane box = new StackPane(hintLabel);
        box.setPrefSize(HINT_BOX_SIZE, HINT_BOX_SIZE);
        box.setMinSize(HINT_BOX_SIZE, HINT_BOX_SIZE);
        box.setMaxSize(HINT_BOX_SIZE, HINT_BOX_SIZE);
        box.setLayoutX(HINT_BOX_X);
        box.setLayoutY(HINT_BOX_Y);
        box.setMouseTransparent(true);
        box.setStyle(
                "-fx-background-color: rgba(0,184,169,0.08);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(0,184,169,0.5);" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );
        return box;
    }

    public int getBirthLimit() {
        return birthLimit != null ? (int) birthLimit.getValue() : 0;
    }

    public int getDeathLimit() {
        return deathLimit != null ? (int) deathLimit.getValue() : 0;
    }

    public void blockAutoPlay() {
        ViewControlButtons.blockAutoPlayBack(buttonAutoPlayBack);
    }

    public boolean isAutoPlayOn() {
        return autoPlayOn;
    }

    public void switchSlider(int delta) {
        activeSlider = (activeSlider + delta + 3) % 3;
        updateSliderHighlight();
    }

    public void changeActiveSlider(int direction) {
        Slider current = switch (activeSlider) {
            case 0 -> gameSpeed;
            case 1 -> birthLimit;
            default -> deathLimit;
        };
        if (direction > 0) {
            current.increment();
        } else {
            current.decrement();
        }
    }

    private void updateSliderHighlight() {
        labelSpeed.setTextFill(Color.GRAY);
        labelBirth.setTextFill(Color.GRAY);
        labelDeath.setTextFill(Color.GRAY);

        Label active = switch (activeSlider) {
            case 0 -> labelSpeed;
            case 1 -> labelBirth;
            default -> labelDeath;
        };
        active.setTextFill(ACCENT);
    }

    private Spinner<Integer> createSizeSpinner(int initial) {
        Spinner<Integer> spinner = new Spinner<>(Maze.MIN_SIZE, Maze.MAX_SIZE, initial);
        spinner.setPrefWidth(80);
        spinner.setEditable(true);
        spinner.setStyle(
                "-fx-background-color: rgba(0,184,169,0.08);" +
                        "-fx-border-color: rgba(0,184,169,0.5);" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        // только цифры
        spinner.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            String digits = newText.replaceAll("\\D", "");
            if (digits.length() > 2) digits = digits.substring(0, 2);
            if (!digits.equals(newText)) spinner.getEditor().setText(digits);
        });

        return spinner;
    }

    public Integer getMazeRows() {
        return mazeRowsSpinner.getValue();
    }

    public Integer getMazeCols() {
        return mazeColsSpinner.getValue();
    }

}