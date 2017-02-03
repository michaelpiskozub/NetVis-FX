package netvis.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import netvis.model.DataController;
import netvis.model.PacketListener;
import netvis.model.Packet;
import netvis.model.filters.IPFilter;
import netvis.model.filters.MACFilter;
import netvis.model.filters.PortFilter;
import netvis.model.filters.ProtocolFilter;
import netvis.ui.controlsfx.Borders;
import netvis.ui.controlsfx.PopOver;
import netvis.ui.controlsfx.RangeSlider;
import netvis.ui.controlsfx.TextFields;
import netvis.ui.controlsfx.utils.AutoCompletionBinding;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FiltersPopOver extends PopOver implements PacketListener {
    private DataController dataController;
    private Set<String> ipSuggestions;
    private Set<String> macSuggestions;
    private Set<String> protocolSuggestions;
    private RangeSlider sourcePortSlider;
    private RangeSlider destinationPortSlider;
    private TextField includedIPsTextField;
    private TextField excludedIPsTextField;
    private TextField includedMACsTextField;
    private TextField excludedMACsTextField;
    private TextField includedProtocolsTextField;
    private TextField excludedProtocolsTextField;
    private AutoCompletionBinding<String> includedIPsTextFieldBinding;
    private AutoCompletionBinding<String> excludedIPsTextFieldBinding;
    private AutoCompletionBinding<String> includedMACsTextFieldBinding;
    private AutoCompletionBinding<String> excludedMACsTextFieldBinding;
    private AutoCompletionBinding<String> includedProtocolsTextFieldBinding;
    private AutoCompletionBinding<String> excludedProtocolsTextFieldBinding;
    private ObservableList<String> ipWhiteList;
    private ObservableList<String> ipBlackList;
    private ObservableList<String> macWhiteList;
    private ObservableList<String> macBlackList;
    private ObservableList<String> protocolWhiteList;
    private ObservableList<String> protocolBlackList;

    public FiltersPopOver(DataController dataController) {
        setArrowSize(0);
        setCornerRadius(5);
        setDetachedTitle("Filters");

        this.dataController = dataController;
        initializeLists();

        VBox stackedTitledPanes = new VBox();
        TitledPane portTitledPane = new TitledPane("Port", createPortLayout());
        portTitledPane.setExpanded(false);
        portTitledPane.setAnimated(false);
        TitledPane ipTitledPane = new TitledPane("IP", createIPLayout());
        ipTitledPane.setExpanded(false);
        ipTitledPane.setAnimated(false);
        TitledPane macTitledPane = new TitledPane("MAC", createMACLayout());
        macTitledPane.setExpanded(false);
        macTitledPane.setAnimated(false);
        TitledPane protocolTitledPane = new TitledPane("Protocol", createProtocolLayout());
        protocolTitledPane.setExpanded(false);
        protocolTitledPane.setAnimated(false);
        stackedTitledPanes.getChildren().addAll(portTitledPane, ipTitledPane, macTitledPane, protocolTitledPane);
        ScrollPane scroll = new ScrollPane(stackedTitledPanes);
        scroll.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            stackedTitledPanes.setPrefWidth(newValue.getWidth());
        });
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefSize(600, 800);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 0, 0, 0));
        vbox.getChildren().addAll(scroll);
        setContentNode(vbox);

        dataController.addPacketListener(this);
        dataController.addFilter(new PortFilter(sourcePortSlider, destinationPortSlider));
        dataController.addFilter(new IPFilter(ipWhiteList, ipBlackList));
        dataController.addFilter(new MACFilter(macWhiteList, macBlackList));
        dataController.addFilter(new ProtocolFilter(protocolWhiteList, protocolBlackList));
    }

    @Override
    public void allDataChanged(List<Packet> allPackets, int updateInterval, int intervalsComplete) {
        unbindAutoCompletion();
        ipSuggestions.clear();
        macSuggestions.clear();
        protocolSuggestions.clear();
        updateAutoCompletionLists(allPackets);
        bindAutoCompletion();
    }

    @Override
    public void newPacketsArrived(List<Packet> newPackets) {
        updateAutoCompletionLists(newPackets);

        //unbinds old AutoCompletionBinding's from text fields
        unbindAutoCompletion();

        //binds new AutoCompletionBinding's, updated with data from new packets, to text fields
        bindAutoCompletion();
    }

    private void updateAutoCompletionLists(List<Packet> packetList) {
        for (Packet p : packetList) {
            ipSuggestions.add(p.SOURCE_IP);
            ipSuggestions.add(p.DESTINATION_IP);
            macSuggestions.add(p.SOURCE_MAC);
            macSuggestions.add(p.DESTINATION_MAC);
            protocolSuggestions.add(p.PROTOCOL);
        }
    }

    private void unbindAutoCompletion() {
        if (includedIPsTextFieldBinding != null) {
            includedIPsTextFieldBinding.dispose();
        }
        if (excludedIPsTextFieldBinding != null) {
            excludedIPsTextFieldBinding.dispose();
        }
        if (includedMACsTextField != null) {
            includedMACsTextFieldBinding.dispose();
        }
        if (excludedMACsTextFieldBinding != null) {
            excludedMACsTextFieldBinding.dispose();
        }
        if (includedProtocolsTextFieldBinding != null) {
            includedProtocolsTextFieldBinding.dispose();
        }
        if (excludedProtocolsTextFieldBinding != null) {
            excludedProtocolsTextFieldBinding.dispose();
        }
    }

    private void bindAutoCompletion() {
        includedIPsTextFieldBinding = TextFields.bindAutoCompletion(includedIPsTextField, ipSuggestions);
        excludedIPsTextFieldBinding = TextFields.bindAutoCompletion(excludedIPsTextField, ipSuggestions);
        includedMACsTextFieldBinding = TextFields.bindAutoCompletion(includedMACsTextField, macSuggestions);
        excludedMACsTextFieldBinding = TextFields.bindAutoCompletion(excludedMACsTextField, macSuggestions);
        includedProtocolsTextFieldBinding = TextFields.bindAutoCompletion(includedProtocolsTextField,
                protocolSuggestions);
        excludedProtocolsTextFieldBinding = TextFields.bindAutoCompletion(excludedProtocolsTextField,
                protocolSuggestions);
    }

    @Override
    public void interrupt() {}

    private void initializeLists() {
        ipSuggestions = new TreeSet<>();
        macSuggestions = new TreeSet<>();
        protocolSuggestions = new TreeSet<>();
        ipWhiteList = FXCollections.observableArrayList();
        ipBlackList = FXCollections.observableArrayList();
        macWhiteList = FXCollections.observableArrayList();
        macBlackList = FXCollections.observableArrayList();
        protocolWhiteList = FXCollections.observableArrayList();
        protocolBlackList = FXCollections.observableArrayList();
    }

    private Region createPortSlider(RangeSlider slider) {
        final TextField minField = new TextField();
        minField.setPrefColumnCount(5);
        final TextField maxField = new TextField();
        maxField.setPrefColumnCount(5);

        slider.lowValueChangingProperty().addListener((observableValue, wasChanging, changing) -> {
            if (!changing) {
                dataController.applyAllDataChanged();
            }
        });
        slider.highValueChangingProperty().addListener((observableValue, wasChanging, changing) -> {
            if (!changing) {
                dataController.applyAllDataChanged();
            }
        });
        slider.setMajorTickUnit(10000);
        slider.setMinorTickCount(5);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setBlockIncrement(1);
        slider.setPrefWidth(300);

        minField.setText("" + slider.getLowValue());
        minField.setEditable(true);
        minField.setPromptText("Min");
        minField.setOnKeyPressed(event -> {
            KeyCode k = event.getCode();
            if (k == KeyCode.ENTER) {
                int portNumber = Integer.parseInt(getUnformattedPortNumberString(minField.getText()));
                if (portNumber >= slider.getMin() && portNumber < slider.getMax()) {
                    minField.setText(getFormattedPortNumberString(minField.getText()));
                    dataController.applyAllDataChanged();
                }
            }
        });

        maxField.setText("" + slider.getHighValue());
        maxField.setEditable(true);
        maxField.setPromptText("Max");
        maxField.setOnKeyPressed(event -> {
            KeyCode k = event.getCode();
            if (k == KeyCode.ENTER) {
                int portNumber = Integer.parseInt(getUnformattedPortNumberString(maxField.getText()));
                if (portNumber > slider.getMin() && portNumber <= slider.getMax()) {
                    maxField.setText(getFormattedPortNumberString(maxField.getText()));
                    dataController.applyAllDataChanged();
                }
            }
        });

        StringConverter<Number> converter = new NumberStringConverter();
        Bindings.bindBidirectional(minField.textProperty(), slider.lowValueProperty(), converter);
        Bindings.bindBidirectional(maxField.textProperty(), slider.highValueProperty(), converter);

        HBox box = new HBox(10);
        box.getChildren().addAll(minField, slider, maxField);
        box.setPadding(new Insets(20, 0, 20, 0));
        box.setHgrow(slider, Priority.ALWAYS);
        box.setFillHeight(false);

        return box;
    }

    private String getFormattedPortNumberString(String numberString) {
        if (numberString.length() > 3 && !numberString.contains(",")) {
            return numberString.substring(0, numberString.length() - 3) + ","
                    + numberString.substring(numberString.length() - 3);
        }
        return numberString;
    }

    private String getUnformattedPortNumberString(String numberString) {
        if (numberString.contains(",")) {
            return numberString.substring(0, numberString.length() - 4)
                    + numberString.substring(numberString.length() - 3);
        }
        return numberString;
    }

    private Node createPortControl(String title, RangeSlider slider) {
        VBox srcPortBox = new VBox();
        Label srcPortLabel = new Label(title);
        Region srcPortSlider = createPortSlider(slider);
        srcPortBox.getChildren().addAll(srcPortLabel, srcPortSlider);
        return Borders.wrap(srcPortBox).lineBorder().thickness(1).radius(5).build().build();
    }

    private Node createListControl(String title, ObservableList<String> selectedItemsList, TextField textField) {
        VBox controlsBox = new VBox(10);

        Label label = new Label(title);

        selectedItemsList.addListener((ListChangeListener<String>) c -> dataController.applyAllDataChanged());
        ListView<String> itemsListView = new ListView<>(selectedItemsList);
        itemsListView.setPrefWidth(250);
        itemsListView.setPrefHeight(200);

        itemsListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            itemsListView.setOnKeyPressed(event -> {
                KeyCode k = event.getCode();
                if (k == KeyCode.DELETE) {
                    String selectedItem = itemsListView.getSelectionModel().getSelectedItem();
                    itemsListView.getItems().remove(selectedItem);
                }
            });

            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
            deleteItem.setOnAction(event -> itemsListView.getItems().remove(cell.getItem()));
            contextMenu.getItems().addAll(deleteItem);

            cell.textProperty().bind(cell.itemProperty());

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

        textField.setOnKeyPressed(event -> {
            KeyCode k = event.getCode();
            if (k == KeyCode.ENTER) {
                if (!textField.getText().equals("")) {
                    if (!selectedItemsList.contains(textField.getText())) {
                        selectedItemsList.add(textField.getText());
                    }
                    textField.clear();
                }
            }
        });

        controlsBox.getChildren().addAll(label, itemsListView, textField);
        return Borders.wrap(controlsBox).lineBorder().thickness(1).radius(5).build().build();
    }

    private VBox createPortLayout() {
        VBox portBox = new VBox(10);
        sourcePortSlider = new RangeSlider(0, 65535, 0, 65535);
        destinationPortSlider = new RangeSlider(0, 65535, 0, 65535);
        portBox.getChildren().addAll(
                createPortControl("Source Port", sourcePortSlider),
                createPortControl("Destination Port", destinationPortSlider)
        );
        return portBox;
    }

    private HBox createIPLayout() {
        HBox ipBox = new HBox(10);
        includedIPsTextField = new TextField();
        includedIPsTextFieldBinding = TextFields.bindAutoCompletion(includedIPsTextField, ipSuggestions);
        excludedIPsTextField = new TextField();
        excludedIPsTextFieldBinding = TextFields.bindAutoCompletion(excludedIPsTextField, ipSuggestions);
        ipBox.getChildren().addAll(
                createListControl("Included IPs", ipWhiteList, includedIPsTextField),
                createListControl("Excluded IPs", ipBlackList, excludedIPsTextField)
        );
        return ipBox;
    }

    private HBox createMACLayout() {
        HBox macBox = new HBox(10);
        includedMACsTextField = new TextField();
        includedMACsTextFieldBinding = TextFields.bindAutoCompletion(includedMACsTextField, macSuggestions);
        excludedMACsTextField = new TextField();
        excludedMACsTextFieldBinding = TextFields.bindAutoCompletion(excludedMACsTextField, macSuggestions);
        macBox.getChildren().addAll(
                createListControl("Included MACs", macWhiteList, includedMACsTextField),
                createListControl("Excluded MACs", macBlackList, excludedMACsTextField)
        );
        return macBox;
    }

    private HBox createProtocolLayout() {
        HBox protocolBox = new HBox(10);
        includedProtocolsTextField = new TextField();
        includedProtocolsTextFieldBinding = TextFields.bindAutoCompletion(includedProtocolsTextField,
                protocolSuggestions);
        excludedProtocolsTextField = new TextField();
        excludedProtocolsTextFieldBinding = TextFields.bindAutoCompletion(excludedProtocolsTextField,
                protocolSuggestions);
        protocolBox.getChildren().addAll(
                createListControl("Included Protocols", protocolWhiteList, includedProtocolsTextField),
                createListControl("Excluded Protocols", protocolBlackList, excludedProtocolsTextField));
        return protocolBox;
    }
}
