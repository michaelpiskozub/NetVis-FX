package netvis.view.visualizations.fx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.PickResult;
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

import java.util.*;

public class ParallelGalaxyVisualization extends VisualizationFX {
    private Map<String, Map<String, Planet>> planets;
    private Map<String, Map<String, PlanetLabel>> planetsLabels;
    private List<PacketTransitionFactory> packetTransitions;

    private final Point3D ORIGIN = new Point3D(0, 0, 0);
    private boolean isConstantSpeed;
    private boolean arePlanetsLabelsShown;
    private PlanetsPopOver planetsPopOver;
    private PacketsPopOver packetsPopOver;
    private BooleanProperty pauseProperty;
    private BooleanProperty goToEndProperty;
    private Timeline newPacketsTimeline;
    private final int MAX_PLANETS_PER_COLUMN = 10;
    private final int PLANET_RADIUS = 50;
    private final int COLOR_GENERATOR_COLLECTION = ColorGenerator.COLLECTION_ONE_RED;
    private final int COLOR_GENERATOR_ORDER = ColorGenerator.ORDER_RANDOM;
    private ColorGenerator colorGenerator;
    private Group root;
    private Map<String, List<Point3D>> planetsPositions;
    private Map<String, List<Point3D>> planetsTextPositions;
    private List<Point3D> planetColumnsTextPositions;
    private List<String> planetColumnsNames;
    private int planetColumnsCounter;
    private Group planetsGroup;
    private Planet sun;
    private Point3D cameraOffset;
    private FPSController fpsController;
    private AdvancedCamera camera;

    public ParallelGalaxyVisualization(DataController dataController, Scene scene, BooleanProperty pauseProperty,
                                       BooleanProperty goToEndProperty) {
        super (dataController);

        planets = new LinkedHashMap<>();
        planets.put("Source IP", new LinkedHashMap<>());
        planets.put("Destination IP", new LinkedHashMap<>());
        planets.put("Source Port", new LinkedHashMap<>());
        planets.put("Destination Port", new LinkedHashMap<>());
        planets.put("Protocol", new LinkedHashMap<>());
        planetsLabels = new LinkedHashMap<>();
        planetsLabels.put("Source IP", new LinkedHashMap<>());
        planetsLabels.put("Destination IP", new LinkedHashMap<>());
        planetsLabels.put("Source Port", new LinkedHashMap<>());
        planetsLabels.put("Destination Port", new LinkedHashMap<>());
        planetsLabels.put("Protocol", new LinkedHashMap<>());
        packetTransitions = new ArrayList<>();

        isConstantSpeed = false;
        arePlanetsLabelsShown = true;
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
        planetsPositions = new LinkedHashMap<>();
        planetsPositions.put("Source IP", new ArrayList<>());
        planetsPositions.put("Destination IP", new ArrayList<>());
        planetsPositions.put("Source Port", new ArrayList<>());
        planetsPositions.put("Destination Port", new ArrayList<>());
        planetsPositions.put("Protocol", new ArrayList<>());
        planetsTextPositions = new LinkedHashMap<>();
        planetsTextPositions.put("Source IP", new ArrayList<>());
        planetsTextPositions.put("Destination IP", new ArrayList<>());
        planetsTextPositions.put("Source Port", new ArrayList<>());
        planetsTextPositions.put("Destination Port", new ArrayList<>());
        planetsTextPositions.put("Protocol", new ArrayList<>());
        planetColumnsTextPositions = new ArrayList<>();
        planetColumnsNames = new ArrayList<>(Arrays.asList("Source IP", "Destination IP", "Source Port",
                "Destination Port", "Protocol"));
        planetColumnsCounter = 0;
        planetsGroup = new Group();
        sun = new Planet(10, new ColorPair(Color.WHITE, Color.WHITE));
        Translate sunTranslate = new Translate(ORIGIN.getX(), ORIGIN.getY(), ORIGIN.getZ());
        sun.getTransforms().add(sunTranslate);
        cameraOffset = new Point3D(1530, 75, -1650);

        initializeColumnPositions(planetsPositions.get("Source IP"), 560, -550, 0, 120);
        initializeColumnPositions(planetsPositions.get("Destination IP"), 1120, -550, 0, 120);
        initializeColumnPositions(planetsPositions.get("Source Port"), 1680, -550, 0, 120);
        initializeColumnPositions(planetsPositions.get("Destination Port"), 2240, -550, 0, 120);
        initializeColumnPositions(planetsPositions.get("Protocol"), 2800, -550, 0, 120);
        initializeColumnPositions(planetsTextPositions.get("Source IP"), 640, -550, 25, 120);
        initializeColumnPositions(planetsTextPositions.get("Destination IP"), 1200, -550, 25, 120);
        initializeColumnPositions(planetsTextPositions.get("Source Port"), 1760, -550, 25, 120);
        initializeColumnPositions(planetsTextPositions.get("Destination Port"), 2320, -550, 25, 120);
        initializeColumnPositions(planetsTextPositions.get("Protocol"), 2880, -550, 25, 120);
        initializeRowPositions(planetColumnsTextPositions, 435, -700, 0, 560);

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

        CheckBox speedCheckBox = new CheckBox("Constant Speed");
        speedCheckBox.selectedProperty().addListener((observable1, oldValue1, newValue1) -> {
            isConstantSpeed = !isConstantSpeed;
        });
        CheckBox labelsCheckBox = new CheckBox("Planet Labels");
        labelsCheckBox.setSelected(true);
        labelsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
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

        vbox.getChildren().addAll(speedCheckBox, labelsCheckBox, planetsPopOverButton, packetsPopOverButton);
        return Borders.wrap(vbox).lineBorder().thickness(1).radius(5).build().build();
    }

