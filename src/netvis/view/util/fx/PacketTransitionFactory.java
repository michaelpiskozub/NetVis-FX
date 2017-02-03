package netvis.view.util.fx;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import netvis.model.Packet;
import netvis.fxyz.geometry.Ray;

import java.util.ArrayList;
import java.util.List;

public class PacketTransitionFactory {
    private List<PacketTransition> transitionChain;

    public PacketTransitionFactory(Packet packet, double speed, Color packetColor, Group root,
                                   List<PacketTransitionFactory> packetTransitions, Planet... objects) {
        transitionChain = new ArrayList<>();

        for (int i = 0; i < objects.length - 1; i++) {
            Planet source = objects[i];
            Planet destination = objects[i + 1];
            Ray ray = createRay(source, destination);
            double d = Math.round(getDistance(source, destination) / speed * 1000);
            if (d < 0) {
                d = 1000.0;
            }
            Duration time = Duration.millis(d);
            PacketTransition t = new PacketTransition(packet, ray, packetColor, destination, time, root);
            transitionChain.add(t);
        }

        PacketTransition lastT = transitionChain.get(transitionChain.size() - 1);
        lastT.setOnFinished(event -> {
            if (lastT.getTarget().getBoundsInParent().contains(lastT.getRay().getPosition())) {
                lastT.getRootGroup().getChildren().removeAll(lastT.getPacketBox());
                packetTransitions.remove(this);
            }
        });

        for (int i = transitionChain.size() - 2; i >= 0; i--) {
            PacketTransition nextT = transitionChain.get(i + 1);
            PacketTransition currentT = transitionChain.get(i);
            currentT.setOnFinished(event -> {
                if (currentT.getTarget().getBoundsInParent().contains(currentT.getRay().getPosition())) {
                    currentT.getRootGroup().getChildren().removeAll(currentT.getPacketBox());
                    nextT.playFromStart();
                }
            });
        }
        packetTransitions.add(this);
    }

    private Ray createRay(Planet from, Planet to) {
        Point3D source = Point3D.ZERO.add(from.getTranslateX(), from.getTranslateY(), from.getTranslateZ());
        Point3D destination = Point3D.ZERO.add(to.getTranslateX(), to.getTranslateY(), to.getTranslateZ());
        Point3D direction = destination.subtract(source);
        return new Ray(source, direction);
    }

    private double getDistance(Planet from, Planet to) {
        Point3D source = Point3D.ZERO.add(from.getTranslateX(), from.getTranslateY(), from.getTranslateZ());
        Point3D destination = Point3D.ZERO.add(to.getTranslateX(), to.getTranslateY(), to.getTranslateZ());
        return destination.distance(source);
    }

    public PacketTransition getHeadOfTransitionChain() {
        return transitionChain.get(0);
    }

    public void interrupt() {
        transitionChain.forEach(netvis.view.util.fx.PacketTransition::interrupt);
    }

    public void pauseAll() {
        transitionChain.forEach(netvis.view.util.fx.PacketTransition::pause);
    }

    public void resumeAll() {
        transitionChain.forEach(netvis.view.util.fx.PacketTransition::play);
    }
}
