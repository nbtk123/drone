package com.dji.sdkdemo.BrainControl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by nbtk123 on 16/03/2017.
 */

public class BTReceiverThread extends Thread implements BTServer {

    private static final String TAG = BTReceiverThread.class.getSimpleName();

    private BluetoothAdapter btAdapter;
    private BTListener listener;

    public BTReceiverThread(BluetoothAdapter btAdapter, BTListener listener) {
        this.btAdapter = btAdapter;
        this.listener = listener;
    }

    @Override
    public void run() {

        BluetoothSocket btSocket = null;

        try {
            Iterator btDevicesIterator = btAdapter.getBondedDevices().iterator();
            BluetoothDevice btDevice = (BluetoothDevice) btDevicesIterator.next();
            while (!btDevice.getName().toLowerCase().contains("nbtk")) {
                btDevice = (BluetoothDevice) btDevicesIterator.next();
            }
            Log.d(TAG, "BT device = " + btDevice.getName());
            btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb"));
            Log.d(TAG, "Connecting...");
            btSocket.connect();

            Log.d(TAG, "Waiting for input...");
            InputStream inputStream = btSocket.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            char[] buf = new char[64];
            while (reader.read(buf) != -1) {
                String data = new String(buf).trim();
                if (!TextUtils.isEmpty(data)) {
                    Log.d(TAG, "Received from bluetooth: " + data);
                    listener.onBTDataReceived(data);
                }
            }

            Log.d(TAG, "End of receiving");
            btSocket.close();

        } catch (IOException e) {
            Log.e(TAG, "BTServerService.startListening() failed!");
            e.printStackTrace();
        }
    }

    @Override
    public boolean askForBT(Activity activityForResult) {
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activityForResult.startActivityForResult(enableBtIntent, BrainControlActivity.REQUEST_CODE_ENABLE_BT);
            }

            return true;
        }

        return false;
    }

    @Override
    public void shutdown() {
        interrupt();
    }
}
