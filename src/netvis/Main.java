package netvis;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import netvis.model.DataController;
import netvis.model.RealTimeOpener;
import netvis.model.RecordingOpener;
import netvis.model.input.DataFeeder;
import netvis.model.input.csv.CSVDataFeeder;
import netvis.model.input.dummy.DummyDataFeeder;
import netvis.model.input.tshark.TSharkUtilities;
import netvis.model.input.tshark.pcap.PCAPDataFeeder;
import netvis.model.input.tshark.realtime.RealTimeDataFeeder;
import netvis.ui.*;
import netvis.view.Visualization;
import netvis.view.VisualizationFX;
import netvis.view.VisualizationJOGL;
import netvis.view.VisualizationsController;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

public class Main extends Application implements RealTimeOpener, RecordingOpener {
    private final String VERSION_NUMBER = "2.0";
    private final Main THIS = this;
    private BorderPane root;
    private Scene scene;
    private StringProperty tsharkPathProperty;
    private StringProperty captureInterfaceProperty;
    private TSharkUtilities tSharkUtilities;
    private BooleanProperty dataFeederPauseProperty;
    private BooleanProperty dataFeederGoToEndProperty;
    private DataFeeder dataFeeder;
    private DataController dataController;
    private VBox visualizationBox;
    private FileChooser fileChooser;
    private MenuItem openMenuItem;
    private MenuItem startRealTimeMenuItem;
    private MenuItem tsharkSettingsMenuItem;
    private MenuItem exitMenuItem;
    private MenuItem controlsMenuItem;
    private MenuItem visualizationDescriptionMenuItem;
    private MenuItem filtersMenuItem;
    private MenuItem aboutMenuItem;
    private TsharkSettingsPopOver tsharkSettingsPopOver;
    private ControlsPopOver controlsPopOver;
    private VisualizationDescriptionPopOver visualizationDescriptionPopOver;
    private FiltersPopOver filtersPopOver;
    private AboutPopOver aboutPopOver;

    @Override
    public void init() throws Exception {
        tsharkPathProperty = new SimpleStringProperty();
        captureInterfaceProperty = new SimpleStringProperty();
        tSharkUtilities = new TSharkUtilities(tsharkPathProperty, captureInterfaceProperty);
        dataFeederPauseProperty = new SimpleBooleanProperty();
        dataFeederGoToEndProperty = new SimpleBooleanProperty();
        dataFeeder = new DummyDataFeeder();
        dataController = new DataController(dataFeeder, 1000);

        tsharkSettingsPopOver = new TsharkSettingsPopOver(tsharkPathProperty, captureInterfaceProperty);
        controlsPopOver = new ControlsPopOver();
        controlsPopOver.setPlaybackControls(dataFeeder.getControls());
        visualizationDescriptionPopOver = new VisualizationDescriptionPopOver();
        filtersPopOver = new FiltersPopOver(dataController);
        aboutPopOver = new AboutPopOver();

        visualizationBox = new VBox();
        root = new BorderPane(visualizationBox, null, null, null, null);
        scene = new Scene(root, 1093, 614, Color.WHITE);
        VisualizationsController.INSTANCE.initializeAllVisualizations(dataController, scene, dataFeederPauseProperty,
                dataFeederGoToEndProperty);
        root.setTop(createMenus());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setControlsActions(primaryStage);

        primaryStage.setTitle("NetVis " + VERSION_NUMBER);
        primaryStage.setScene(scene);
        primaryStage.getIcons().addAll(
                new Image(getClass().getResourceAsStream("/netvis_icon_256.png")),
                new Image(getClass().getResourceAsStream("/netvis_icon_128.png")),
                new Image(getClass().getResourceAsStream("/netvis_icon_64.png")),
                new Image(getClass().getResourceAsStream("/netvis_icon_32.png"))
        );
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        dataController.interruptAll();
    }

    @Override
    public void openCSV(File file) {
        if (dataFeederPauseProperty.isBound()) {
            dataFeederPauseProperty.unbind();
        }
        if (dataFeederGoToEndProperty.isBound()) {
            dataFeederGoToEndProperty.unbind();
        }
        dataFeeder = new CSVDataFeeder(file, THIS, dataFeederPauseProperty, dataFeederGoToEndProperty);
        controlsPopOver.setPlaybackControls(dataFeeder.getControls());
        dataController.setDataFeeder(dataFeeder);
    }

