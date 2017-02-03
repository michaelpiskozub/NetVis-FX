/*
 * Copyright (C) 2013-2015 F(X)yz, 
 * Sean Phillips, Jason Pollastrini and Jose Pereda
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package netvis.fxyz.cameras.controllers;

import javafx.geometry.Point3D;
import netvis.fxyz.utils.AnimationPreference;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import netvis.fxyz.utils.MathUtils;

/**
 * Creates a First-Person Shooter like controller of a camera. Pressing key: W moves the camera forward, S moves the
 * camera backward, A moves the camera to the left, D moves the camera to the right. Holding the left mouse button and
 * dragging moves the camera around. Holding the right mouse button and initially indicating direction with a mouse
 * (either up or down) moves the camera upward or downward. Holding the Shift key increases the speed of above
 * interactions.
 * Improvement contains implementation of moving camera up and down, and adjusting interaction speeds.
 * @author Dub, improvement by Michal Piskozub
 */
public class FPSController extends CameraController {

    private boolean fwd, strafeL, strafeR, back, up, down, mouseLookEnabled;
    private double speed = 2.0;
    private final double maxSpeed = 20.0, minSpeed = 2.0;

    public FPSController() {
        super(true, AnimationPreference.TIMER);
    }

    @Override
    public void update() {
        if (fwd && !back) {
            moveForward();
        }
        if (strafeL) {
            strafeLeft();
        }
        if (strafeR) {
            strafeRight();
        }
        if (back && !fwd) {
            moveBack();
        }
        if (up && !down) {
            moveUp();
        }
        if (down && !up) {
            moveDown();
        }
    }

