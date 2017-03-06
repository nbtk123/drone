package com.dji.sdkdemo.BrainControl;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;

/**
 * Created by nbtk123 on 06/03/2017.
 */

public class JoystickControllerImpl implements JoystickController{

    public static final int MOVEMENT_SPEED = 500;

    private int yaw;
    private int pitch;
    private int roll;
    private int throttle;

    private void move(int yaw, int pitch, int roll, int throttle) {
        DJIDrone.getDjiGroundStation().setAircraftJoystick(yaw, pitch, roll, throttle,new DJIGroundStationExecuteCallBack(){

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                // TODO Auto-generated method stub

            }

        });
    }

    private void setYaw(int yaw) {
        this.yaw = yaw;
        move(yaw, pitch, roll, throttle);
    }
    private void setPitch(int pitch) {
        this.pitch = pitch;
        move(yaw, pitch, roll, throttle);
    }
    private void setRoll(int roll) {
        this.roll = roll;
        move(yaw, pitch, roll, throttle);
    }
    private void setThrottle(int throttle) {
        this.throttle = throttle;
        move(yaw, pitch, roll, throttle);
    }

    @Override
    public void setMoveLeft(boolean move) {
        setRoll(move ? -MOVEMENT_SPEED : 0);
    }

    @Override
    public void setMoveRight(boolean move) {
        setRoll(move ? MOVEMENT_SPEED : 0);
    }

    @Override
    public void setMoveForward(boolean move) {
        setPitch(move ? MOVEMENT_SPEED : 0);
    }

    @Override
    public void setMoveBackward(boolean move) {
        setPitch(move ? -MOVEMENT_SPEED : 0);
    }
}