package netvis.view;

import javafx.beans.property.BooleanProperty;
import javafx.scene.Scene;
import netvis.model.DataController;
import netvis.view.visualizations.fx.GalaxyVisualization;
import netvis.view.visualizations.fx.ParallelGalaxyVisualization;
import netvis.view.visualizations.jogl.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class VisualizationsController {
    public static final VisualizationsController INSTANCE = new VisualizationsController();
    private Map<String, VisualizationJOGL> joglVisualizations;
    private Map<String, VisualizationFX> fxVisualizations;
    private Map<String, Visualization> allVisualizations;
    private Visualization currentVisualization;

    private VisualizationsController() {
        joglVisualizations = new LinkedHashMap<>();
        fxVisualizations = new LinkedHashMap<>();
        allVisualizations = new LinkedHashMap<>();
    }

    public Set<Map.Entry<String, VisualizationJOGL>> getJOGLVisualizations() {
        return joglVisualizations.entrySet();
    }

    public Set<Map.Entry<String, VisualizationFX>> getFXVisualizations() {
        return fxVisualizations.entrySet();
    }

    public Set<Map.Entry<String, Visualization>> getAllVisualizations() {
        return allVisualizations.entrySet();
    }

    public Set<String> getJOGLVisualizationsNames() {
        return joglVisualizations.keySet();
    }

    public Set<String> getFXVisualizationsNames() {
        return fxVisualizations.keySet();
    }

    public Set<String> getAllVisualizationsNames() {
        return allVisualizations.keySet();
    }

    public void setCurrentVisualization(Visualization visualization) {
        currentVisualization = visualization;
    }

    public void deactivateCurrentVisualization() {
        if (currentVisualization != null) {
            currentVisualization.deactivate();
        }
    }

    public void initializeAllVisualizations(DataController dataController, Scene scene, BooleanProperty pauseProperty,
                                            BooleanProperty goToEndProperty) {
        VisualizationFX galaxy = new GalaxyVisualization(dataController, scene, pauseProperty, goToEndProperty);
        VisualizationFX parallelGalaxy = new ParallelGalaxyVisualization(dataController, scene, pauseProperty,
                goToEndProperty);
        VisualizationJOGL trafficVolume = new TrafficVolumeVisualization(dataController);
        VisualizationJOGL attributeDistribution = new AttributeDistributionVisualization(dataController);
        VisualizationJOGL groupsOfActivity = new GroupsOfActivityVisualization(dataController);
        VisualizationJOGL heatmapOfActivity = new HeatmapOfActivityVisualization(dataController);
        VisualizationJOGL dataflow = new DataflowVisualization(dataController);
        VisualizationJOGL spinningCube = new SpinningCubeVisualization(dataController);

        fxVisualizations.put(galaxy.getName(), galaxy);
        fxVisualizations.put(parallelGalaxy.getName(), parallelGalaxy);
        joglVisualizations.put(trafficVolume.getName(), trafficVolume);
        joglVisualizations.put(attributeDistribution.getName(), attributeDistribution);
        joglVisualizations.put(groupsOfActivity.getName(), groupsOfActivity);
        joglVisualizations.put(heatmapOfActivity.getName(), heatmapOfActivity);
        joglVisualizations.put(dataflow.getName(), dataflow);
        joglVisualizations.put(spinningCube.getName(), spinningCube);

        allVisualizations.putAll(fxVisualizations);
        allVisualizations.putAll(joglVisualizations);
    }
}