    @Override
    public void handleKeyEvent(KeyEvent event, boolean handle) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            switch (event.getCode()) {
                case W:
                    fwd = true;
                    break;
                case S:
                    back = true;
                    break;
                case A:
                    strafeL = true;
                    break;
                case D:
                    strafeR = true;
                    break;
                case SHIFT:
                    speed = maxSpeed;
                    break;
            }
        } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
            switch (event.getCode()) {
                case W:
                    fwd = false;
                    break;
                case S:
                    back = false;
                    break;
                case A:
                    strafeL = false;
                    break;
                case D:
                    strafeR = false;
                    break;
                case SHIFT:
                    speed = minSpeed;
                    break;
            }
        }
    }

    @Override
    protected void handlePrimaryMouseDrag(MouseEvent event, Point2D dragDelta, double modifier) {
        if (!mouseLookEnabled) {
            t.setX(getPosition().getX());
            t.setY(getPosition().getY());
            t.setZ(getPosition().getZ());
            
            affine.setToIdentity();
            
            rotateY.setAngle(
                    MathUtils.clamp(((rotateY.getAngle() + dragDelta.getX() * (1.0 * 0.25)) % 360 + 540) % 360 - 180, -360, 360)
            ); // horizontal                
            rotateX.setAngle(
                    MathUtils.clamp(((rotateX.getAngle() - dragDelta.getY() * (1.0 * 0.25)) % 360 + 540) % 360 - 180, -90, 90)
            ); // vertical
            
            affine.prepend(t.createConcatenation(rotateY.createConcatenation(rotateX)));
        }     
    }

    @Override
    protected void handleMiddleMouseDrag(MouseEvent event, Point2D dragDelta, double modifier) {
        // do nothing for now        
    }

    @Override
    protected void handleSecondaryMouseDrag(MouseEvent event, Point2D dragDelta, double modifier) {
        if (!mouseLookEnabled) {
            double deltaY = dragDelta.getY();

            if (deltaY < 0) {
                up = true;
            } else if (deltaY > 0) {
                down = true;
            }
        }
    }

    @Override
    protected void handleMouseMoved(MouseEvent event, Point2D moveDelta, double speed) {
        if (mouseLookEnabled) {
            t.setX(getPosition().getX());
            t.setY(getPosition().getY());
            t.setZ(getPosition().getZ());
            
            affine.setToIdentity();
            
            rotateY.setAngle(
                    MathUtils.clamp(((rotateY.getAngle() + moveDelta.getX() * (speed * 0.05)) % 360 + 540) % 360 - 180, -360, 360)
            ); // horizontal                
            rotateX.setAngle(
                    MathUtils.clamp(((rotateX.getAngle() - moveDelta.getY() * (speed * 0.05)) % 360 + 540) % 360 - 180, -90, 90)
            ); // vertical
            
            affine.prepend(t.createConcatenation(rotateY.createConcatenation(rotateX)));
            
        }
    }

    @Override
    protected void handleScrollEvent(ScrollEvent event) {
        //do nothing for now, use for Zoom?
    }

    @Override
    protected double getSpeedModifier(KeyEvent event) {
        return speed;
    }

    @Override
    public Node getTransformableNode() {
        if (getCamera() != null) {
            return getCamera();
        } else {
            throw new UnsupportedOperationException("Must have a Camera");
        }
    }

    public void setXYZ(double x, double y, double z) {
        affine.setTx(x);
        affine.setTy(y);
        affine.setTz(z);
    }

    public void setXYZ(Point3D point) {
        affine.setTx(point.getX());
        affine.setTy(point.getY());
        affine.setTz(point.getZ());
    }

    public void setX(double x) {
        affine.setTx(x);
    }

    public void setY(double y) {
        affine.setTy(y);
    }

    public void setZ(double z) {
        affine.setTz(z);
    }

    private void moveForward() {      
        affine.setTx(getPosition().getX() + speed * getForwardMatrixRow().x);
        affine.setTy(getPosition().getY() + speed * getForwardMatrixRow().y);
        affine.setTz(getPosition().getZ() + speed * getForwardMatrixRow().z);
    }

    private void strafeLeft() {
        affine.setTx(getPosition().getX() + speed * -getRightMatrixRow().x);
        affine.setTy(getPosition().getY() + speed * -getRightMatrixRow().y);
        affine.setTz(getPosition().getZ() + speed * -getRightMatrixRow().z);
    }

    private void strafeRight() {
        affine.setTx(getPosition().getX() + speed * getRightMatrixRow().x);
        affine.setTy(getPosition().getY() + speed * getRightMatrixRow().y);
        affine.setTz(getPosition().getZ() + speed * getRightMatrixRow().z);
    }

    private void moveBack() {
        affine.setTx(getPosition().getX() + speed * -getForwardMatrixRow().x);
        affine.setTy(getPosition().getY() + speed * -getForwardMatrixRow().y);
        affine.setTz(getPosition().getZ() + speed * -getForwardMatrixRow().z);
    }

    private void moveUp() {
        affine.setTx(getPosition().getX() + speed * -getUpMatrixRow().x);
        affine.setTy(getPosition().getY() + speed * -getUpMatrixRow().y);
        affine.setTz(getPosition().getZ() + speed * -getUpMatrixRow().z);
    }

    private void moveDown() {
        affine.setTx(getPosition().getX() + speed * getUpMatrixRow().x);
        affine.setTy(getPosition().getY() + speed * getUpMatrixRow().y);
        affine.setTz(getPosition().getZ() + speed * getUpMatrixRow().z);
    }

    public void setMouseLookEnabled(boolean b) {
        mouseLookEnabled = b;
    }

    @Override
    protected void handlePrimaryMouseClick(MouseEvent t) {
        //System.out.println("Primary Button Clicked!");
    }

    @Override
    protected void handleSecondaryMouseClick(MouseEvent t) {
        //System.out.println("Secondary Button Clicked!");
    }

    @Override
    protected void handleMiddleMouseClick(MouseEvent t) {
        //System.out.println("Middle Button Clicked!");
    }

    @Override
    protected void handlePrimaryMouseRelease(MouseEvent e) {

    }

    @Override
    protected void handleSecondaryMouseRelease(MouseEvent e) {
        up = false;
        down = false;
    }

    @Override
    protected void handleMiddleMouseRelease(MouseEvent e) {

    }

    @Override
    protected void handlePrimaryMousePress(MouseEvent e) {
        
    }

    @Override
    protected void handleSecondaryMousePress(MouseEvent e) {
       
    }

    @Override
    protected void handleMiddleMousePress(MouseEvent e) {
        
    }

    @Override
    protected void updateTransition(double now) {
        
    }

}
