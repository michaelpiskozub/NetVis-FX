package netvis.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import netvis.ui.controlsfx.PopOver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AboutPopOver extends PopOver {
    public AboutPopOver() {
        setArrowSize(0);
        setCornerRadius(5);
        setDetachedTitle("About");

        Font helveticaBold = Font.loadFont(getClass().getResourceAsStream("/Helvetica CE Bold.ttf"), 15);
        Font helveticaRegular = Font.loadFont(getClass().getResourceAsStream("/Helvetica CE Regular.ttf"), 13);
        List<String> firstVersionAuthors = new ArrayList<>(Arrays.asList("James Nicholls", "Dominik Peters",
                "Albert Slawinski", "Thomas Spoor", "Sergiu Vicol"));
        String secondVersionAuthor = "Michal Piskozub";

        VBox vbox = new VBox(1);
        vbox.setPadding(new Insets(20, 20, 20, 20));
        vbox.setPrefSize(200, 200);

        Label firstVersionAuthorsLabel = new Label("NetVis 1.0.4 Authors");
        firstVersionAuthorsLabel.setFont(helveticaBold);
        VBox firstVersionAuthorsBox = new VBox(1);
        List<Label> authorsLabels = new ArrayList<>();
        for (String s : firstVersionAuthors) {
            Label l = new Label(s);
            l.setFont(helveticaRegular);
            authorsLabels.add(l);
        }
        firstVersionAuthorsBox.getChildren().addAll(authorsLabels);

        Label secondVersionAuthorLabel = new Label("NetVis 2.0 Author");
        secondVersionAuthorLabel.setFont(helveticaBold);
        Label author = new Label(secondVersionAuthor);

        vbox.getChildren().addAll(firstVersionAuthorsLabel, firstVersionAuthorsBox, secondVersionAuthorLabel, author);
        vbox.setMargin(secondVersionAuthorLabel, new Insets(20, 0, 0, 0));
        setContentNode(vbox);
    }
}
