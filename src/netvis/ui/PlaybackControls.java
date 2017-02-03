package netvis.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import netvis.ui.controlsfx.Borders;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaybackControls {
    protected List<Button> playbackButtons;
    protected Button playButton;
    protected Label currentTimeLabel;
    protected Label speedRateLabel;
    protected Label packetsPerSecondLabel;
    protected Label infoLabel;
    protected HBox buttonsBox;
    protected VBox playbackBox;

    public PlaybackControls() {
        playbackButtons = new ArrayList<>();
        playButton = new Button();
        playButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_start.png"))));
        playbackButtons.add(playButton);
        currentTimeLabel = new Label("Current time: 0s");
        speedRateLabel = new Label("Speed rate: 1.0");
        packetsPerSecondLabel = new Label("Packets / s: 0");
        infoLabel = new Label();
        buttonsBox = new HBox(5);
        playbackBox = new VBox(5);
        buttonsBox.getChildren().addAll(playButton);
        playbackBox.getChildren().addAll(buttonsBox, currentTimeLabel, speedRateLabel, packetsPerSecondLabel, infoLabel);
    }

    public void toggleDisablePlaybackButtons() {
        for (Button b : playbackButtons) {
            b.setDisable(!b.isDisabled());
        }
    }

    public Node getPlaybackControls() {
        return Borders.wrap(playbackBox).lineBorder().thickness(1).radius(5).build().build();
    }

    public void setCurrentTimeLabelText(String text) {
        currentTimeLabel.setText(text);
    }

    public void setSpeedRateLabelText(String text) {
        speedRateLabel.setText(text);
    }

    public void setPacketsPerSecondLabel(String text) {
        packetsPerSecondLabel.setText(text);
    }

    public void setInfoLabelText(String text) {
        infoLabel.setText(text);
    }
}
