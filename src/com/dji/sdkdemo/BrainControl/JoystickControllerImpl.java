package com.dji.sdkdemo.BrainControl;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;

/**
 * Created by nbtk123 on 06/03/2017.
 */

public class JoystickControllerImpl implements JoystickController {

    public static final int MAX_SPEED = 500;

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

    @Override
    public void setYaw(int yaw) {

        if (yaw > MAX_SPEED || yaw < -MAX_SPEED) {
            yaw = MAX_SPEED;
        }

        this.yaw = yaw;
        move(yaw, pitch, roll, throttle);
    }
    public void setPitch(int pitch) {

        if (pitch > MAX_SPEED || pitch < -MAX_SPEED) {
            pitch = MAX_SPEED;
        }

        this.pitch = pitch;
        move(yaw, pitch, roll, throttle);
    }
    public void setRoll(int roll) {

        if (roll > MAX_SPEED || roll < -MAX_SPEED) {
            roll = MAX_SPEED;
        }

        this.roll = roll;
        move(yaw, pitch, roll, throttle);
    }
    public void setThrottle(int throttle) {

        if (throttle > MAX_SPEED || throttle < -MAX_SPEED) {
            throttle = MAX_SPEED;
        }

        this.throttle = throttle;
        move(yaw, pitch, roll, throttle);
    }
}