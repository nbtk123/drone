package com.dji.sdkdemo.BrainControl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * Created by nbtk123 on 03/01/2017.
 */

public class BTController implements Serializable {

    private BluetoothAdapter bluetoothAdapter;

    WeakReference<Context> context;

    public BTController(Context context) {
        this.context = new WeakReference<Context>(context);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public boolean setup(Activity activityForResultIfRequestToEnableBt){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activityForResultIfRequestToEnableBt.startActivityForResult(enableBtIntent, BrainControlActivity.REQUEST_CODE_ENABLE_BT);
            }

            return true;
        }

        return false;
    }

    public boolean startBTServer() {
        if (bluetoothAdapter != null) {
            BTServerService.setInitData(new BTServerService.InitData(this));
            Intent intent = new Intent(context.get(), BTServerService.class);
            intent.setAction(BTServerService.ACTION_START_LISTENING);
            context.get().startService(intent);
            return true;
        }

        return false;
    }

    public void stopBTServer() {
        Intent intent = new Intent(context.get(), BTServerService.class);
        intent.setAction(BTServerService.ACTION_STOP_LISTENING);
        context.get().startService(intent);
    }
}