    @Override
    public String getName() {
        return "Parallel Galaxy";
    }

    @Override
    public String getDescription() {
        return "Implementation of parallel coordinates as a galaxy. Columns of planets represent IP addresses, ports " +
                "and protocols. Packets transition from the sun (the origin point) to exactly one planet from each " +
                "column.";
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
                    speed = 700;
                } else {
                    speed = getSpeed(p.LENGTH, newPackets.size());
                }
                Planet sourceIPTarget = getSourceIPTarget(p);
                Planet destinationIPTarget = getDestinationIPTarget(p);
                Planet sourcePortTarget = getSourcePortTarget(p);
                Planet destinationPortTarget = getDestinationPortTarget(p);
                Planet protocolTarget = getProtocolTarget(p);
                sourceIPTarget.increaseHitsBy(1);
                sourceIPTarget.increaseTotalLengthBy(p.LENGTH);
                destinationIPTarget.increaseHitsBy(1);
                destinationIPTarget.increaseTotalLengthBy(p.LENGTH);
                sourcePortTarget.increaseHitsBy(1);
                sourcePortTarget.increaseTotalLengthBy(p.LENGTH);
                destinationPortTarget.increaseHitsBy(1);
                destinationPortTarget.increaseTotalLengthBy(p.LENGTH);
                protocolTarget.increaseHitsBy(1);
                protocolTarget.increaseTotalLengthBy(p.LENGTH);

