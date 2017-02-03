package netvis.ui;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import netvis.ui.controlsfx.PopOver;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

public class TsharkSettingsPopOver extends PopOver {
    private StringProperty tsharkPathProperty;
    private StringProperty captureInterfaceProperty;
    private Map<String, String> captureInterfaces;
    private VBox vbox;
    private TextField pathTextField;
    private ComboBox<String> interfacesComboBox;
    private ChangeListener<String> interfacesComboBoxListener;

    public TsharkSettingsPopOver(StringProperty tsharkPathProperty,
                                 StringProperty captureInterfaceProperty) {
        setArrowSize(0);
        setCornerRadius(5);
        setDetachedTitle("Tshark Settings");

        this.tsharkPathProperty = tsharkPathProperty;
        this.captureInterfaceProperty = captureInterfaceProperty;
        captureInterfaces = new TreeMap<>();
        interfacesComboBoxListener = (observable, oldValue, newValue) -> {
            captureInterfaceProperty.set(captureInterfaces.get(newValue));
        };

        vbox = new VBox(10);
        vbox.setPadding(new Insets(20, 20, 20, 20));
        vbox.setPrefSize(500, 80);

        HBox pathBox = new HBox(10);
        Label pathLabel = new Label("Tshark Path");
        pathTextField = new TextField();
        if (SystemUtils.IS_OS_WINDOWS) {
            pathTextField.setText("C:\\Program Files\\Wireshark\\tshark.exe");
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            pathTextField.setText("/Applications/Wireshark.app/Contents/Resources/bin/tshark");
        }
        tsharkPathProperty.bind(pathTextField.textProperty());
        Button chooseButton = new Button("Choose");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Tshark Path");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooseButton.setOnAction(event -> {
            File selectedFile = fileChooser.showOpenDialog(this);
            if (selectedFile != null) {
                pathTextField.setText(selectedFile.getAbsolutePath());
            }
        });
        pathBox.getChildren().addAll(pathLabel, pathTextField, chooseButton);
        pathBox.setHgrow(pathTextField, Priority.ALWAYS);

        HBox interfacesBox = new HBox(10);
        Label interfacesLabel = new Label("Capture Interface");
        interfacesComboBox = new ComboBox<>();
        Button interfacesButton = new Button("Get Capture Interfaces");
        interfacesButton.setOnAction(event -> discoverCaptureInterfaces());
        if (new File(pathTextField.getText()).exists()) {
            discoverCaptureInterfaces();
        }
        interfacesBox.getChildren().addAll(interfacesLabel, interfacesComboBox, interfacesButton);

        vbox.getChildren().addAll(pathBox, interfacesBox);
        vbox.setMargin(interfacesBox, new Insets(30, 0, 0, 0));
        setContentNode(vbox);
    }

    public void discoverCaptureInterfaces() {
        interfacesComboBox.valueProperty().removeListener(interfacesComboBoxListener);
        captureInterfaces.clear();
        interfacesComboBox.getItems().clear();
        List<String> commands = new ArrayList<>(Arrays.asList(pathTextField.getText(), "-D"));
        try {
            Process process = new ProcessBuilder(commands).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    Charset.defaultCharset()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\\s+(?=([^\\(]*\\([^\\(]*\\))*[^\\)]*$)");
                String interfaceName = split[2].substring(1, split[2].length() - 1);
                captureInterfaces.put(interfaceName, split[1]);
            }
            interfacesComboBox.getItems().addAll(new ArrayList<>(captureInterfaces.keySet()));
            if (captureInterfaces.size() > 0) {
                interfacesComboBox.getSelectionModel().select(0);
                captureInterfaceProperty.set(interfacesComboBox.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        interfacesComboBox.valueProperty().addListener(interfacesComboBoxListener);
    }
}
