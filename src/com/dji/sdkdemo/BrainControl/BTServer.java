package com.dji.sdkdemo.BrainControl;

import android.app.Activity;
import android.content.Context;

/**
 * Created by nbtk123 on 16/03/2017.
 */

public interface BTServer {
    boolean askForBT(Activity activity);
    void start();
    void shutdown();
}