                KeyFrame keyFrame = new KeyFrame(Duration.millis(p.getTimeMillis()), event -> {
                    PacketTransitionFactory ptf = new PacketTransitionFactory(p, speed, packetColor, root,
                            packetTransitions, sun, sourceIPTarget, destinationIPTarget, sourcePortTarget,
                            destinationPortTarget, protocolTarget);
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
                Planet sourceIPTarget = getSourceIPTarget(p);
                Planet destinationIPTarget = getDestinationIPTarget(p);
                Planet sourcePortTarget = getSourcePortTarget(p);
                Planet destinationPortTarget = getDestinationPortTarget(p);
                Planet protocolTarget = getProtocolTarget(p);
                sourceIPTarget.increaseHitsBy(1);
                sourceIPTarget.increaseTotalLengthBy(p.LENGTH);
                destinationIPTarget.increaseHitsBy(1);
                destinationIPTarget.increaseTotalLengthBy(p.LENGTH);
                sourcePortTarget.increaseHitsBy(1);
                sourcePortTarget.increaseTotalLengthBy(p.LENGTH);
                destinationPortTarget.increaseHitsBy(1);
                destinationPortTarget.increaseTotalLengthBy(p.LENGTH);
                protocolTarget.increaseHitsBy(1);
                protocolTarget.increaseTotalLengthBy(p.LENGTH);
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
        planetColumnsCounter = 0;
        planets.get("Source IP").clear();
        planets.get("Destination IP").clear();
        planets.get("Source Port").clear();
        planets.get("Destination Port").clear();
        planets.get("Protocol").clear();
        planetsLabels.get("Source IP").clear();
        planetsLabels.get("Destination IP").clear();
        planetsLabels.get("Source Port").clear();
        planetsLabels.get("Destination Port").clear();
        planetsLabels.get("Protocol").clear();
        colorGenerator = new ColorGenerator(COLOR_GENERATOR_COLLECTION, COLOR_GENERATOR_ORDER);
        planetsPopOver.reset();
        packetsPopOver.reset();

        initializeIPMode("Source IP");
        initializeIPMode("Destination IP");
        initializePortMode("Source Port");
        initializePortMode("Destination Port");
        for (Point3D p : planetColumnsTextPositions) {
            PlanetLabel columnLabel = new PlanetLabel(planetColumnsNames.get(planetColumnsCounter));
            planetColumnsCounter++;
            columnLabel.setTranslateX(p.getX() + ORIGIN.getX());
            columnLabel.setTranslateY(p.getY() + ORIGIN.getY());
            columnLabel.setTranslateZ(p.getZ() + ORIGIN.getZ());
            planetsGroup.getChildren().add(columnLabel);
        }
    }

    private void initializeIPMode(String planetType) {
        int planetRange = (int) Math.floor(255 / (MAX_PLANETS_PER_COLUMN * 1.0));
        int previousRange = 0;
        for (int i = 0; i < MAX_PLANETS_PER_COLUMN; i++) {
            if (i < MAX_PLANETS_PER_COLUMN - 1) {
                int currentRange = (i + 1) * planetRange;
                String planetName = previousRange + "-" + (currentRange - 1) + ".x.y.z";
                createPlanet(planetName, planetType);
                previousRange = currentRange;
            } else {
                String planetName = previousRange + "-" + 255 + ".x.y.z";
                createPlanet(planetName, planetType);
            }
        }
    }

    private void initializePortMode(String planetType) {
        //range: 0-65535
        int planetRange = (int) Math.ceil(65535 / (MAX_PLANETS_PER_COLUMN * 1.0));
        int previousRange = 0;
        for (int i = 0; i < MAX_PLANETS_PER_COLUMN; i++) {
            if (i < MAX_PLANETS_PER_COLUMN - 1) {
                int currentRange = (i + 1) * planetRange;
                String planetName = previousRange + "-" + (currentRange - 1);
                createPlanet(planetName, planetType);
                previousRange = currentRange;
            } else {
                String planetName = previousRange + "-" + 65535;
                createPlanet(planetName, planetType);
            }
        }
    }

    private Planet getSourcePortTarget(Packet p) {
        return getPortTarget(p, p.SOURCE_PORT, "Source Port");
    }

    private Planet getDestinationPortTarget(Packet p) {
        return getPortTarget(p, p.DESTINATION_PORT, "Destination Port");
    }

    private Planet getPortTarget(Packet packet, int port, String planetType) {
        int planetRange = (int) Math.ceil(65535 / (MAX_PLANETS_PER_COLUMN * 1.0));
        int planetIndex = port / planetRange;
        int lowerRange = planetRange * planetIndex;
        int higherRange = (planetRange * (planetIndex + 1)) - 1;
        if (higherRange > 65535) {
            higherRange = 65535;
        }
        String planetName = lowerRange + "-" + higherRange;
        Planet planet = planets.get(planetType).get(planetName);
        if (planet == null) {
            System.out.println(packet);
        }
        return planet;
    }

    private Planet getSourceIPTarget(Packet p) {
        return getIPTarget(p, p.SOURCE_IP, "Source IP");
    }

    private Planet getDestinationIPTarget(Packet p) {
        return getIPTarget(p, p.DESTINATION_IP, "Destination IP");
    }

    private Planet getIPTarget(Packet packet, String ip, String planetType) {
        int planetRange = (int) Math.floor(255 / (MAX_PLANETS_PER_COLUMN * 1.0));
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
            if (planetIndex == MAX_PLANETS_PER_COLUMN) {
                planetIndex = MAX_PLANETS_PER_COLUMN - 1;
            }
            int lowerRange = planetRange * planetIndex;
            int higherRange = (planetRange * (planetIndex + 1)) - 1;
            if (higherRange > 255 || planetIndex == MAX_PLANETS_PER_COLUMN - 1) {
                higherRange = 255;
            }
            planetName = lowerRange + "-" + higherRange + ".x.y.z";
        }
        Planet planet = planets.get(planetType).get(planetName);
        if (planet == null) {
            System.out.println(packet);
        }
        return planet;
    }

    private Planet getProtocolTarget(Packet p) {
        if (!planets.get("Protocol").containsKey(p.PROTOCOL)) {
            createPlanet(p.PROTOCOL, "Protocol");
        }
        if (planets.get("Protocol").containsKey(p.PROTOCOL)) {
            return planets.get("Protocol").get(p.PROTOCOL);
        } else {
            return planets.get("Protocol").get("Others");
        }
    }

    private String getProtocolTargetName(String name, Map<String, Planet> planetsCollection,
                                         List<Point3D> positionsList) {
        if (planetsCollection.size() == positionsList.size() - 1) {
            return "Others";
        } else {
            return name;
        }
    }

    public void initializeColumnPositions(List<Point3D> positionsList, int x, int y, int z, int offset) {
        int currentY = y;
        for (int i = 0; i < MAX_PLANETS_PER_COLUMN; i++) {
            positionsList.add(new Point3D(x, currentY, z));
            currentY += offset;
        }
    }

