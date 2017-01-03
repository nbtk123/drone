package com.dji.sdkdemo.BrainControl;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by nbtk123 on 03/01/2017.
 */

public class BTServerService extends IntentService {

    public static final String ACTION_START_LISTENING = "ACTION_START_LISTENING";
    public static final String ACTION_STOP_LISTENING = "ACTION_STOP_LISTENING";

    public static final String EXTRA_BT_CONTROLLER = "EXTRA_BT_CONTROLLER";

    ListenThread listenThread;

    public BTServerService() {
        super(BTServerService.class.getSimpleName());
        listenThread = new ListenThread();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_START_LISTENING)) {
            if (listenThread != null && !listenThread.isAlive()) {
                BTController btController = (BTController) intent.getSerializableExtra(EXTRA_BT_CONTROLLER);
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

        BluetoothServerSocket btServerSocket;
        BluetoothAdapter btAdapter;

        @Override
        public void run() {
            try {
                btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("DroneBrainControl", UUID.fromString("yoloblat"));
                BluetoothSocket btSocket = btServerSocket.accept();
                if (btSocket != null) {
                    InputStreamReader reader = new InputStreamReader(btSocket.getInputStream());
                    int c;
                    while ((c = reader.read()) != -1) {
                        Log.d("BLAT", "Received from bluetooth: " + c);
                    }
                }
                btServerSocket.close();
            } catch (IOException e) {
                Log.e("BLAT", "BTServerService.startListening() failed!");
                try { btServerSocket.close(); }
                catch (IOException e1) { e1.printStackTrace(); }
                e.printStackTrace();
            }
        }

        public void setBTAdapter(BluetoothAdapter btAdapter) {
            this.btAdapter = btAdapter;
        }
    }
}
