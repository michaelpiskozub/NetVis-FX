package netvis.view;

import com.jogamp.opengl.util.FPSAnimator;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import netvis.model.DataController;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import java.awt.*;

public abstract class VisualizationJOGL extends Visualization implements GLEventListener {
    protected GLJPanel visualizationPanel;
    protected SwingNode swingNode;
    protected FPSAnimator fpsAnimator;

    public VisualizationJOGL(DataController dataController, int fps) {
        super(dataController);
        visualizationPanel = new GLJPanel(new GLCapabilities(GLProfile.getDefault()));
        visualizationPanel.setPreferredSize(new Dimension(3840, 2160));
        visualizationPanel.addGLEventListener(this);
        swingNode = new SwingNode();
        fpsAnimator = new FPSAnimator(visualizationPanel, fps);
    }

    @Override
    public Node getVisualization() {
        SwingUtilities.invokeLater(() -> swingNode.setContent(visualizationPanel));
        return swingNode;
    }

    @Override
    public void activate() {
        super.activate();
        fpsAnimator.start();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SwingUtilities.invokeLater(fpsAnimator::stop);
    }

    @Override
    public void interrupt() {
        SwingUtilities.invokeLater(() -> {
            fpsAnimator.stop();
            visualizationPanel.destroy();
        });
    }
}
