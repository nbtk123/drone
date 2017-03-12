package com.dji.sdkdemo.BrainControl;

/**
 * Created by nbtk123 on 07/03/2017.
 */

public interface JoystickController {

    void setPitch(int value);
    void setRoll(int value);
    void setThrottle(int value);
    void setYaw(int value);
}
