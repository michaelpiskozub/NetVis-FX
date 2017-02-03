package netvis.view.visualizations.fx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import netvis.model.DataController;
import netvis.model.Packet;
import netvis.ui.PacketsPopOver;
import netvis.ui.PlanetsPopOver;
import netvis.ui.controlsfx.Borders;
import netvis.view.VisualizationFX;
import netvis.view.util.fx.*;
import netvis.fxyz.cameras.AdvancedCamera;
import netvis.fxyz.cameras.controllers.FPSController;
import netvis.fxyz.extras.BillboardBehavior;

import java.util.*;

public class GalaxyVisualization extends VisualizationFX {
    private Map<String, Planet> planets;
    private Map<String, PlanetLabel> planetsLabels;
    private List<PacketTransitionFactory> packetTransitions;

    private final Point3D ORIGIN = new Point3D(0, 0, 0);
    private boolean isConstantSpeed;
    private boolean arePlanetsLabelsShown;
    private boolean isBillboarding;
    private BooleanProperty protocolModeProperty;
    private BooleanProperty sourceIPModeProperty;
    private BooleanProperty destinationIPModeProperty;
    private BooleanProperty sourcePortModeProperty;
    private BooleanProperty destinationPortModeProperty;
    private PlanetsPopOver planetsPopOver;
    private PacketsPopOver packetsPopOver;
    private BooleanProperty pauseProperty;
    private BooleanProperty goToEndProperty;
    private Timeline newPacketsTimeline;
    private final int MAX_NUMBER_OF_PLANETS = 20;
    private final int PLANET_RADIUS = 50;
    private final int COLOR_GENERATOR_COLLECTION = ColorGenerator.COLLECTION_ONE_RED;
    private final int COLOR_GENERATOR_ORDER = ColorGenerator.ORDER_RANDOM;
    private ColorGenerator colorGenerator;
    private Group root;
    private List<Point3D> planetsPositions;
    private List<Point3D> planetsTextPositions;
    private int planetsCount;
    private Group planetsGroup;
    private Planet sun;
    private Point3D cameraOffset;
    private FPSController fpsController;
    private AdvancedCamera camera;

