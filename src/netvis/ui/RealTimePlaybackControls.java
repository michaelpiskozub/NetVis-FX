package netvis.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import netvis.model.input.PlayableFeeder;

public class RealTimePlaybackControls extends PlaybackControls {
    public RealTimePlaybackControls(PlayableFeeder feeder) {
        super();

        infoLabel.setText("");

        playButton.setOnAction(event -> feeder.togglePlay());
    }

    public void setPlayIcon(boolean play) {
        if (play) {
            playButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_stop.png"))));
        } else {
            playButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/blue_start.png"))));
        }
    }
}
