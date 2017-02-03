package netvis.view.util.fx;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;
import netvis.model.Packet;
import netvis.fxyz.geometry.Ray;

public class PacketTransition extends Transition {
    protected Packet packet;
    protected PhongMaterial material;
    protected Ray ray;
    protected Planet target;
    protected Duration time;
    protected Group rootGroup;
    protected PacketBox packetBox;
    protected double distance;

    public PacketTransition(Packet packet, Ray ray, Color packetColor, Planet target, Duration time, Group rootGroup) {
        super();

        this.packet = packet;
        material = new PhongMaterial(packetColor);
        this.ray = ray;
        this.target = target;
        this.time = time;
        this.rootGroup = rootGroup;
        packetBox = new PacketBox(packet, 12, 12, 12);
        packetBox.setTranslateX((ray.getOrigin()).getX());
        packetBox.setTranslateY((ray.getOrigin()).getY());
        packetBox.setTranslateZ((ray.getOrigin()).getZ());
        packetBox.setMaterial(material);
        // to match the color of target, change the line above to
        //packetBox.setMaterial(new PhongMaterial(target.getDiffuseColor()));
        distance = ray.getOrigin().distance(
                Point3D.ZERO.add(target.getTranslateX(), target.getTranslateY(), target.getTranslateZ()));
        setCycleDuration(time);
        setInterpolator(Interpolator.LINEAR);
        rootGroup.getChildren().add(packetBox);
    }

    @Override
    protected void interpolate(double frac) {
        ray.project(distance * frac);
        packetBox.setTranslateX(ray.getPosition().getX());
        packetBox.setTranslateY(ray.getPosition().getY());
        packetBox.setTranslateZ(ray.getPosition().getZ());
    }

    public Planet getTarget() {
        return target;
    }

    public Ray getRay() {
        return ray;
    }

    public Group getRootGroup() {
        return rootGroup;
    }

    public PacketBox getPacketBox() {
        return packetBox;
    }

    public void interrupt() {
        stop();
        rootGroup.getChildren().removeAll(packetBox);
    }
}