    public GalaxyVisualization(DataController dataController, Scene scene, BooleanProperty pauseProperty,
                               BooleanProperty goToEndProperty) {
        super(dataController);

        planets = new LinkedHashMap<>();
        planetsLabels = new LinkedHashMap<>();
        packetTransitions = new ArrayList<>();

        isConstantSpeed = false;
        arePlanetsLabelsShown = true;
        isBillboarding = true;
        protocolModeProperty = new SimpleBooleanProperty();
        sourceIPModeProperty = new SimpleBooleanProperty();
        destinationIPModeProperty = new SimpleBooleanProperty();
        sourcePortModeProperty = new SimpleBooleanProperty();
        destinationPortModeProperty = new SimpleBooleanProperty();
        planetsPopOver = new PlanetsPopOver();
        packetsPopOver = new PacketsPopOver();
        this.pauseProperty = pauseProperty;
        this.goToEndProperty = goToEndProperty;
        newPacketsTimeline = new Timeline();
        pauseProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                newPacketsTimeline.pause();
                packetTransitions.forEach(netvis.view.util.fx.PacketTransitionFactory::pauseAll);
            } else {
                newPacketsTimeline.play();
                packetTransitions.forEach(netvis.view.util.fx.PacketTransitionFactory::resumeAll);
            }
        });
        colorGenerator = new ColorGenerator(COLOR_GENERATOR_COLLECTION, COLOR_GENERATOR_ORDER);
        root = new Group();
        planetsPositions = new ArrayList<>();
        planetsTextPositions = new ArrayList<>();
        planetsCount = 0;
        planetsGroup = new Group();
        sun = new Planet(10, new ColorPair(Color.WHITE, Color.WHITE));
        Translate sunTranslate = new Translate(ORIGIN.getX(), ORIGIN.getY(), ORIGIN.getZ());
        sun.getTransforms().add(sunTranslate);
        cameraOffset = new Point3D(100, 75, -1500);

        initializePositions(planetsPositions, 900, 900, 600);
        initializePositions(planetsTextPositions, 1000, 1000, 600);
        root.getChildren().addAll(sun, planetsGroup);

        fpsController = new FPSController();
        camera = new AdvancedCamera();
        camera.setController(fpsController);

        subScene = new SubScene(root, 1080, 720, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.grayRgb(38));
        subScene.setFocusTraversable(true);
        fpsController.setSubScene(subScene);
        fpsController.setXYZ(cameraOffset);

        subScene.widthProperty().bind(scene.widthProperty());
        subScene.heightProperty().bind(scene.heightProperty());

        subScene.setOnMouseClicked(event -> {
            PickResult pr = event.getPickResult();
            Node in = pr.getIntersectedNode();
            if (pr != null && in != null) {
                if (in instanceof Planet) {
                    Planet planet = (Planet) in;
                    planetsPopOver.addPlanetText(planet);
                    if (!planetsPopOver.isShowing()) {
                        planetsPopOver.setDetached(true);
                        planetsPopOver.show(planet, planetsPopOver.getAnchorX(), planetsPopOver.getAnchorY(),
                                Duration.millis(10));
                    }
                } else if (in instanceof PacketBox) {
                    PacketBox packetBox = (PacketBox) in;
                    packetsPopOver.addPacketText(packetBox.getPacket());
                    if (!packetsPopOver.isShowing()) {
                        packetsPopOver.setDetached(true);
                        packetsPopOver.show(packetBox, packetsPopOver.getAnchorX(), packetsPopOver.getAnchorY(),
                                Duration.millis(10));
                    }
                }
            }
        });

        subScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case R:
                    setCameraInFront();
                    break;
            }
        });

        controls = initializeControls();
    }

    @Override
    protected Node initializeControls() {
        VBox vbox = new VBox(10);
        HBox modeBox = new HBox(10);
        Label modeLabel = new Label("Mode");
        ComboBox<String> modeComboBox = new ComboBox<>();
        modeComboBox.getItems().addAll("Source IP", "Destination IP", "Source Port", "Destination Port", "Protocol");
        ChangeListener<Boolean> modeListener = (observable, oldValue, newValue) -> {
            reset();
        };
        protocolModeProperty.bind(Bindings.when(modeComboBox.valueProperty().isEqualTo("Protocol")).then(true).otherwise(false));
        protocolModeProperty.addListener(modeListener);
        sourceIPModeProperty.bind(Bindings.when(modeComboBox.valueProperty().isEqualTo("Source IP")).then(true).otherwise(false));
        sourceIPModeProperty.addListener(modeListener);
        destinationIPModeProperty.bind(Bindings.when(modeComboBox.valueProperty().isEqualTo("Destination IP")).then(true).otherwise(false));
        destinationIPModeProperty.addListener(modeListener);
        sourcePortModeProperty.bind(Bindings.when(modeComboBox.valueProperty().isEqualTo("Source Port")).then(true).otherwise(false));
        sourcePortModeProperty.addListener(modeListener);
        destinationPortModeProperty.bind(Bindings.when(modeComboBox.valueProperty().isEqualTo("Destination Port")).then(true).otherwise(false));
        destinationPortModeProperty.addListener(modeListener);
        modeComboBox.getSelectionModel().select("Protocol");

        modeBox.getChildren().addAll(modeLabel, modeComboBox);
        CheckBox speedCheckBox = new CheckBox("Constant Speed");
        speedCheckBox.selectedProperty().addListener((observable1, oldValue1, newValue1) -> {
            isConstantSpeed = !isConstantSpeed;
        });
        CheckBox labelsBillboardingCheckBox = new CheckBox("Planet Labels Billboarding");
        labelsBillboardingCheckBox.setSelected(true);
        labelsBillboardingCheckBox.selectedProperty().addListener((observable1, oldValue1, newValue1) -> {
            togglePlanetLabelsBillboarding();
        });
        CheckBox labelsCheckBox = new CheckBox("Planet Labels");
        labelsCheckBox.setSelected(true);
        labelsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                labelsBillboardingCheckBox.setDisable(true);
            } else {
                labelsBillboardingCheckBox.setDisable(false);
            }
            togglePlanetLabels();
        });
        Button planetsPopOverButton = new Button("Show Planets Data");
        planetsPopOver.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                planetsPopOverButton.setText("Hide Planets Data");
            } else {
                planetsPopOverButton.setText("Show Planets Data");
            }
        });
        planetsPopOverButton.setOnAction(event -> {
            if (!planetsPopOver.isShowing()) {
                planetsPopOver.setDetached(true);
                planetsPopOver.show(planetsPopOverButton, planetsPopOver.getAnchorX(), planetsPopOver.getAnchorY(),
                        Duration.millis(10));
            } else {
                planetsPopOver.hide();
            }
        });
        Button packetsPopOverButton = new Button("Show Packets Data");
        packetsPopOver.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                packetsPopOverButton.setText("Hide Packets Data");
            } else {
                packetsPopOverButton.setText("Show Packets Data");
            }
        });
        packetsPopOverButton.setOnAction(event -> {
            if (!packetsPopOver.isShowing()) {
                packetsPopOver.setDetached(true);
                packetsPopOver.show(packetsPopOverButton, packetsPopOver.getAnchorX(), packetsPopOver.getAnchorY(),
                        Duration.millis(10));
            } else {
                packetsPopOver.hide();
            }
        });

        vbox.getChildren().addAll(modeBox, speedCheckBox, labelsCheckBox, labelsBillboardingCheckBox,
                planetsPopOverButton, packetsPopOverButton);
        return Borders.wrap(vbox).lineBorder().thickness(1).radius(5).build().build();
    }

    @Override
    public String getName() {
        return "Galaxy";
    }

    @Override
    public String getDescription() {
        return "Visualization showing a set of planets representing protocols, ports or IP addresses surrounding the " +
                "sun, which is where the packets appear. Each packet is headed towards one of the planets based on " +
                "its parameter being equal to the one represented by that planet.";
    }

    @Override
    public void newPacketsArrived(List<Packet> newPackets) {
        if (isShown && !goToEndProperty.get()) {
            this.newPackets = newPackets;
            List<KeyFrame> packetsKeyFrames = new ArrayList<>();
            newPacketsTimeline = new Timeline();
            Iterator<Packet> newPacketsIterator = newPackets.iterator();
            while (newPacketsIterator.hasNext()) {
                Packet p = newPacketsIterator.next();
                Color packetColor;
                if (p.LENGTH >= 500 && p.LENGTH < 1000) {
                    packetColor = Color.YELLOW;
                } else if (p.LENGTH >= 1000) {
                    packetColor = Color.AQUA;
                } else {
                    packetColor = Color.CHARTREUSE;
                }
                int speed;
                if (isConstantSpeed) {
                    speed = 250;
                } else {
                    speed = getSpeed(p.LENGTH);
                }
                Planet target;
                if (protocolModeProperty.get()) {
                    target = getProtocolTarget(p);
                } else if (sourcePortModeProperty.get()) {
                    target = getSourcePortTarget(p);
                } else if (destinationPortModeProperty.get()) {
                    target = getDestinationPortTarget(p);
                } else if (sourceIPModeProperty.get()) {
                    target = getSourceIPTarget(p);
                } else {
                    target = getDestinationIPTarget(p);
                }
                target.increaseHitsBy(1);
                target.increaseTotalLengthBy(p.LENGTH);

                KeyFrame keyFrame = new KeyFrame(Duration.millis(p.getTimeMillis()), event -> {
                    PacketTransitionFactory ptf =
                            new PacketTransitionFactory(p, speed, packetColor, root, packetTransitions, sun, target);
                    ptf.getHeadOfTransitionChain().playFromStart();
                });
                packetsKeyFrames.add(keyFrame);
            }
            newPacketsTimeline.getKeyFrames().addAll(packetsKeyFrames);
            if (!pauseProperty.get()) {
                newPacketsTimeline.playFromStart();
            }
        } else {
            Iterator<Packet> newPacketsIterator = newPackets.iterator();
            while (newPacketsIterator.hasNext()) {
                Packet p = newPacketsIterator.next();
                Planet target;
                if (protocolModeProperty.get()) {
                    target = getProtocolTarget(p);
                } else if (sourcePortModeProperty.get()) {
                    target = getSourcePortTarget(p);
                } else {
                    target = getDestinationPortTarget(p);
                }
                target.increaseHitsBy(1);
                target.increaseTotalLengthBy(p.LENGTH);
            }
        }
    }

    @Override
    public void allDataChanged(List<Packet> allPackets, int updateInterval, int intervalsComplete) {
        super.allDataChanged(allPackets, updateInterval, intervalsComplete);
        reset();
    }

    private void reset() {
        packetTransitions.forEach(netvis.view.util.fx.PacketTransitionFactory::interrupt);
        packetTransitions.clear();
        newPacketsTimeline = new Timeline();
        planetsGroup.getChildren().clear();
        planetsCount = 0;
        planets.clear();
        planetsLabels.clear();
        colorGenerator = new ColorGenerator(COLOR_GENERATOR_COLLECTION, COLOR_GENERATOR_ORDER);
        planetsPopOver.reset();
        packetsPopOver.reset();
        if (sourcePortModeProperty.get() || destinationPortModeProperty.get()) {
            initializePortMode();
        } else if (sourceIPModeProperty.get() || destinationIPModeProperty.get()) {
            initializeIPMode();
        }
    }

    private void initializePortMode() {
        //range: 0-65535
        int planetRange = (int) Math.ceil(65535 / (MAX_NUMBER_OF_PLANETS * 1.0));
        int previousRange = 0;
        for (int i = 0; i < MAX_NUMBER_OF_PLANETS; i++) {
            if (i < MAX_NUMBER_OF_PLANETS - 1) {
                int currentRange = (i + 1) * planetRange;
                String planetName = previousRange + "-" + (currentRange - 1);
                createPlanet(planetName);
                previousRange = currentRange;
            } else {
                String planetName = previousRange + "-" + 65535;
                createPlanet(planetName);
            }
        }
    }

    private void initializeIPMode() {
        int planetRange = (int) Math.floor(255 / (MAX_NUMBER_OF_PLANETS * 1.0));
        int previousRange = 0;
        for (int i = 0; i < MAX_NUMBER_OF_PLANETS; i++) {
            if (i < MAX_NUMBER_OF_PLANETS - 1) {
                int currentRange = (i + 1) * planetRange;
                String planetName = previousRange + "-" + (currentRange - 1) + ".x.y.z";
                createPlanet(planetName);
                previousRange = currentRange;
            } else {
                String planetName = previousRange + "-" + 255 + ".x.y.z";
                createPlanet(planetName);
            }
        }
    }

    private Planet getSourcePortTarget(Packet p) {
        return getPortTarget(p.SOURCE_PORT);
    }

    private Planet getDestinationPortTarget(Packet p) {
        return getPortTarget(p.DESTINATION_PORT);
    }

    private Planet getPortTarget(int port) {
        int planetRange = (int) Math.ceil(65535 / (MAX_NUMBER_OF_PLANETS * 1.0));
        int planetIndex = port / planetRange;
        int lowerRange = planetRange * planetIndex;
        int higherRange = (planetRange * (planetIndex + 1)) - 1;
        if (higherRange > 65535) {
            higherRange = 65535;
        }
        String planetName = lowerRange + "-" + higherRange;
        return planets.get(planetName);
    }

    private Planet getSourceIPTarget(Packet p) {
        return getIPTarget(p.SOURCE_IP);
    }

    private Planet getDestinationIPTarget(Packet p) {
        return getIPTarget(p.DESTINATION_IP);
    }

    private Planet getIPTarget(String ip) {
        int planetRange = (int) Math.floor(255 / (MAX_NUMBER_OF_PLANETS * 1.0));
        String planetName;
        if (ip.contains(":") || !ip.contains(".")) {
            planetName = 0 + "-" + (planetRange - 1) + ".x.y.z";
        } else {
            int firstByte = 0;
            int i = 0;
            try {
                while (ip.charAt(i) != '.') {
                    firstByte = firstByte * 10 + (ip.charAt(i) - '0');
                    i++;
                }
            } catch (StringIndexOutOfBoundsException e) {
                System.out.println(ip);
            }
            int planetIndex = firstByte / planetRange;
            int lowerRange = planetRange * planetIndex;
            int higherRange = (planetRange * (planetIndex + 1)) - 1;
            if (higherRange > 255 || planetIndex == MAX_NUMBER_OF_PLANETS - 1) {
                higherRange = 255;
            }
            planetName = lowerRange + "-" + higherRange + ".x.y.z";
        }
        return planets.get(planetName);
    }

    private Planet getProtocolTarget(Packet p) {
        if (!planets.containsKey(p.PROTOCOL)) {
            createPlanet(p.PROTOCOL);
        }
        if (planets.containsKey(p.PROTOCOL)) {
            return planets.get(p.PROTOCOL);
        } else {
            return planets.get("Others");
        }
    }

    private String getProtocolTargetName(String name) {
        if (planetsCount == planetsPositions.size() - 1) {
            return "Others";
        } else {
            return name;
        }
    }

    private void initializePositions(List<Point3D> positionsList, int x, int y, int z) {
        double circleInRad = Math.toRadians(360);
        for (int i = 0; i < MAX_NUMBER_OF_PLANETS; i++) {
            double iNormalized = i * (circleInRad / MAX_NUMBER_OF_PLANETS);
            positionsList.add(new Point3D(Math.sin(iNormalized) * x, Math.cos(iNormalized) * y, z));
        }
    }

    private void createPlanet(String name) {
        if (planetsCount < planetsPositions.size()) {
            Point3D planetPosition = planetsPositions.get(planetsCount);
            Planet planet = new Planet(PLANET_RADIUS, colorGenerator.getNextColor());
            String planetName;
            if (protocolModeProperty.get()) {
                planetName = getProtocolTargetName(name);
            } else {
                planetName = name;
            }
            planet.setName(planetName);
            planets.put(planetName, planet);

            planet.setTranslateX(planetPosition.getX() + ORIGIN.getX());
            planet.setTranslateY(planetPosition.getY() + ORIGIN.getY());
            planet.setTranslateZ(planetPosition.getZ() + ORIGIN.getZ());

            Point3D planetTextPosition = planetsTextPositions.get(planetsCount);
            PlanetLabel planetLabel = new PlanetLabel(planet.getName());
            boolean hasPlanetTextPositionChanged = false;
            double xOffset = 0.0;
            double yOffset = 0.0;
            if (planetTextPosition.getY() + planetLabel.getTextImageHeight() < planetPosition.getY() &&
                    planetTextPosition.getY() + planetLabel.getTextImageHeight() > planetPosition.getY() - PLANET_RADIUS) {
                yOffset = -planetLabel.getTextImageHeight();
                hasPlanetTextPositionChanged = true;
            }
            if (planetTextPosition.getX() < planetPosition.getX()) {
                xOffset = -planetLabel.getTextImageWidth();
                hasPlanetTextPositionChanged = true;
            }
            if (hasPlanetTextPositionChanged) {
                planetTextPosition = new Point3D(
                        planetTextPosition.getX() + xOffset,
                        planetTextPosition.getY() + yOffset,
                        planetTextPosition.getZ()
                );
            }
            planetLabel.setTranslateX(planetTextPosition.getX() + ORIGIN.getX());
            planetLabel.setTranslateY(planetTextPosition.getY() + ORIGIN.getY());
            planetLabel.setTranslateZ(planetTextPosition.getZ() + ORIGIN.getZ());
            planetsLabels.put(planetLabel.getName(), planetLabel);

            planetsGroup.getChildren().add(planet);
            if (arePlanetsLabelsShown) {
                planetsGroup.getChildren().add(planetLabel);
            }
            planetsCount++;

            planetLabel.setBillboardMode(BillboardBehavior.BillboardMode.SPHERICAL);
            if (isBillboarding) {
                planetLabel.startBillboardBehavior();
            }
        }
    }

    private void togglePlanetLabels() {
        if (arePlanetsLabelsShown) {
            arePlanetsLabelsShown = false;
            Map<String, PlanetLabel> planetsLabelsCopy = new LinkedHashMap<>(planetsLabels);
            planetsLabelsCopy.values().stream().filter(pl -> planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().remove(pl));
        } else {
            arePlanetsLabelsShown = true;
            Map<String, PlanetLabel> planetsLabelsCopy = new LinkedHashMap<>(planetsLabels);
            planetsLabelsCopy.values().stream().filter(pl -> !planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().add(pl));
        }
    }

    private void togglePlanetLabelsBillboarding() {
        if (isBillboarding) {
            isBillboarding = false;
            Map<String, PlanetLabel> planetsLabelsCopy = new LinkedHashMap<>(planetsLabels);
            planetsLabelsCopy.values().stream().filter(pl -> planetsGroup.getChildren().
                    contains(pl)).forEach(BillboardBehavior::stopBillboardBehavior);
        } else {
            isBillboarding = true;
            Map<String, PlanetLabel> planetsLabelsCopy = new LinkedHashMap<>(planetsLabels);
            planetsLabelsCopy.values().stream().filter(pl -> planetsGroup.getChildren().
                    contains(pl)).forEach(BillboardBehavior::startBillboardBehavior);
        }
    }

    private void setCameraInFront() {
        fpsController.affine.setToIdentity();
        fpsController.setXYZ(cameraOffset);
        fpsController.setRotateXYZ(0, 0, 0);
    }

    private int getSpeed(int length) {
        return (int) Math.round(-200.0 * Math.log(0.1 * length + 3) + 1315);
        // other speed functions: one linear and others logarithmic
//        return (int) Math.round(-(10.0 / 51.0) * length + 500);
//        return (int) Math.round(-150.0 * Math.log(0.1 * length + 8) + 1000);
//        return (int) Math.round(-150.0 * Math.log(0.1 * length + 15) + 1000);
//        return (int) Math.round(-150.0 * Math.log(0.05 * length + 15) + 800);
//        return (int) Math.round(-110.0 * Math.log(0.05 * length + 20) + 670);
    }

    private class PlanetLabel extends ImageView implements BillboardBehavior<PlanetLabel> {
        private String name;
        private Image textImage;

        public PlanetLabel(String name) {
            super();

            this.name = name;
            Font font = Font.loadFont(getClass().getResourceAsStream("/Helvetica CE Bold.ttf"), 60);
            Text t = new Text(name);
            t.setFont(font);
            t.setFill(Color.ALICEBLUE);
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.rgb(255, 255, 255, 0.0));
            textImage = t.snapshot(sp, null);
            setPreserveRatio(true);
            setSmooth(true);
            setImage(textImage);
            setDepthTest(DepthTest.ENABLE);
        }

        @Override
        public PlanetLabel getBillboardNode() {
            return this;
        }

        @Override
        public Node getOther() {
            return camera;
        }

        public double getTextImageWidth() {
            return textImage.getWidth();
        }

        public double getTextImageHeight() {
            return textImage.getHeight();
        }

        public String getName() {
            return name;
        }
    }
}
