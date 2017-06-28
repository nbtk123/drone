package com.dji.sdkdemo.BrainControl;

import android.util.Log;

import com.dji.sdkdemo.GsProtocolJoystickDemoActivity;

import java.util.Timer;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIGroundStationHoverCallBack;
import dji.sdk.interfaces.DJIGroundStationOneKeyFlyCallBack;

/**
 * Created by nbtk123 on 06/03/2017.
 */

public class JoystickControllerImpl implements JoystickController {

    public static final String TAG = JoystickControllerImpl.class.getSimpleName();
    public static final int MAX_SPEED = 500;

    private int yaw;
    private int pitch;
    private int roll;
    private int throttle;

    private void move(final int yaw, final int pitch, final int roll, final int throttle) {
        DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationHoverCallBack(){

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationHoverResult result) {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.name();
                Log.d(TAG, "move() --> pauseGroundStationTask --> result = ".concat(result.name()));

                setAircraftJoystick(yaw, pitch, roll, throttle);
            }
        });
    }

    private void setAircraftJoystick(final int yaw, final int pitch, final int roll, final int throttle) {
        DJIDrone.getDjiGroundStation().setAircraftJoystick(yaw, pitch, roll, throttle,new DJIGroundStationExecuteCallBack(){

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                // TODO Auto-generated method stub
                Log.d(TAG, "move() --> setAircraftJoystick --> result = ".concat(result.name()));
                Log.d(TAG, String.format("(yaw, pitch, roll, throttle) = (%s, %s, %s, %s)", yaw, pitch, roll, throttle));
            }
        });
    }

    private void moveSmooth(final int targetYaw, final int targetPitch, final int targetRoll, final int targetThrottle) {

        DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationHoverCallBack(){

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationHoverResult result) {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.name();
                Log.d(TAG, "move() --> pauseGroundStationTask --> result = ".concat(result.name()));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i=0; i<=MAX_SPEED; i+=50) {

                            int yawSign = targetYaw < 0 ? -1 : 1;
                            int pitchSign = targetPitch < 0 ? -1 : 1;
                            int rollSign = targetRoll < 0 ? -1 : 1;
                            int throttleSign = targetThrottle < 0 ? -1 : 1;

                            // Every paramter is set up to its target value + Check for negative values
                            int yaw = i <= Math.abs(targetYaw) ? i*yawSign : targetYaw;
                            int pitch = i <= Math.abs(targetPitch) ? i*pitchSign : targetPitch;
                            int roll = i <= Math.abs(targetRoll) ? i*rollSign : targetRoll;
                            int throttle = i <= Math.abs(targetThrottle) ? i*throttleSign : targetThrottle;

                            // let it fly
                            setAircraftJoystick(yaw, pitch, roll, throttle);

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void setYaw(int yaw) {

        if (yaw > MAX_SPEED || yaw < -MAX_SPEED) {
            int sign = yaw > 0 ? 1 : -1;
            yaw = MAX_SPEED * sign;
        }

        this.yaw = yaw;
        moveSmooth(yaw, pitch, roll, throttle);
    }
    public void setPitch(int pitch) {

        if (pitch > MAX_SPEED || pitch < -MAX_SPEED) {
            int sign = pitch > 0 ? 1 : -1;
            pitch = MAX_SPEED * sign;
        }

        this.pitch = pitch;
        moveSmooth(yaw, pitch, roll, throttle);
    }
    public void setRoll(int roll) {

        if (roll > MAX_SPEED || roll < -MAX_SPEED) {
            int sign = roll > 0 ? 1 : -1;
            roll = MAX_SPEED * sign;
        }

        this.roll = roll;
        moveSmooth(yaw, pitch, roll, throttle);
    }
    public void setThrottle(int throttle) {

        if (throttle > MAX_SPEED || throttle < -MAX_SPEED) {
            int sign = throttle > 0 ? 1 : -1;
            throttle = MAX_SPEED*sign;
        }

        this.throttle = throttle;
        moveSmooth(yaw, pitch, roll, throttle);
    }
}