    @Override
    public void openPCAP(File file) {
        if (dataFeederPauseProperty.isBound()) {
            dataFeederPauseProperty.unbind();
        }
        if (dataFeederGoToEndProperty.isBound()) {
            dataFeederGoToEndProperty.unbind();
        }
        dataFeeder = new PCAPDataFeeder(file, this, dataFeederPauseProperty, dataFeederGoToEndProperty, tSharkUtilities);
        controlsPopOver.setPlaybackControls(dataFeeder.getControls());
        dataController.setDataFeeder(dataFeeder);
    }

    @Override
    public void startRealTime() {
        dataFeeder = new RealTimeDataFeeder(this, tSharkUtilities);
        controlsPopOver.setPlaybackControls(dataFeeder.getControls());
        dataController.setDataFeeder(dataFeeder);
    }

    private void setControlsActions(Stage stage) {
        openMenuItem.setOnAction(event -> {
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                fileChooser.setInitialDirectory(selectedFile.getParentFile());
                String selectedFileExtension = FilenameUtils.getExtension(selectedFile.getAbsolutePath());
                if (selectedFileExtension.equals("cap") || selectedFileExtension.equals("pcap") ||
                        selectedFileExtension.equals("pcapng")) {
                    openPCAP(selectedFile);
                } else if (selectedFileExtension.equals("csv")) {
                    openCSV(selectedFile);
                }
                stage.setTitle("NetVis " + VERSION_NUMBER + " - " + selectedFile.getName());
            }
        });

        startRealTimeMenuItem.setOnAction(event -> {
            startRealTime();
            stage.setTitle("NetVis " + VERSION_NUMBER + " - Real-Time Mode");
        });

        tsharkSettingsMenuItem.setOnAction(event -> {
            if (!tsharkSettingsPopOver.isShowing()) {
                tsharkSettingsPopOver.setDetached(true);
                tsharkSettingsPopOver.show(root,
                        tsharkSettingsPopOver.getAnchorX(),
                        tsharkSettingsPopOver.getAnchorY(),
                        Duration.millis(10)
                );
            } else {
                tsharkSettingsPopOver.hide();
            }
        });

        exitMenuItem.setOnAction(event -> Platform.exit());

        controlsMenuItem.setOnAction(event -> {
            if (!controlsPopOver.isShowing()) {
                controlsPopOver.setDetached(true);
                controlsPopOver.show(root,
                        controlsPopOver.getAnchorX(),
                        controlsPopOver.getAnchorY(),
                        Duration.millis(10)
                );
            } else {
                controlsPopOver.hide();
            }
        });

        visualizationDescriptionMenuItem.setOnAction(event -> {
            if (!visualizationDescriptionPopOver.isShowing()) {
                visualizationDescriptionPopOver.setDetached(true);
                visualizationDescriptionPopOver.show(root,
                        visualizationDescriptionPopOver.getAnchorX(),
                        visualizationDescriptionPopOver.getAnchorY(),
                        Duration.millis(10)
                );
            } else {
                visualizationDescriptionPopOver.hide();
            }
        });

        filtersMenuItem.setOnAction(event -> {
            if (!filtersPopOver.isShowing()) {
                filtersPopOver.setDetached(true);
                filtersPopOver.show(root,
                        filtersPopOver.getAnchorX(),
                        filtersPopOver.getAnchorY(),
                        Duration.millis(10));
            } else {
                filtersPopOver.hide();
            }
        });