    public void initializeRowPositions(List<Point3D> positionsList, int x, int y, int z, int offset) {
        int currentX = x;
        for (int i = 0; i < 5; i++) {
            positionsList.add(new Point3D(currentX, y, z));
            currentX += offset;
        }
    }

    private void createPlanet(String name, String planetType) {
        if (planets.get(planetType).size() < planetsPositions.get(planetType).size()) {
            Point3D planetPosition = planetsPositions.get(planetType).get(planets.get(planetType).size());
            Planet planet = new Planet(PLANET_RADIUS, colorGenerator.getNextColor(), planetType);
            String planetName;
            if (planetType.equals("Protocol")) {
                planetName = getProtocolTargetName(name, planets.get(planetType), planetsPositions.get(planetType));
            } else {
                planetName = name;
            }
            planet.setName(planetName);

            planet.setTranslateX(planetPosition.getX() + ORIGIN.getX());
            planet.setTranslateY(planetPosition.getY() + ORIGIN.getY());
            planet.setTranslateZ(planetPosition.getZ() + ORIGIN.getZ());

            Point3D planetTextPosition = planetsTextPositions.get(planetType).get(planets.get(planetType).size());
            planets.get(planetType).put(planetName, planet);
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
            planetsLabels.get(planetType).put(planetLabel.getName(), planetLabel);

            planetsGroup.getChildren().add(planet);
            if (arePlanetsLabelsShown) {
                planetsGroup.getChildren().add(planetLabel);
            }
        }
    }

    private void togglePlanetLabels() {
        if (arePlanetsLabelsShown) {
            arePlanetsLabelsShown = false;
            Map<String, PlanetLabel> sourceIPLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Source IP"));
            Map<String, PlanetLabel> destinationIPLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Destination IP"));
            Map<String, PlanetLabel> sourcePortLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Source Port"));
            Map<String, PlanetLabel> destinationPortLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Destination Port"));
            Map<String, PlanetLabel> protocolLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Protocol"));

            sourceIPLabelsCopy.values().stream().filter(pl -> planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().remove(pl));
            destinationIPLabelsCopy.values().stream().filter(pl -> planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().remove(pl));
            sourcePortLabelsCopy.values().stream().filter(pl -> planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().remove(pl));
            destinationPortLabelsCopy.values().stream().filter(pl -> planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().remove(pl));
            protocolLabelsCopy.values().stream().filter(pl -> planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().remove(pl));
        } else {
            arePlanetsLabelsShown = true;
            Map<String, PlanetLabel> sourceIPLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Source IP"));
            Map<String, PlanetLabel> destinationIPLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Destination IP"));
            Map<String, PlanetLabel> sourcePortLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Source Port"));
            Map<String, PlanetLabel> destinationPortLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Destination Port"));
            Map<String, PlanetLabel> protocolLabelsCopy = new LinkedHashMap<>(planetsLabels.get("Protocol"));

            sourceIPLabelsCopy.values().stream().filter(pl -> !planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().add(pl));
            destinationIPLabelsCopy.values().stream().filter(pl -> !planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().add(pl));
            sourcePortLabelsCopy.values().stream().filter(pl -> !planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().add(pl));
            destinationPortLabelsCopy.values().stream().filter(pl -> !planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().add(pl));
            protocolLabelsCopy.values().stream().filter(pl -> !planetsGroup.getChildren().
                    contains(pl)).forEach(pl -> planetsGroup.getChildren().add(pl));
        }
    }

    private void setCameraInFront() {
        fpsController.affine.setToIdentity();
        fpsController.setXYZ(cameraOffset);
        fpsController.setRotateXYZ(0, 0, 0);
    }

    private int getSpeed(int packetLength, int numberOfPackets) {
        double shift;
        if (numberOfPackets > 100) {
            shift = 5 / 3.0 * numberOfPackets + (3445 / 3.0);
        } else {
            shift = 1315;
        }
        return (int) Math.round(-200.0 * Math.log(0.1 * packetLength + 3) + shift);
        // other speed functions: one linear and others logarithmic
//        return (int) Math.round(-(10.0 / 51.0) * length + 500);
//        return (int) Math.round(-150.0 * Math.log(0.1 * length + 8) + 1000);
//        return (int) Math.round(-150.0 * Math.log(0.1 * length + 15) + 1000);
//        return (int) Math.round(-150.0 * Math.log(0.05 * length + 15) + 800);
//        return (int) Math.round(-110.0 * Math.log(0.05 * length + 20) + 670);
    }

    private class PlanetLabel extends ImageView {
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
