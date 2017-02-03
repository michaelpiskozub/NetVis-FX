package netvis.fxyz.extras;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.*;
import javafx.util.Callback;
import netvis.fxyz.geometry.Vector3D;

/**
 * Transformable as an abstract class instead of an interface. Original interface version creates an affine transform
 * that is shared by all instances of classes that implement this interface. This is clearly wrong under the principles
 * of Object-Oriented Programming, e.g. FPSController can be created in two separate classes and changes in the position
 * of the first one affect the position of the second one. This should not happen as two instances of FPSController
 * class should be independent of each other. Creating it as an abstract class solves this problem.
 * @author Improvement by Michal Piskozub, based on {@link Transformable} class by Dub
 * @param <T> Node type to be used
 */
public abstract class AbstractTransformable<T extends Node> {
    public enum RotateOrder {
        XYZ,
        XZY,
        YXZ,
        YZX,
        ZXY,
        ZYX,
        USE_AFFINE;

        RotateOrder() {}
    }

    // Simple Transforms

    //Rotates
    public Rotate rotateX = new Rotate(0.0, Rotate.X_AXIS);
    public Rotate rotateY = new Rotate(0.0, Rotate.Y_AXIS);
    public Rotate rotateZ = new Rotate(0.0, Rotate.Z_AXIS);

    public void setRotate(double x, double y, double z) {
        rotateX.setAngle(x);
        rotateY.setAngle(y);
        rotateZ.setAngle(z);
    }

    public void setRotateXYZ(double x, double y, double z) {
        rotateX.setAngle(x);
        rotateY.setAngle(y);
        rotateZ.setAngle(z);
    }

    public void setRotateX(double x) {
        rotateX.setAngle(x);
    }

    public void setRotateY(double y) {
        rotateY.setAngle(y);
    }

    public void setRotateZ(double z) {
        rotateZ.setAngle(z);
    }

    public double getRotateX() {
        return rotateX.getAngle();
    }


    public double getRotateY() {
        return rotateY.getAngle();
    }

    public double getRotateZ() {
        return rotateZ.getAngle();
    }

    // Translates
    public Translate t = new Translate();
    public Translate p = new Translate();
    public Translate ip = new Translate();

    public void setTx(double x) {
        t.setX(x);
    }

    public void setTy(double y) {
        t.setY(y);
    }

    public void setTz(double z) {
        t.setZ(z);
    }

    public double getTx() {
        return t.getX();
    }

    public double getTy() {
        return t.getY();
    }

    public double getTz() {
        return t.getZ();
    }

    // Scale
    public Scale s = new Scale();

    public void setScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    public void setScale(double x, double y, double z) {
        s.setX(x);
        s.setY(y);
        s.setZ(z);
    }

    // Transform methods
    public void setPivot(double x, double y, double z) {
        p.setX(x);
        p.setY(y);
        p.setZ(z);
        ip.setX(-x);
        ip.setY(-y);
        ip.setZ(-z);
    }

    //advanced transform
    public Affine affine = new Affine();

    //Vectors: fwd, right, up   Point3D: pos

    //Forward / look direction
    Callback<Transform, Vector3D> forwardDirCallback = (a) -> new Vector3D(a.getMzx(), a.getMzy(), a.getMzz());
    Callback<Transform, Vector3D> forwardMatrixRowCallback = (a) -> new Vector3D(a.getMxz(), a.getMyz(), a.getMzz());
    // up direction
    Callback<Transform, Vector3D> upDirCallback = (a) -> new Vector3D(a.getMyx(), a.getMyy(), a.getMyz());
    Callback<Transform, Vector3D> upMatrixRowCallback = (a) -> new Vector3D(a.getMxy(), a.getMyy(), a.getMzy());
    // right direction
    Callback<Transform, Vector3D> rightDirCallback = (a) -> new Vector3D(a.getMxx(), a.getMxy(), a.getMxz());
    Callback<Transform, Vector3D> rightMatrixRowCallback = (a) -> new Vector3D(a.getMxx(), a.getMyx(), a.getMzx());
    //position
    Callback<Transform, Point3D> positionCallback = (a) -> new Point3D(a.getTx(), a.getTy(), a.getTz());

