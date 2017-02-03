package netvis.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import netvis.ui.controlsfx.PopOver;

public class ControlsPopOver extends PopOver {
    private VBox vbox;

    public ControlsPopOver() {
        setArrowSize(0);
        setCornerRadius(5);
        setDetachedTitle("Controls");

        vbox = new VBox(10);
        vbox.setPadding(new Insets(20, 20, 20, 20));
        vbox.setPrefSize(350, 80);

        //create two dummy Region elements: one for playback controls, one for visualization controls
        vbox.getChildren().addAll(new Region(), new Region());
        setContentNode(vbox);
    }

    public void setPlaybackControls(Node playbackControlsNode) {
        vbox.getChildren().set(0, playbackControlsNode);
    }

    public void setVisualizationControls(Node visualizationControlsNode) {
        vbox.getChildren().set(1, visualizationControlsNode);
    }
}
