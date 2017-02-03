package netvis.ui;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import netvis.model.input.TimeControllableFeeder;

public class RecordedPlaybackControls extends PlaybackControls {
    private Button restartButton;
    private Button slowerButton;
    private Button fasterButton;
    private Button endButton;

    public RecordedPlaybackControls(TimeControllableFeeder feeder) {
        super();

        restartButton = new Button();
        restartButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_restart.png"))));
        playbackButtons.add(restartButton);
        slowerButton = new Button();
        slowerButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_backward.png"))));
        playbackButtons.add(slowerButton);
        fasterButton = new Button();
        fasterButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_forward.png"))));
        playbackButtons.add(fasterButton);
        endButton = new Button();
        endButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_end.png"))));
        playbackButtons.add(endButton);

        infoLabel.setText("");

        playButton.setOnAction(event -> feeder.togglePlay());
        restartButton.setOnAction(event -> feeder.skipToStart());
        slowerButton.setOnAction(event -> feeder.slower());
        fasterButton.setOnAction(event -> feeder.faster());
        endButton.setOnAction(event -> feeder.skipToEnd());

        buttonsBox.getChildren().addAll(restartButton, slowerButton, fasterButton, endButton);
    }

    public void setOnDataEnd() {
        playButton.setDisable(true);
        slowerButton.setDisable(true);
        fasterButton.setDisable(true);
        endButton.setDisable(true);
        setPacketsPerSecondLabel("Packets / s: 0");
        setInfoLabelText("No more data available.");
    }

    public void setPlayIcon(boolean play) {
        if (play) {
            playButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_pause.png"))));
        } else {
            playButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_start.png"))));
        }
    }
}