    public Vector3D getForwardDirection() {
        return forwardDirCallback.call(getTransformableNode().getLocalToSceneTransform());
    }

    public Vector3D getForwardMatrixRow() {
        return forwardMatrixRowCallback.call(getTransformableNode().getLocalToSceneTransform());
    }

    public Vector3D getRightDirection() {
        return rightDirCallback.call(getTransformableNode().getLocalToSceneTransform());
    }

    public Vector3D getRightMatrixRow() {
        return rightMatrixRowCallback.call(getTransformableNode().getLocalToSceneTransform());
    }

    public Vector3D getUpDirection() {
        return upDirCallback.call(getTransformableNode().getLocalToSceneTransform());
    }

    public Vector3D getUpMatrixRow() {
        return upMatrixRowCallback.call(getTransformableNode().getLocalToSceneTransform());
    }

    public Point3D getPosition() {
        return positionCallback.call(getTransformableNode().getLocalToSceneTransform());
    }

    public void reset() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        rotateX.setAngle(0.0);
        rotateY.setAngle(0.0);
        rotateZ.setAngle(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);

        affine.setMxx(1);
        affine.setMxy(0);
        affine.setMxz(0);

        affine.setMyx(0);
        affine.setMyy(1);
        affine.setMyz(0);

        affine.setMzx(0);
        affine.setMzy(0);
        affine.setMzz(1);
    }

    public void resetTSP() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);
    }

    public void debug() {
        System.out.println("t = (" +
                t.getX() + ", " +
                t.getY() + ", " +
                t.getZ() + ")  " +
                "r = (" +
                rotateX.getAngle() + ", " +
                rotateY.getAngle() + ", " +
                rotateZ.getAngle() + ")  " +
                "s = (" +
                s.getX() + ", " +
                s.getY() + ", " +
                s.getZ() + ")  " +
                "p = (" +
                p.getX() + ", " +
                p.getY() + ", " +
                p.getZ() + ")  " +
                "ip = (" +
                ip.getX() + ", " +
                ip.getY() + ", " +
                ip.getZ() + ")" +
                "affine = " + affine);
    }


    /**
     * Toggle Transforms on / off
     * @param b
     */
    public void enableTransforms(boolean b) {
        // if true, check if node is a camera
        if (b) {
            if (getRotateOrder() != null) {
                switch (getRotateOrder()) {
                    case XYZ:
                        getTransformableNode().getTransforms().addAll(t, p, rotateZ, rotateY, rotateX, s, ip);
                        break;
                    case XZY:
                        getTransformableNode().getTransforms().addAll(t, p, rotateY, rotateZ, rotateX, s, ip);
                        break;
                    case YXZ:
                        getTransformableNode().getTransforms().addAll(t, p, rotateZ, rotateX, rotateY, s, ip);
                        break;
                    case YZX:
                        getTransformableNode().getTransforms().addAll(t, p, rotateX, rotateZ, rotateY, s, ip);
                        break;
                    case ZXY:
                        getTransformableNode().getTransforms().addAll(t, p, rotateY, rotateX, rotateZ, s, ip);
                        break;
                    case ZYX:
                        getTransformableNode().getTransforms().addAll(t, p, rotateX, rotateY, rotateZ, s, ip);
                        break;
                    case USE_AFFINE:
                        getTransformableNode().getTransforms().addAll(affine);
                        break;
                }

            }
            // if false clear transforms from Node.
        } else if (!b) {
            getTransformableNode().getTransforms().clear();
            reset();
        }
    }

    public void initialize() {
        if (getTransformableNode() != null) {
            enableTransforms(true);
        }
    }

    public abstract T getTransformableNode();
    public abstract RotateOrder getRotateOrder();
}