        aboutMenuItem.setOnAction(event -> {
            if (!aboutPopOver.isShowing()) {
                aboutPopOver.setDetached(true);
                aboutPopOver.show(root,
                        aboutPopOver.getAnchorX(),
                        aboutPopOver.getAnchorY(),
                        Duration.millis(10));
            } else {
                aboutPopOver.hide();
            }
        });
    }

    private MenuBar createMenus() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Capture Files", "*.cap", "*.csv", "*.pcap", "*.pcapng"),
                new FileChooser.ExtensionFilter("CAP", "*.cap"),
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("PCAP", "*.pcap"),
                new FileChooser.ExtensionFilter("PCAPNG", "*.pcapng")
        );

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("_File");
        fileMenu.setMnemonicParsing(true);
        Menu viewMenu = new Menu("_View");
        viewMenu.setMnemonicParsing(true);
        Menu visualizationMenu = new Menu("Visuali_zation");
        visualizationMenu.setMnemonicParsing(true);
        Menu helpMenu = new Menu("_Help");
        helpMenu.setMnemonicParsing(true);

        openMenuItem = new MenuItem("Open...");
        openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        startRealTimeMenuItem = new MenuItem("Start Real-Time");
        startRealTimeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        tsharkSettingsMenuItem = new MenuItem("Tshark Settings");
        tsharkSettingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
        fileMenu.getItems().addAll(openMenuItem, startRealTimeMenuItem, tsharkSettingsMenuItem, new SeparatorMenuItem(),
                exitMenuItem);

        controlsMenuItem = new MenuItem("Controls");
        controlsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        visualizationDescriptionMenuItem = new MenuItem("Description");
        visualizationDescriptionMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN));
        filtersMenuItem = new MenuItem("Filters");
        filtersMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
        viewMenu.getItems().addAll(controlsMenuItem, visualizationDescriptionMenuItem, filtersMenuItem);

        ArrayList<KeyCode> digitKeyCodes = new ArrayList<>(Arrays.asList(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3,
                KeyCode.DIGIT4, KeyCode.DIGIT5, KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9));
        ToggleGroup visualizationsToggleGroup = new ToggleGroup();
        ArrayList<RadioMenuItem> fxVisualizationsMenuItems = new ArrayList<>();
        for (Map.Entry<String, VisualizationFX> e : VisualizationsController.INSTANCE.getFXVisualizations()) {
            RadioMenuItem visualizationMenuItem = new RadioMenuItem(e.getKey());
            visualizationMenuItem.setToggleGroup(visualizationsToggleGroup);
            if (digitKeyCodes.size() > 0) {
                visualizationMenuItem.setAccelerator(new KeyCodeCombination(digitKeyCodes.remove(0), KeyCombination.SHORTCUT_DOWN));
            }
            visualizationMenuItem.setOnAction(new VisualizationMenuItemHandler(e.getValue()));
            fxVisualizationsMenuItems.add(visualizationMenuItem);
        }
        ArrayList<RadioMenuItem> joglVisualizationsMenuItems = new ArrayList<>();
        for (Map.Entry<String, VisualizationJOGL> e : VisualizationsController.INSTANCE.getJOGLVisualizations()) {
            RadioMenuItem visualizationMenuItem = new RadioMenuItem(e.getKey());
            visualizationMenuItem.setToggleGroup(visualizationsToggleGroup);
            if (digitKeyCodes.size() > 0) {
                visualizationMenuItem.setAccelerator(new KeyCodeCombination(digitKeyCodes.remove(0), KeyCombination.SHORTCUT_DOWN));
            }
            visualizationMenuItem.setOnAction(new VisualizationMenuItemHandler(e.getValue()));
            joglVisualizationsMenuItems.add(visualizationMenuItem);
        }
        visualizationMenu.getItems().addAll(fxVisualizationsMenuItems);
        visualizationMenu.getItems().add(new SeparatorMenuItem());
        visualizationMenu.getItems().addAll(joglVisualizationsMenuItems);

        aboutMenuItem = new MenuItem("About");
        helpMenu.getItems().addAll(aboutMenuItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, visualizationMenu, helpMenu);
        return menuBar;
    }

    private class VisualizationMenuItemHandler implements EventHandler<ActionEvent> {
        private Visualization visualization;

        public VisualizationMenuItemHandler(Visualization visualization) {
            this.visualization = visualization;
        }

        @Override
        public void handle(ActionEvent event) {
            VisualizationsController.INSTANCE.deactivateCurrentVisualization();
            visualizationBox.getChildren().clear();
            visualizationBox.getChildren().add(visualization.getVisualization());
            visualization.activate();
            VisualizationsController.INSTANCE.setCurrentVisualization(visualization);
            controlsPopOver.setVisualizationControls(visualization.getControls());
            visualizationDescriptionPopOver.setVisualizationDescription(visualization.getDescription());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
