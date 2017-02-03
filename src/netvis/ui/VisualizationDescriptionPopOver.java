package netvis.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import netvis.ui.controlsfx.PopOver;

public class VisualizationDescriptionPopOver extends PopOver {
    private Text text;

    public VisualizationDescriptionPopOver() {
        setArrowSize(0);
        setCornerRadius(5);
        setDetachedTitle("Description");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20, 20, 20, 20));
        vbox.setPrefSize(300, 50);
        text = new Text();
        text.setFont(new Font(13));
        text.setWrappingWidth(260);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        vbox.getChildren().add(text);
        setContentNode(vbox);
    }

    public void setVisualizationDescription(String description) {
        text.setText(description);
    }
}
