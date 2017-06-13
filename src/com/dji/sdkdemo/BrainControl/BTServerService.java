package com.dji.sdkdemo.BrainControl;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by nbtk123 on 03/01/2017.
 */

public class BTServerService extends IntentService {
    
    public static final String TAG = BTServerService.class.getSimpleName();
    
    public static final String ACTION_START_LISTENING = "ACTION_START_LISTENING";
    public static final String ACTION_STOP_LISTENING = "ACTION_STOP_LISTENING";

    private static InitData initData;

    BTController btController;
    ListenThread listenThread;

    public static void setInitData(InitData data) {
        initData = data;
    }

    public BTServerService() {
        super(BTServerService.class.getSimpleName());
        listenThread = new ListenThread();
        btController = initData.getBtConreoller();

        initData.clear();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_START_LISTENING)) {
            if (listenThread != null && !listenThread.isAlive()) {
                listenThread.setBTAdapter(btController.getBluetoothAdapter());
                listenThread.start();
            }
        } else if (intent.getAction().equals(ACTION_STOP_LISTENING)) {
            if (listenThread != null) {
                listenThread.interrupt();
            }
        }
    }

    private static class ListenThread extends Thread {

        //BluetoothServerSocket btServerSocket;
        BluetoothAdapter btAdapter;

        @Override
        public void run() {
            try {
                Log.d(TAG, "BT Listen thread is running...");
                BluetoothDevice btDevice = btAdapter.getBondedDevices().iterator().next();
                //UUID uuid = btDevice.getUuids()[1].getUuid();
                BluetoothSocket btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb"));
                btSocket.connect();
                //btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("DroneBrainControl", uuid);
                //BluetoothSocket btSocket = btServerSocket.accept(30000);
                Log.d(TAG, "PASSED IT!");
                if (btSocket != null) {
                    Log.d(TAG, "SUCCESSFULLY!");
                    InputStreamReader reader = new InputStreamReader(btSocket.getInputStream());
                    int c;
                    while ((c = reader.read()) != -1) {
                        Log.d(TAG, "Received from bluetooth: " + c);
                    }
                }
                //btServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "BTServerService.startListening() failed!");
                /*try { btServerSocket.close(); }
                catch (IOException e1) { e1.printStackTrace(); }*/
                e.printStackTrace();
            }
        }

        public void setBTAdapter(BluetoothAdapter btAdapter) {
            this.btAdapter = btAdapter;
        }
    }

    public static class InitData {
        private WeakReference<BTController> btController;

        public InitData(BTController btController) {
            this.btController = new WeakReference<BTController>(btController);
        }
        public BTController getBtConreoller() {
            return btController.get();
        }

        public void clear() {
            btController = null;
        }
    }

}
