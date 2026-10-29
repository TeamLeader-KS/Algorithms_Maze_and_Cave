package org.example.view;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.util.Duration;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;

public class GreetingsBackground extends Pane {
    private ImageView Greetings;
    private Text title;
    private FadeTransition blink;

    private MediaPlayer player;   // поле класса (было локальной переменной)

    public GreetingsBackground() {
        setupGreetingsBackground();
        setuptitle();

        PixelParticles particles = new PixelParticles();
        particles.prefWidthProperty().bind(widthProperty());
        particles.prefHeightProperty().bind(heightProperty());
        getChildren().add(particles);
    }


    public void setupGreetingsBackground() {
        try {
            var videoUrl = getClass().getResource("/videos/GreetingsBackground.mp4");
            if (videoUrl == null) {
                System.err.println("Видео не найдено: /videos/GreetingsBackground.mp4");
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



    private void setuptitle() {
        title = new Text("Нажмите пробел для продолжения");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setFill(Color.CYAN);
        title.setMouseTransparent(true);

        // по центру, у нижнего края
        title.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> (getWidth() - title.getLayoutBounds().getWidth()) / 2,
                widthProperty(), title.layoutBoundsProperty()));
        title.layoutYProperty().bind(heightProperty().subtract(50));

        getChildren().add(title);

        // плавное мигание
        blink = new FadeTransition(Duration.seconds(1), title);
        blink.setFromValue(1.0);
        blink.setToValue(0.25);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.setAutoReverse(true);
        blink.play();       // ← запустить анимацию
    }

    public void stop() {
        if (blink != null) blink.stop();
        if (player != null) player.dispose();
    }

}
