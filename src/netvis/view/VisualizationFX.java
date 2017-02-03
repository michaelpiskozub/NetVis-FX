package netvis.view;

import javafx.scene.Node;
import javafx.scene.SubScene;
import netvis.model.DataController;

public abstract class VisualizationFX extends Visualization {
    protected SubScene subScene;

    public VisualizationFX(DataController dataController) {
        super(dataController);
    }

    @Override
    public Node getVisualization() {
        return subScene;
    }
}
