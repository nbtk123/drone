package com.dji.sdkdemo.BrainControl.udp;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by nbtk123 on 10/03/2017.
 */

public class DroneOrderParser {

    public interface ParserListener {
        void onPitch(int value);
        void onRoll(int value);
        void onThrottle(int value);
        void onYaw(int value);
    }

    // {
    //  action: send,
    //  payload: {pitch: 500}
    // }

    public void parse(DatagramPacket packet, ParserListener listener) throws Exception {
        JSONObject json = new JSONObject(new String(packet.getData()));

        String action = json.optString("action");
        String payloadStr = json.optString("payload");

        if (action != null && payloadStr != null) {

            action = action.toLowerCase();
            JSONObject payload = new JSONObject(payloadStr.toLowerCase());

            if ("send".equals(action)) {
                if (payload.has("pitch")) {
                    listener.onPitch(payload.optInt("pitch", 0));
                } else if (payload.has("roll")) {
                    listener.onRoll(payload.optInt("roll", 0));
                } else if (payload.has("throttle")) {
                    listener.onThrottle(payload.optInt("throttle", 0));
                } else if (payload.has("yaw")) {
                    listener.onYaw(payload.optInt("yaw", 0));
                }
            }
        }
    }
}
