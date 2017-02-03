package netvis.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.converter.NumberStringConverter;
import netvis.ui.controlsfx.Borders;
import netvis.ui.controlsfx.PopOver;
import netvis.view.util.fx.Planet;

import java.util.ArrayList;
import java.util.List;

public class PlanetsPopOver extends PopOver {
    private List<Planet> planets;
    private Font font;
    private FlowPane flowPane;
    private VBox vbox;

    public PlanetsPopOver() {
        setArrowSize(0);
        setCornerRadius(5);
        setDetachedTitle("Planets");

        planets = new ArrayList<>();
        font = Font.loadFont(getClass().getResourceAsStream("/Helvetica CE Bold.ttf"), 12);
        flowPane = new FlowPane();
        flowPane.setPrefWrapLength(650);
        flowPane.setPrefSize(710, 530);
        flowPane.setPadding(new Insets(10, 10, 10, 10));

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> reset());
        vbox = new VBox();
        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        vbox.setMargin(clearButton, new Insets(0, 0, 10, 20));
        vbox.getChildren().addAll(clearButton, scroll);

        setContentNode(vbox);
    }

    public void reset() {
        flowPane.getChildren().clear();
        planets.clear();
    }

    public void addPlanetText(Planet planet) {
        if (!planets.contains(planet)) {
            VBox planetTextBox = new VBox();
            planetTextBox.setPrefSize(120, 40);

            Label planetTypeLabel = new Label(planet.getType());

            Label planetNameLabel = new Label(planet.getName());
            planetNameLabel.setFont(font);

            HBox hitsBox = new HBox(5);
            Label hitsLabel = new Label("Hits: ");
            Label hitsValueLabel = new Label();
            hitsValueLabel.textProperty().bindBidirectional(planet.getHitsProperty(), new NumberStringConverter());
            hitsBox.getChildren().addAll(hitsLabel, hitsValueLabel);

            HBox totalLengthBox = new HBox(5);
            Label totalLengthLabel = new Label("Length: ");
            Label totalLengthValueLabel = new Label();
            totalLengthValueLabel.textProperty().bindBidirectional(planet.getTotalLengthProperty(),
                    new NumberStringConverter());
            totalLengthBox.getChildren().addAll(totalLengthLabel, totalLengthValueLabel);

            if (!planet.getType().equals("")) {
                planetTextBox.getChildren().add(planetTypeLabel);
            }
            planetTextBox.getChildren().addAll(planetNameLabel, hitsBox, totalLengthBox);
            Node wrappedBox = Borders.wrap(planetTextBox).lineBorder().thickness(1).radius(5).build().build();
            flowPane.getChildren().addAll(wrappedBox);
            planets.add(planet);
        }
    }
}
